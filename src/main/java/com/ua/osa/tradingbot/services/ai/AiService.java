package com.ua.osa.tradingbot.services.ai;

import com.ua.osa.tradingbot.AppProperties;
import com.ua.osa.tradingbot.models.dto.MinMaxPoint;
import com.ua.osa.tradingbot.models.dto.publicReq.kline.KlineResponse;
import com.ua.osa.tradingbot.restClients.WhiteBitClient;
import com.ua.osa.tradingbot.scheduler.TaskManager;
import com.ua.osa.tradingbot.services.ai.dto.EducateModel;
import com.ua.osa.tradingbot.services.ai.dto.OperationEnum;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.deeplearning4j.datasets.iterator.utilty.ListDataSetIterator;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.GradientNormalization;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.evaluation.classification.Evaluation;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.dataset.api.preprocessor.DataNormalization;
import org.nd4j.linalg.dataset.api.preprocessor.NormalizerMinMaxScaler;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.springframework.stereotype.Component;
import org.ta4j.core.*;
import org.ta4j.core.indicators.MACDIndicator;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.StochasticOscillatorKIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsLowerIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsMiddleIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsUpperIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;

import java.io.File;
import java.time.*;
import java.util.*;

@Component
@Slf4j
public class AiService {
    private final String modelPath = "D:\\tradingModel.zip";
    private final String modelPathUi = "D:\\tradingModelUI";
    private final String prop = "D:\\dl4j-ui-conf.xml";
    private final MultiLayerNetwork model;
    private final WhiteBitClient whiteBitClient;
    private final TaskManager taskManager;

    public AiService(WhiteBitClient whiteBitClient, TaskManager taskManager) {
        log.warn("Start creating AI model... Time: " + LocalDateTime.now().toString());
        this.whiteBitClient = whiteBitClient;
        this.taskManager = taskManager;

        File modelFile = new File(modelPath);
        if (modelFile.exists()) {
            model = loadModel();
            System.out.println("Model loaded from file.");

        } else {
            double[] classWeightsArray = {1.0, 0.1, 0.1};
            INDArray classWeights = Nd4j.create(classWeightsArray);

            model = getMultiLayerNetworkMultiClass();
            model.init();

            taskManager.execute(() -> {
                try {
                    educateModel(null);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            });
        }
    }

    public OperationEnum getRecommendedOperation(BarSeries series) {
        INDArray predicted = getPrediction(series);
        log.warn("Predicted: " + predicted);
        return OperationEnum.getByOrdinar(predicted);
    }

    private INDArray getPrediction(BarSeries series) {
        Indicator<Num> closePrice = new ClosePriceIndicator(series);
        RSIIndicator rsi = new RSIIndicator(closePrice, 14);
        MACDIndicator macd = new MACDIndicator(closePrice, 12, 26);

        Num factor = series.numOf(2);
        BollingerBandsMiddleIndicator bbMiddle = new BollingerBandsMiddleIndicator(new SMAIndicator(closePrice, 20));
        BollingerBandsUpperIndicator bbUpper = new BollingerBandsUpperIndicator(bbMiddle, closePrice, factor);
        BollingerBandsLowerIndicator bbLower = new BollingerBandsLowerIndicator(bbMiddle, closePrice, factor);
        StochasticOscillatorKIndicator stochasticK = new StochasticOscillatorKIndicator(series, 14);

        INDArray testFeatures = Nd4j.create(new double[][]{
                {
                        rsi.getValue(series.getEndIndex()).doubleValue(),
                        macd.getValue(series.getEndIndex()).doubleValue(),
                        bbUpper.getValue(series.getEndIndex()).doubleValue(),
                        bbMiddle.getValue(series.getEndIndex()).doubleValue(),
                        bbLower.getValue(series.getEndIndex()).doubleValue(),
                        stochasticK.getValue(series.getEndIndex()).doubleValue(),
                        series.getLastBar().getVolume().doubleValue(),
                        series.getLastBar().getAmount().doubleValue()
                }
        });

        return this.model.output(testFeatures);
    }

    private MultiLayerNetwork getModel() {
        return model;
    }

    private MultiLayerNetwork educateModel(BarSeries series) {
        if (series == null) {
            series = getBarSeriesFromBroker();
        }

        List<MinMaxPoint> allMinMaxDifferences = findAllMinMaxDifferences(series.getBarData());

        final Set<Double> buyPrices = new HashSet<>();
        final Set<Double> sellPrices = new HashSet<>();

        for (MinMaxPoint minMaxPoint : allMinMaxDifferences) {
            if (minMaxPoint.getDifference() > 300) {
                buyPrices.add(minMaxPoint.getMinValue());
                sellPrices.add(minMaxPoint.getMaxValue());
            }
        }

        final Set<Double> buyPricesClear = new HashSet<>(buyPrices);
        final Set<Double> sellPricesClear = new HashSet<>(sellPrices);

        buyPricesClear.removeAll(sellPrices);
        sellPricesClear.removeAll(buyPrices);

        prepareDataAndEducate(series, buyPricesClear, sellPricesClear);
        return model;
    }

    private void prepareDataAndEducate(BarSeries series, Set<Double> buyPricesClear, Set<Double> sellPricesClear) {
        // Розрахунок індикаторів
        Indicator<Num> closePrice = new ClosePriceIndicator(series);
        RSIIndicator rsi = new RSIIndicator(closePrice, 14);
        MACDIndicator macd = new MACDIndicator(closePrice, 12, 26);
        Num factor = series.numOf(2);
        BollingerBandsMiddleIndicator bbMiddle = new BollingerBandsMiddleIndicator(new SMAIndicator(closePrice, 20));
        BollingerBandsUpperIndicator bbUpper = new BollingerBandsUpperIndicator(bbMiddle, closePrice, factor);
        BollingerBandsLowerIndicator bbLower = new BollingerBandsLowerIndicator(bbMiddle, closePrice, factor);
        StochasticOscillatorKIndicator stochasticK = new StochasticOscillatorKIndicator(series, 14);

        // Розбивка на тренувальні та тестові данні
        int numSamples = series.getBarCount();
        int trainSize = (int) (numSamples * 0.8);
        int testSize = numSamples - trainSize;

        // Підготовка данних для моделі
        double[][] featuresTrain = new double[trainSize][8];
        int[][] labelsTrain = new int[trainSize][3];
        double[][] featuresTest = new double[testSize][8];
        int[][] labelsTest = new int[testSize][3];

        List<EducateModel> data = getEducatadedData(series, buyPricesClear, sellPricesClear, closePrice, rsi, macd,
                bbMiddle, bbUpper, bbLower, stochasticK);

        int j = 0;
        for (int i = 0; i < data.size(); i++) {
            if (i < trainSize) {
                featuresTrain[i] = data.get(i).getFuture();
                labelsTrain[i] = data.get(i).getLabel();
            } else {
                featuresTest[j] = data.get(i).getFuture();
                labelsTest[j] = data.get(i).getLabel();
                j++;
            }
        }

        INDArray featuresINDArrayTrain = Nd4j.create(featuresTrain);
        INDArray labelsINDArrayTrain = Nd4j.create(labelsTrain);
        INDArray featuresINDArrayTest = Nd4j.create(featuresTest);
        INDArray labelsINDArrayTest = Nd4j.create(labelsTest);

        DataSet trainData = new DataSet(featuresINDArrayTrain, labelsINDArrayTrain);
        DataSet testData = new DataSet(featuresINDArrayTest, labelsINDArrayTest);

        // Нормалізація данних
        DataNormalization normalizer = new NormalizerMinMaxScaler();
        normalizer.fit(trainData);
        normalizer.transform(trainData);

        DataSetIterator trainIterator = new ListDataSetIterator<>(trainData.asList(), trainSize);
        DataSetIterator testIterator = new ListDataSetIterator<>(testData.asList(), testSize);

        // Educate
        taskManager.execute(() -> {
            log.warn("AI model start education... Time: " + LocalDateTime.now().toString());

            // Навчання моделі
            model.fit(trainIterator, 100);

            // Оцінка моделі
            Evaluation eval = new Evaluation();
            try {
                while (testIterator.hasNext()) {
                    DataSet testDataSet = testIterator.next();
                    INDArray output = model.output(testDataSet.getFeatures());
                    eval.eval(testDataSet.getLabels(), output);
                }
                System.out.println(eval.stats());
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }

            // Збереження моделі після навчання
            saveModel(model);
            System.out.println("Model saved to file.");

            log.warn("AI model has been educated... Time: " + LocalDateTime.now().toString());
        });
        log.warn("AI model has been created... Time: " + LocalDateTime.now().toString());
    }

    private void saveModel(MultiLayerNetwork model) {
        try {
            File locationToSave = new File(modelPath);
            boolean saveUpdater = true;
            ModelSerializer.writeModel(model, locationToSave, saveUpdater);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private MultiLayerNetwork loadModel() {
        try {
            File locationToSave = new File(modelPath);
            return ModelSerializer.restoreMultiLayerNetwork(locationToSave);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    private List<MinMaxPoint> findAllMinMaxDifferences(List<Bar> bars) {
        log.warn("start: findAllMinMaxDifferences... Time: " + LocalDateTime.now().toString());

        List<MinMaxPoint> minMaxPoints = new ArrayList<>();
        int n = bars.size();
        for (int i = 0; i < n; i++) {
            double min = bars.get(i).getClosePrice().doubleValue();
            for (int j = i + 1; j < n; j++) {
                double max = bars.get(j).getClosePrice().doubleValue();
                if (Math.abs(i - j) < 120 && j > i && min < max) {
                    minMaxPoints.add(new MinMaxPoint(i, j, min, max));
                }
            }
        }

        log.warn("finish: findAllMinMaxDifferences. Time: " + LocalDateTime.now().toString());
        return minMaxPoints;
    }

    @NonNull
    private List<EducateModel> getEducatadedData(BarSeries series,
                                                 Set<Double> buyPricesClear,
                                                 Set<Double> sellPricesClear,
                                                 Indicator<Num> closePrice,
                                                 RSIIndicator rsi,
                                                 MACDIndicator macd,
                                                 BollingerBandsMiddleIndicator bbMiddle,
                                                 BollingerBandsUpperIndicator bbUpper,
                                                 BollingerBandsLowerIndicator bbLower,
                                                 StochasticOscillatorKIndicator stochasticK) {
        List<EducateModel> data = new LinkedList<>();
        for (int i = 0; i < series.getBarCount(); i++) {
            int label;
            if (buyPricesClear.contains(closePrice.getValue(i).doubleValue())) {
                label = 1;
            } else if (sellPricesClear.contains(closePrice.getValue(i).doubleValue())) {
                label = 2;
            } else {
                label = 0;
            }
            data.add(new EducateModel(label, rsi.getValue(i).doubleValue(), macd.getValue(i).doubleValue(),
                    bbMiddle.getValue(i).doubleValue(), bbUpper.getValue(i).doubleValue(),
                    bbLower.getValue(i).doubleValue(), stochasticK.getValue(i).doubleValue(),
                    series.getBarData().getLast().getVolume().doubleValue(),
                    series.getBarData().getLast().getAmount().doubleValue()
            ));
        }
        return data;
    }

    @NonNull
    private BarSeries getBarSeriesFromBroker() {
        BarSeries series;
        series = new BaseBarSeries();
        try {
            long week = 604_800;
            long start = 1_710_783_000;
            long end = start + week;
            for (int i = 0; i < 30; i++) {
                KlineResponse response = whiteBitClient.getKlains(AppProperties.TRADE_PAIR.get().name(),
                        "1m", "1440", start, end);
                for (List<Object> kline : response.getResult()) {
                    long timestamp = ((Number) kline.get(0)).longValue();
                    ZonedDateTime endTime = ZonedDateTime.ofInstant(Instant.ofEpochSecond(timestamp), ZoneId.systemDefault());
                    Num open = DecimalNum.valueOf((String) kline.get(1));
                    Num high = DecimalNum.valueOf((String) kline.get(3));
                    Num low = DecimalNum.valueOf((String) kline.get(4));
                    Num close = DecimalNum.valueOf((String) kline.get(2));
                    Num volumeInStock = DecimalNum.valueOf((String) kline.get(5));
                    Num volumeInMoney = DecimalNum.valueOf((String) kline.get(6));
                    BaseBar bar = new BaseBar(
                            Duration.ofMinutes(5),
                            endTime, open, high, low, close, volumeInStock, volumeInMoney
                    );
                    try {
                        series.addBar(bar);
                    } catch (Exception e) {
                        log.error(e.getMessage());
                    }
                }
                start += week;
                end += week;
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return series;
    }

    @NonNull
    private MultiLayerNetwork getModelConfig2() {
        return new MultiLayerNetwork(new NeuralNetConfiguration.Builder()
                .seed(123)
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .updater(new Adam())
                .list()
                .layer(new DenseLayer.Builder()
                        .nIn(8)
                        .nOut(16)
                        .weightInit(WeightInit.XAVIER)
                        .activation(Activation.RELU)
                        .build())
                .layer(new DenseLayer.Builder()
                        .nIn(16)
                        .nOut(16)
                        .weightInit(WeightInit.XAVIER)
                        .activation(Activation.RELU)
                        .build())
                .layer(new OutputLayer.Builder(LossFunctions.LossFunction.MSE)
                        .nIn(16)
                        .nOut(1)
                        .weightInit(WeightInit.XAVIER)
                        .activation(Activation.SIGMOID)
                        .build())
                .build());
    }

    @NonNull
    private MultiLayerNetwork getMultiLayerNetworkMultiClass() {
        return new MultiLayerNetwork(new NeuralNetConfiguration.Builder()
                .seed(123)
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .updater(new Adam())
                .weightInit(WeightInit.XAVIER)
                .l2(0.001)  // Пример коэффициента L2 регуляризации
                .list()
                .layer(new DenseLayer.Builder()
                        .nIn(8)
                        .nOut(128)
                        .activation(Activation.RELU)
                        .build())
                .layer(new DenseLayer.Builder()
                        .nIn(128)
                        .nOut(64)
                        .activation(Activation.RELU)
                        .build())
                .layer(new DenseLayer.Builder()
                        .nIn(64)
                        .nOut(32)
                        .activation(Activation.RELU)
                        .build())
                .layer(new OutputLayer.Builder(LossFunctions.LossFunction.MCXENT)
                        .activation(Activation.SOFTMAX)
                        .nIn(32)
                        .nOut(3)
                        .weightInit(WeightInit.XAVIER)  // Инициализация весов
                        .build())
                .build());
    }

    @NonNull
    private MultiLayerNetwork getMultiLayerNetworkBinary() {
        return new MultiLayerNetwork(new NeuralNetConfiguration.Builder()
                .gradientNormalization(GradientNormalization.ClipElementWiseAbsoluteValue)
                .gradientNormalizationThreshold(1.0)
                .seed(123)
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .list()
                .layer(0, new DenseLayer.Builder()
                        .nIn(8)
                        .nOut(16)
                        .activation(Activation.RELU)
                        .weightInit(WeightInit.XAVIER)
                        .build())
                .layer(1, new OutputLayer.Builder(LossFunctions.LossFunction.XENT)  // Используем логистическую регрессию
                        .nIn(16)
                        .nOut(3)
                        .activation(Activation.SIGMOID)  // Функция активации Sigmoid для бинарной классификации
                        .weightInit(WeightInit.XAVIER)
                        .build())
                .build());
    }
}

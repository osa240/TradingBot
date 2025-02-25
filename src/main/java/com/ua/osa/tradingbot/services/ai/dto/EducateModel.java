package com.ua.osa.tradingbot.services.ai.dto;

public class EducateModel {
    private int[] label = new int[]{0, 0, 0};

    private double rsi;
    private double macd;
    private double bbMiddle;
    private double bbUpper;
    private double bbLower;
    private double stochasticK;
    private double volumeStock;
    private double volumeAmount;

    public EducateModel(int label,
                        double rsi,
                        double macd,
                        double bbMiddle,
                        double bbUpper,
                        double bbLower,
                        double stochasticK,
                        double volumeStock,
                        double volumeAmount) {
        this.label[label] = 1;
        if (isNotNaN(rsi)) {
            this.rsi = rsi;
        }
        if (isNotNaN(macd)) {
            this.macd = macd;
        }
        if (isNotNaN(bbMiddle)) {
            this.bbMiddle = bbMiddle;
        }
        if (isNotNaN(bbUpper)) {
            this.bbUpper = bbUpper;
        }
        if (isNotNaN(bbLower)) {
            this.bbLower = bbLower;
        }
        if (isNotNaN(stochasticK)) {
            this.stochasticK = stochasticK;
        }
        if (isNotNaN(volumeStock)) {
            this.volumeStock = volumeStock;
        }
        if (isNotNaN(volumeAmount)) {
            this.volumeAmount = volumeAmount;
        }
    }

    public int[] getLabel() {
        return label;
    }

    public double[] getFuture() {
        return new double[]{rsi, macd, bbMiddle, bbUpper, bbLower,
                stochasticK, volumeStock, volumeAmount};
    }

    private boolean isNotNaN(double data) {
        return !Double.isNaN(data) && !Double.isInfinite(data);
    }

    @Override
    public String toString() {
        return "EducateModel{"
                + "label=" + label
                + ", rsi=" + rsi
                + ", macd=" + macd
                + ", bbMiddle=" + bbMiddle
                + ", bbUpper=" + bbUpper
                + ", bbLower=" + bbLower
                + ", stochasticK=" + stochasticK
                + '}';
    }
}

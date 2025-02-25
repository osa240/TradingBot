# Trading bot:

<a id="content"></a>
#### Content:
- [About](#about)
- [How to use](#how-to-use)
- [Recommended parameters](#requires)
- [Configure Project](#configure)

<a id="about"></a>
## About the project:
Trading bot is a project for individual use. 
It is used as a trading bot for the WhiteBit exchange. 
The aims of this project was to get new skills with 
artificial intelligence (AI) technologies, telegram bot, 
and the latest Java technologies.


This application built on the architectural style of programming RESTful application (API) with command management
like telegram bot. In it, I used popular frameworks like Hibernate and Spring Boot, library org.deeplearning4j for AI.

<a id="how-to-use"></a>
## How to use?
In my application, I have implied access to resources by roles using command in telegram bot.

You are given the following commands in telegram bot for use in main menu:

Command | Description
--- | ---
/start_bot | starting trading bot
/stop_bot | stopping trading bot
/buy_flag | change status to buy (manual)
/sell_flag | change status to sell (manual)
/report | download a report
/report_orderbook | download a report of order book


<a id="requires"></a>
## Recommended parameters to run on your platform:
- OS: Windows / Linux / Mac OS
- DataBase: MySql (ver. 8.*) or another
- JDK: 21
- IDE: prefer JetBrains IntelliJ IDEA or another


<a id="configure"></a>
## Configure project and run:
1. Open search in Telegram and type @BotFather

    1.1. Run bot and choose in menu /newbot

    1.2. Follow instruction for complete

2. Open search in Telegram and type @getmyid_bot

   2.1. Run bot and you recieve your user_id
3. Please open the file src/main/resources/application.properties:
    
    3.1 insert bot settings:
```
# Telegram Bot setting
bot.name=BOT_NAME
bot.token=BOT_TOKEN
bot.userId=USER_ID
```

3.2 insert DB settings:
```
# Налаштування БД
spring.datasource.url=YOUR_DATABASE_URL
spring.datasource.username=YOUR_USERNAME
spring.datasource.password=YOUR_PASSWORD
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# Налаштування JPA и Hibernate
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.database=mysql
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
spring.jpa.defer-datasource-initialization=false
```

3.3 insert settings for WhiteBit exchange:
```
# Налаштування підключення до Біржі
whitebit.api.key=**************
whitebit.api.secret=**************
```

- Insert minimum data and configure to your needs
- Run project

For tests you can use my own Telegram bot:
```
in search: @trade_OsA_bot
token: 7204426990:AAF3-T8OKgbVYwjD9zhVtouL-kL18u3jveM
```
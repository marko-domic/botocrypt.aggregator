# Data Aggregator Service

A part of the botocrypt platform, the main purpose of this service is to collect all necessary data from cryptocurrency exchanges regarding specific cryptocurrencies, their prices and rest of their data. When they are collected, together they are sent to the **arbitrage service**.

## Architecture overview

An overview of Botocrypt architecture looks something like this:

![Botocrypt architecture preview](resources/botocrypt_architecture.png)

The main purpose of Aggregator service is to fetch all necessary data regarding cryptocurrencies from exchanges (their prices, matching pairs...), group and send them to the Arbitrage service. There is no advanced logic or any other calculation happening in this service.

## Registered exchanges

Exchanges from where Aggregator service receives cryptocurrency orders are:

* [CEX.IO](https://cex.io/)

## Registered currencies

Currencies with which Aggregator service works are:

* BTC
* USD

## Running the application locally

There are several ways to run a Botocrypt Aggregator service and initializer on your local machine.

Before starting Aggregator, it needs to be initialized with required data. It could be done by execute the `main` method in the `com.botocrypt.aggregator.InitRunner` with environment variable `SPRING_PROFILE=init` class from your IDE, or use [Spring Boot Gradle plugin](https://docs.spring.io/spring-boot/docs/current/reference/html/build-tool-plugins.html#build-tool-plugins-gradle-plugin) like so:

```shell
./gradlew bootRun --args='--spring.profiles.active=init'
```

Starting a service could be done by executing the `main` method in the `com.botocrypt.aggregator.Application` class from your IDE or running Gradle plugin command:

```shell
./gradlew bootRun
```

package com.example.demo.devicedatasimulator.simulator;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.Instant;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;

public class DeviceDataSimulator {

    // Numele exchange-ului și routing key-ul sunt aceleași ca în monitoring-service
    private static final String EXCHANGE_NAME = "device-data-exchange";
    private static final String ROUTING_KEY = "device.data";

    // CONFIG RabbitMQ pentru simulator (rulează pe host, nu în Docker network)
    private static final String RABBIT_HOST = "localhost";
    private static final int RABBIT_PORT = 5672;
    private static final String RABBIT_USER = "admin";
    private static final String RABBIT_PASSWORD = "admin";

    // Cât de des trimitem date (în secunde)
    // Pentru demo: 10 secunde. Pentru cerință finală poți pune 600 (10 minute).
    private static final long SEND_INTERVAL_SECONDS = 10;

    // Consum maxim aproximativ pe „măsurare”
    private static final double MIN_VALUE_KWH = 0.05;
    private static final double MAX_VALUE_KWH = 0.40;

    public static void main(String[] args) throws Exception {
        // Poți trece deviceId ca argument la rulare, altfel folosim unul hardcodat
        String deviceId = args.length > 0
                ? args[0]
                : "4019ad8f-b900-487a-9978-11c32bd67187"; // schimbă cu un device real din DB dacă vrei

        UUID.fromString(deviceId); // validare basic: aruncă excepție dacă nu e UUID

        System.out.println("Starting Device Data Simulator for device " + deviceId);
        System.out.println("Connecting to RabbitMQ at " + RABBIT_HOST + ":" + RABBIT_PORT);

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(RABBIT_HOST);
        factory.setPort(RABBIT_PORT);
        factory.setUsername(RABBIT_USER);
        factory.setPassword(RABBIT_PASSWORD);

        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {

            // Declaram exchange-ul pentru siguranță (direct, durabil)
            channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.DIRECT, true);

            Random random = new Random();

            while (true) {
                // 1. Timestamp în format ISO-8601 (compatibil cu OffsetDateTime.parse)
                String timestamp = Instant.now().toString();

                // 2. Valoare random între MIN și MAX
                double value = MIN_VALUE_KWH + (MAX_VALUE_KWH - MIN_VALUE_KWH) * random.nextDouble();

                // 3. JSON-ul trimis (identic cu ce așteaptă monitoring-service)
                DecimalFormatSymbols symbols = new DecimalFormatSymbols();
                symbols.setDecimalSeparator('.');

                DecimalFormat df = new DecimalFormat("#0.0000", symbols);
                String numericValue = df.format(value);
                String payload =
                        "{\"timestamp\":\"" + timestamp + "\"," +
                                "\"deviceId\":\"" + deviceId + "\"," +
                                "\"measurementValue\":" + numericValue +
                                "}";


                // 4. Publicăm mesajul în RabbitMQ
                channel.basicPublish(
                        EXCHANGE_NAME,
                        ROUTING_KEY,
                        null,
                        payload.getBytes(StandardCharsets.UTF_8)
                );

                System.out.println("Sent message: " + payload);

                // 5. Pauză între mesaje
                Thread.sleep(SEND_INTERVAL_SECONDS * 1000);
            }
        }
    }
}

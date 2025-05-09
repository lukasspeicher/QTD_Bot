package com.qtd.bot.stonks;

import com.qtd.bot.Command;
import com.qtd.bot.QTDBot;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.javacord.api.event.message.MessageCreateEvent;

import org.json.*;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;


public class Crypto implements Command {

    final String COMMAND = "crypto";

    final String COMMAND_OPTIONAL = " <name> <option>";

    final String DESCRIPTION = "Gibt den aktuellen Preis der Kryptowährung zurück (in $ und €).\n <name> = Name der Kryptowährung\n<option> = FIAT-Währung (z.B. €)\noder graph = Stellt die Preisentwicklung als Graph dar. Interval möglich (z.B. 2d).";

    @Override
    public String getCommand() {
        return COMMAND;
    }

    @Override
    public String getCommandOptional() {
        return COMMAND_OPTIONAL;
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    @Override
    public void sendEventMessage(MessageCreateEvent event, String option) {

        checkCurrencyExchange();

        String crypto = option;

        String currency = "";

        if (option.contains(" ")) {
            crypto = option.substring(0, option.indexOf(' '));

            currency = option.substring(option.indexOf(' ') + 1, option.indexOf(' ') + 2);

            if (option.contains("graph")) {
                String possibleTimeInterval = option.substring(option.indexOf("graph"));

                if (possibleTimeInterval.contains("d")) {
                    printGraph(event, crypto, option.substring(option.lastIndexOf(' ') + 1));
                } else {
                    printGraph(event, crypto, null);
                }
            }

        }

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder().url("https://api.coincap.io/v2/assets/" + crypto.toLowerCase()).build();

        try (Response response = client.newCall(request).execute()) {

            JSONObject json = new JSONObject(response.body().string());

            String price = json.getJSONObject("data").get("priceUsd").toString();

            double price_value = Double.parseDouble(price.substring(0, price.lastIndexOf('.') + 3));
            DecimalFormat formatter = new DecimalFormat("#,###.00");

            String output = "";

            output += "Aktueller Preis von " + crypto + ":\n";

            if (currency.equals("€")) {
                output += formatter.format(price_value * Currency.usdToEur) + " €";
            } else if (currency.equals("$")) {
                output += formatter.format(price_value) + " $";
            } else {
                output += formatter.format(price_value) + " $\n";
                output += formatter.format(price_value * Currency.usdToEur) + " €";
            }

            QTDBot.LOGGER.info("Crypto message sent to channel");
            event.getChannel().sendMessage(output);

        } catch (IOException | JSONException e) {
            QTDBot.LOGGER.warning("Crypto message could not be generated: " + e.getMessage());
            event.getChannel().sendMessage("Name oder Symbol nicht gefunden");
        }

    }

    void printGraph(MessageCreateEvent event, String crypto, String givenInterval) {

        Timestamp currentTime;
        Timestamp startTime;

        if (givenInterval != null && !givenInterval.equalsIgnoreCase("") && !givenInterval.equalsIgnoreCase(" ")) {

            int timeInterval = Integer.parseInt(givenInterval.substring(0, givenInterval.indexOf('d')));

            currentTime = new Timestamp(new Date().getTime());
            startTime = new Timestamp(currentTime.getTime() - ((1000 * 60 * 60 * 24) * timeInterval));
        } else {
            currentTime = new Timestamp(new Date().getTime());
            startTime = new Timestamp(currentTime.getTime() - (1000 * 60 * 60 * 24));
        }

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder().url("https://api.coincap.io/v2/assets/" + crypto.toLowerCase() + "/history?interval=h1&start=" + startTime.getTime() + "&end=" + currentTime.getTime()).build();

        ArrayList<SpecificValue> data = new ArrayList<>();

        try (Response response = client.newCall(request).execute()) {

            JSONObject json = new JSONObject(response.body().string());

            JSONArray jsonArray = json.getJSONArray("data");

            for (int i = 0; i < jsonArray.length(); i++) {
                data.add(new SpecificValue(jsonArray.getJSONObject(i).get("priceUsd").toString(), new Timestamp(Long.parseLong(jsonArray.getJSONObject(i).get("time").toString()))));
            }

            QTDBot.LOGGER.info("Time interval for crypto generated");
        } catch (IOException | JSONException e) {
            QTDBot.LOGGER.warning("Crypto time interval could not be generated: " + e.getMessage());
            event.getChannel().sendMessage("Name oder Symbol nicht gefunden");
        }

        QTDBot.LOGGER.info("Sent graph to channel");
        event.getChannel().sendMessage("Preisentwicklung von " + crypto + " (in $):");
        event.getChannel().sendMessage(GraphGenerator.generateGraph(data, crypto));
    }

    void checkCurrencyExchange() {
        if (Currency.timestampLastRequest == null) {
            CurrencyRequest();
        } else {
            java.util.Date date = new java.util.Date();
            Timestamp timestampNow = new Timestamp(date.getTime());

            long diffMilliseconds = timestampNow.getTime() - Currency.timestampLastRequest.getTime();

            long minutes = diffMilliseconds / (60 * 1000);

            if (minutes > 60) {
                CurrencyRequest();
            }
        }
    }

    final String fixerApi = "5504a61518bd67f319e9a4fa9f3f19fa";

    void CurrencyRequest() {

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder().url("http://data.fixer.io/api/latest?access_key=" + fixerApi + "&base=EUR" + "&symbols=USD").build();

        try (Response response = client.newCall(request).execute()) {

            JSONObject json = new JSONObject(response.body().string());

            double exchangeRate = Double.parseDouble(json.getJSONObject("rates").get("USD").toString());

            Currency.usdToEur = (1 / exchangeRate);

            Currency.timestampLastRequest = new Timestamp(new java.util.Date().getTime());

            QTDBot.LOGGER.info("New currency exchange value retrieved");

        } catch (IOException | JSONException e) {
            QTDBot.LOGGER.severe("Currency exchange value could not be retrieved: " + e.getMessage());
        }

    }

}

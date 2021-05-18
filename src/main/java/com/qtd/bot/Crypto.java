package com.qtd.bot;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.javacord.api.event.message.MessageCreateEvent;

import org.json.*;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.DecimalFormat;


public class Crypto implements Command{

    final String COMMAND = "crypto";

    final String COMMAND_OPTIONAL = " <name> <option>";

    final String DESCRIPTION = "Gibt den aktuellen Preis der Kryptowährung zurück (in $ und €).\n <name> = Name der Kryptowährung\n<option> = FIAT-Währung (z.B. €)";

    @Override
    public String getCommand() {
        return COMMAND;
    }

    @Override
    public String getCommandOptional() { return COMMAND_OPTIONAL; }

    @Override
    public String getDescription() { return DESCRIPTION; }

    @Override
    public void sendEventMessage(MessageCreateEvent event, String option) {

        checkCurrencyExchange();

        String crypto = option;

        String currency = "";

        if(option.contains(" ")){
            crypto = option.substring(0, option.lastIndexOf(' ')+1);

            currency = option.substring(option.lastIndexOf(' ')+1);
        }

        // System.out.println(crypto + " " + currency);

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("http://api.coincap.io/v2/assets/" + crypto.toLowerCase())
                .build();

        //System.out.println(request.toString());

        try (Response response = client.newCall(request).execute()) {

            JSONObject  json = new JSONObject (response.body().string());

            //System.out.println(json.toString());

            String price = json.getJSONObject("data").get("priceUsd").toString();

            double price_value = Double.parseDouble(price.substring(0, price.lastIndexOf('.')+3));
            DecimalFormat formatter = new DecimalFormat("#,###.00");

            event.getChannel().sendMessage("Aktueller Preis von " + crypto + ":\n");

            if (currency.equals("€")){
                event.getChannel().sendMessage("~ " + formatter.format(price_value*Currency.usdToEur) + " €");
            } else if (currency.equals("$")){
                event.getChannel().sendMessage(formatter.format(price_value) + " $");
            } else {
                event.getChannel().sendMessage(formatter.format(price_value) + "$\n");

                event.getChannel().sendMessage("~ " + formatter.format(price_value*Currency.usdToEur) + "€");
            }

        } catch (IOException | JSONException e) {
            event.getChannel().sendMessage("Name oder Symbol nicht gefunden");
            e.printStackTrace();
        }
/*
        QuickChart chart = new QuickChart();
        chart.setWidth(500);
        chart.setHeight(300);
        chart.setConfig("{"
                + "    type: 'bar',"
                + "    data: {"
                + "        labels: ['Q1', 'Q2', 'Q3', 'Q4'],"
                + "        datasets: [{"
                + "            label: 'Users',"
                + "            data: [50, 60, 70, 180]"
                + "        }]"
                + "    }"
                + "}"
        );

        System.out.println(chart.getUrl());

        // Create the embed
        event.getChannel().sendMessage( new EmbedBuilder()
                .setTitle("Bot Commands")
                .setDescription("Hier der Überblick über alle Commands:")
                .setAuthor("QTD-Bot", "", "")
                .setColor(Color.BLUE)
                .setImage(new File(chart.getUrl()))
                .setTimestamp(Instant.now()));
*/
    }

    void checkCurrencyExchange(){

        if(Currency.timestampLastRequest == null){
            CurrencyRequest();
        } else {
            java.util.Date date = new java.util.Date();
            Timestamp timestampNow = new Timestamp(date.getTime());

            long diffMilliseconds = timestampNow.getTime() - Currency.timestampLastRequest.getTime();

            long minutes = diffMilliseconds / (60 * 1000);

            //System.out.println(minutes);

            if(minutes > 60){
                CurrencyRequest();
            }
        }

    }

    final String fixerApi = "5504a61518bd67f319e9a4fa9f3f19fa";

    void CurrencyRequest(){

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("http://data.fixer.io/api/latest?access_key=" + fixerApi + "&base=EUR" + "&symbols=USD")
                .build();

        //System.out.println(request.toString());

        try (Response response = client.newCall(request).execute()) {

            JSONObject  json = new JSONObject (response.body().string());

            //System.out.println(json.toString());

            double exchangeRate = Double.parseDouble(json.getJSONObject("rates").get("USD").toString());

            Currency.usdToEur = (1/exchangeRate);

            //System.out.println(Currency.usdToEur);

            Currency.timestampLastRequest = new Timestamp(new java.util.Date().getTime());

        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

    }

}

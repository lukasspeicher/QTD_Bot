package com.qtd.bot;

import com.sedmelluq.discord.lavaplayer.natives.statistics.CpuStatistics;
import io.quickchart.QuickChart;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.MessageCreateEvent;

import org.json.*;

import java.awt.*;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;


public class Crypto implements Command{
    //Hallo erstmal

    final String COMMAND = "crypto";

    final String COMMAND_OPTIONAL = " <name> <option>";

    final String DESCRIPTION = "Gibt den aktuellen Preis der Kryptowährung zurück (in $ und €).\n <name> = Name der Kryptowährung\n<option> = FIAT-Währung (z.B. €)\noder graph = Stellt die Preisentwicklung als Graph dar";

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
            crypto = option.substring(0, option.lastIndexOf(' '));

            currency = option.substring(option.lastIndexOf(' ')+1);
        }

        if(currency.contains("graph")){
            createGraph(event, crypto);
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

            String output = "";

            output += "Aktueller Preis von " + crypto + ":\n";

            if (currency.equals("€")){
                output += formatter.format(price_value*Currency.usdToEur) + " €";
            } else if (currency.equals("$")){
                output += formatter.format(price_value) + " $";
            } else {
                output += formatter.format(price_value) + " $\n";
                output += formatter.format(price_value*Currency.usdToEur) + " €";
            }

            event.getChannel().sendMessage(output);

        } catch (IOException | JSONException e) {
            event.getChannel().sendMessage("Name oder Symbol nicht gefunden");
            e.printStackTrace();
        }

    }

    void createGraph (MessageCreateEvent event, String crypto){

        Timestamp currentTime = new Timestamp(new Date().getTime());

        Timestamp startTime = new Timestamp(currentTime.getTime() - (1000 * 60 * 60 * 24));

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("http://api.coincap.io/v2/assets/" + crypto.toLowerCase() + "/history?interval=h1&start=" + startTime.getTime() + "&end=" + currentTime.getTime())
                .build();

        //System.out.println(request.toString());

        ArrayList<HistoricValue> data = new ArrayList<>();

        try (Response response = client.newCall(request).execute()) {

            JSONObject  json = new JSONObject (response.body().string());

            //System.out.println(json.toString());

            JSONArray jsonArray = json.getJSONArray("data");

            //System.out.println(jsonArray.toString());

            for (int i = 0; i < jsonArray.length(); i++) {
                data.add(new HistoricValue(jsonArray.getJSONObject(i).get("priceUsd").toString(), new Timestamp(Long.parseLong(jsonArray.getJSONObject(i).get("time").toString()))));
            }

        } catch (IOException | JSONException e) {
            event.getChannel().sendMessage("Name oder Symbol nicht gefunden");
            e.printStackTrace();
        }

        double priceBefore = Double.parseDouble(data.get(0).getPriceUSD());
        double priceNow = Double.parseDouble(data.get(23).getPriceUSD());

        int r = 204;
        int g = 0;
        int b = 0;

        if(priceNow >= priceBefore){
            r = 0;
            g = 204;
            b = 0;
        }

        // Bisschen beschissen, aber funktionsfähig \0/
        String config = String.format("{"
                + "    type: 'line',"
                + "    data: {"
                + "        labels: ['%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s'],"
                + "        datasets: [{"
                + "            label: '%s (24h)',"
                + "            data: [%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s],"
                + "            borderColor: 'rgb(%d, %d, %d)'"
                + "        }]"
                + "    }"
                + "}", data.get(0).getTimestamp(), data.get(1).getTimestamp(), data.get(2).getTimestamp(), data.get(3).getTimestamp(), data.get(4).getTimestamp(), data.get(5).getTimestamp(), data.get(6).getTimestamp(), data.get(7).getTimestamp(),
                data.get(8).getTimestamp(), data.get(9).getTimestamp(), data.get(10).getTimestamp(), data.get(11).getTimestamp(), data.get(12).getTimestamp(), data.get(13).getTimestamp(), data.get(14).getTimestamp(), data.get(15).getTimestamp(),
                data.get(16).getTimestamp(), data.get(17).getTimestamp(), data.get(18).getTimestamp(), data.get(19).getTimestamp(), data.get(20).getTimestamp(), data.get(21).getTimestamp(), data.get(22).getTimestamp(), data.get(23).getTimestamp(),
                crypto.toUpperCase(),
                data.get(0).getPriceUSD(), data.get(1).getPriceUSD(), data.get(2).getPriceUSD(), data.get(3).getPriceUSD(), data.get(4).getPriceUSD(), data.get(5).getPriceUSD(), data.get(6).getPriceUSD(), data.get(7).getPriceUSD(),
                data.get(8).getPriceUSD(), data.get(9).getPriceUSD(), data.get(10).getPriceUSD(), data.get(11).getPriceUSD(), data.get(12).getPriceUSD(), data.get(13).getPriceUSD(), data.get(14).getPriceUSD(), data.get(15).getPriceUSD(),
                data.get(16).getPriceUSD(), data.get(17).getPriceUSD(), data.get(18).getPriceUSD(), data.get(19).getPriceUSD(), data.get(20).getPriceUSD(), data.get(21).getPriceUSD(), data.get(22).getPriceUSD(), data.get(23).getPriceUSD(),
                r, g, b);

        QuickChart chart = new QuickChart();
        chart.setWidth(800);
        chart.setHeight(500);
        chart.setBackgroundColor("#ffffff");
        chart.setConfig(config);

        //System.out.println(chart.getUrl());


        event.getChannel().sendMessage("Preisentwicklung von " + crypto + " in den letzten 24h (in $):");
        event.getChannel().sendMessage(chart.getUrl());

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

    class HistoricValue{
        String priceUSD;
        Timestamp timestamp;

        public HistoricValue(String priceUSD, Timestamp timestamp) {
            this.priceUSD = priceUSD;
            this.timestamp = timestamp;
        }

        public String getPriceUSD() {
            return priceUSD;
        }

        public void setPriceUSD(String priceUSD) {
            this.priceUSD = priceUSD;
        }

        public Timestamp getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(Timestamp timestamp) {
            this.timestamp = timestamp;
        }
    }

}

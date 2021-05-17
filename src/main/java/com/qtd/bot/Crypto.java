package com.qtd.bot;

import io.quickchart.QuickChart;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.MessageCreateEvent;

import org.json.*;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.time.Instant;


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
                event.getChannel().sendMessage("~ " + formatter.format(price_value*Currency.USDTOEUR) + " €");
            } else if (currency.equals("$")){
                event.getChannel().sendMessage(formatter.format(price_value) + " $");
            } else {
                event.getChannel().sendMessage(formatter.format(price_value) + "$\n");

                event.getChannel().sendMessage("~ " + formatter.format(price_value*Currency.USDTOEUR) + "€");
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

}

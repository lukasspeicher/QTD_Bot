package com.qtd.bot;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.javacord.api.event.message.MessageCreateEvent;

import org.json.*;

import java.io.IOException;


public class CMC implements Command{

    final String COMMAND = "crypto";

    private static final String apiKey = "85be82d7-75a7-48bc-8646-37d1232d32d2";

    @Override
    public String getCommand() {
        return COMMAND;
    }

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

            if (currency.equals("€")){

                String roundedOutput = String.format("%.2f", price_value*0.84);

                event.getChannel().sendMessage("~ " + roundedOutput + " €");
            } else {
                event.getChannel().sendMessage(price_value + " $");
            }

        } catch (IOException | JSONException e) {
            event.getChannel().sendMessage("Name oder Symbol nicht gefunden");
            e.printStackTrace();
        }

    }

}

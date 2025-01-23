package com.qtd.bot.stonks;

import com.qtd.bot.Command;
import com.qtd.bot.QTDBot;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.javacord.api.event.message.MessageCreateEvent;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;

public class Stocks implements Command {

    final String COMMAND = "stock";

    final String COMMAND_OPTIONAL = " <symbol> <option>";

    final String DESCRIPTION = "Gibt den aktuellen Preis der Aktie zurück.\n <symbol> = Symbol der Aktie (z.B. AAPL oder Apple)\n<option> = graph -> Stellt die Preisentwicklung als Graph dar. Intervalle möglich (z.B. 2d, 1mo, 1y, max).";

    final String yApiKey = "VDEfuf3jds6nqdtw8wew8aBcKrfeNJwN9kGJj5iT";

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

        String symbol = "";

        if (option.contains(" ")) {
            symbol = parseQueryToSymbol(option.substring(0, option.indexOf(' ')));

            if (option.contains("graph")) {

                String possibleTimeInterval = option.substring(option.indexOf("graph"));

                if (possibleTimeInterval.contains(" ")) {
                    printGraph(event, symbol, option.substring(option.lastIndexOf(' ') + 1));
                } else {
                    printGraph(event, symbol, null);
                }
            }

        } else {
            symbol = parseQueryToSymbol(option);
        }

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder().url("https://yfapi.net/v6/finance/quote?region=DE&lang=de&symbols=" + symbol).header("x-api-key", yApiKey).build();

        try (Response response = client.newCall(request).execute()) {

            JSONObject json = new JSONObject(response.body().string());

            JSONObject result = json.getJSONObject("quoteResponse").getJSONArray("result").getJSONObject(0);

            Double price = result.getDouble("regularMarketPrice");

            String output = "";

            output += "Aktueller Preis von " + symbol.toUpperCase() + ":\n";

            output += price + " $";

            QTDBot.LOGGER.info("Stock message sent to channel");

            event.getChannel().sendMessage(output);

        } catch (IOException | JSONException e) {
            QTDBot.LOGGER.warning("Stock request not successful: " + e.getMessage());
            event.getChannel().sendMessage("Name oder Symbol nicht gefunden");
        }

    }

    String parseQueryToSymbol(String query) {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder().url("https://yfapi.net/v6/finance/autocomplete?region=DE&lang=de&query=" + query).header("x-api-key", yApiKey).build();

        try (Response response = client.newCall(request).execute()) {

            JSONObject json = new JSONObject(response.body().string());

            JSONObject result = json.getJSONObject("ResultSet").getJSONArray("Result").getJSONObject(0);

            QTDBot.LOGGER.info("Symbol resolution request successful");

            return result.getString("symbol");
        } catch (IOException | JSONException e) {
            QTDBot.LOGGER.warning("Symbol resolution request not successful: " + e.getMessage());
        }

        return null;
    }

    void printGraph(MessageCreateEvent event, String symbol, String range) {

        if (range == null || range.equalsIgnoreCase("") || range.equalsIgnoreCase(" ")) {
            range = "1d";
        }

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder().url("https://yfapi.net/v8/finance/spark?interval=1h&range=" + range + "&symbols=" + symbol).header("x-api-key", yApiKey).build();

        ArrayList<SpecificValue> data = new ArrayList<>();

        try (Response response = client.newCall(request).execute()) {

            JSONObject json = new JSONObject(response.body().string()).getJSONObject(symbol);

            JSONArray timestamps = json.getJSONArray("timestamp");

            for (int i = 0; i < timestamps.length(); i++) {
                SpecificValue value = new SpecificValue();
                value.setTimestamp(new Timestamp(Long.parseLong(timestamps.get(i).toString()) * 1000)); // Time is in seconds. Should be milliseconds

                JSONArray close = json.getJSONArray("close");
                value.setPriceUSD(Double.toString(close.getDouble(i)));

                data.add(value);
            }

            QTDBot.LOGGER.info("Data request for generating graph successful");
        } catch (IOException | JSONException e) {
            event.getChannel().sendMessage("Name oder Symbol nicht gefunden");
            QTDBot.LOGGER.warning("Name or Symbol not found: " + e.getMessage());
        }

        QTDBot.LOGGER.info("Sent graph to channel");
        event.getChannel().sendMessage("Preisentwicklung von " + symbol + " (in $):");
        event.getChannel().sendMessage(GraphGenerator.generateGraph(data, symbol));
    }
}

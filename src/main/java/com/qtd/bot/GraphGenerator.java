package com.qtd.bot;

import io.quickchart.QuickChart;

import java.util.ArrayList;

public class GraphGenerator {

    public static String generateGraph (ArrayList<SpecificValue> data, String item) {

        double priceBefore = Double.parseDouble(data.get(0).getPriceUSD());
        double priceNow = Double.parseDouble(data.get(data.size()-1).getPriceUSD());

        int r = 204;
        int g = 0;
        int b = 0;

        if(priceNow >= priceBefore){
            r = 0;
            g = 204;
        }

        StringBuilder result = new StringBuilder("{"
                + "    type: 'line',"
                + "    data: {"
                + "        labels: [");

        for (SpecificValue value: data) {
            if (data.indexOf(value) == data.size()-1) {
                result.append("'").append(value.getTimestamp()).append("'");
            } else {
                result.append("'").append(value.getTimestamp()).append("', ");
            }
        }

        result.append("],"
                + "        datasets: [{"
                + "            label: '").append(item.toUpperCase()).append("',")
                .append("            data: [");

        for (SpecificValue value: data) {
            if (data.indexOf(value) == data.size()-1) {
                result.append(value.getPriceUSD());
            } else {
                result.append(value.getPriceUSD()).append(", ");
            }
        }

        result.append("]," + "            borderColor: 'rgb(").append(r).append(", ").append(g).append(", ").append(b).append(")'").append("        }]").append("    }").append("}");

        QuickChart chart = new QuickChart();
        chart.setWidth(800);
        chart.setHeight(500);
        chart.setBackgroundColor("#ffffff");
        chart.setConfig(result.toString());

        return chart.getShortUrl();
    }

    // Alte Lösung für Anschauungszwecke
    /*
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
     */
}

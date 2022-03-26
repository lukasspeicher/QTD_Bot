package com.qtd.bot.stonks;

import java.sql.Timestamp;

public class SpecificValue {

    String priceUSD;
    Timestamp timestamp;

    public SpecificValue () {}

    public SpecificValue (String priceUSD, Timestamp timestamp) {
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

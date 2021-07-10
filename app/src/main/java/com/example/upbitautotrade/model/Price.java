package com.example.upbitautotrade.model;

import com.google.gson.annotations.SerializedName;

public class Price {
    @SerializedName("currency")
    private String currency;

    @SerializedName("price_unit")
    private String priceUnit;

    @SerializedName("min_total")
    private Number minTotal;

    public String getCurrency() {
        return currency;
    }

    public String getPriceUnit() {
        return priceUnit;
    }

    public Number getMinTotal() {
        return minTotal;
    }
}

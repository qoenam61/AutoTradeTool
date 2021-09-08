package com.example.upbitautotrade.model;

import com.example.upbitautotrade.fragment.MarketUSDTDelta;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Candle implements Serializable, Comparable<Candle> {
    @SerializedName("market")
    String market;

    @SerializedName("candle_date_time_utc")
    String candleDateTimeUtc;

    @SerializedName("candle_date_time_kst")
    String candleDateTimeKst;

    @SerializedName("opening_price")
    Number openingPrice;

    @SerializedName("high_price")
    Number highPrice;

    @SerializedName("low_price")
    Number lowPrice;

    @SerializedName("trade_price")
    Number tradePrice;

    @SerializedName("timestamp")
    Long timestamp;

    @SerializedName("candle_acc_trade_price")
    Number candleAccTradePrice;

    @SerializedName("candle_acc_trade_volume")
    Number candleAccTradeVolume;

    @SerializedName("unit")
    Integer unit;

    double changedPrice;

    double changedRate;

    public String getMarketId() {
        return market;
    }

    public String getCandleDateTimeUtc() {
        return candleDateTimeUtc;
    }

    public String getCandleDateTimeKst() {
        return candleDateTimeKst;
    }

    public Number getOpeningPrice() {
        return openingPrice;
    }

    public Number getHighPrice() {
        return highPrice;
    }

    public Number getLowPrice() {
        return lowPrice;
    }

    public Number getTradePrice() {
        return tradePrice;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public Number getCandleAccTradePrice() {
//        return Math.floor(candleAccTradePrice.doubleValue() / 1000000);
        return candleAccTradePrice;
    }

    public Number getCandleAccTradeVolume() {
        return candleAccTradeVolume;
    }

    public Integer getUnit() {
        return unit;
    }

    public double getChangedPrice() {
        return changedPrice;
    }

    public double getChangedRate() {
        return changedRate;
    }

    public void setChangedPrice(double changedPrice) {
        this.changedPrice = changedPrice;
    }

    public void setChangedRate(double changedRate) {
        this.changedRate = changedRate;
    }

    @Override
    public int compareTo(Candle o) {
        double originalData = this.getCandleAccTradePrice().doubleValue();
        double compareData = o.getCandleAccTradePrice().doubleValue();

        if (originalData < compareData) {
            return 1;
        } else if (originalData > compareData) {
            return -1;
        } else {
            return 0;
        }
    }
}

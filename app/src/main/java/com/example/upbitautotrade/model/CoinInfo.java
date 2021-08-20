package com.example.upbitautotrade.model;


import android.util.Log;

import java.text.DecimalFormat;

public class CoinInfo {
    String marketId;
    double openPrice;
    double closePrice;
    double highPrice;
    double lowPrice;
    long buyingTime;
    double sellPrice;

    double profitRate;

    private String status = "Waiting";

    public static final String WAITING = "Waiting";
    public static final String BUY = "Buy";
    public static final String SELL = "Sell";

    public CoinInfo(double openPrice, double closePrice, double highPrice, double lowPrice) {
        this.openPrice = openPrice;
        this.closePrice = closePrice;
        this.highPrice = highPrice;
        this.lowPrice = lowPrice;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public double getToBuyPrice() {
        return convertPrice(Math.min((openPrice + closePrice) / 2, (highPrice + lowPrice) / 2));
    }

    public void setBuyingTime(long buyingTime) {
        this.buyingTime = buyingTime;
    }

    public long getBuyingTime() {
        return buyingTime;
    }

    public double getSellPrice() {
        return sellPrice;
    }

    public void setSellPrice(double sellPrice) {
        this.sellPrice = sellPrice;
    }

    public void setProfitRate(double currentPrice) {
        double changedPrice = currentPrice - getToBuyPrice();
        double changedRate = changedPrice / getToBuyPrice();

        if (profitRate == 0) {
            profitRate = changedRate;
        } else {
            profitRate = Math.max(profitRate, changedRate);
        }
    }

    public double getMaxProfitRate() {
        return profitRate;
    }

    public void setMarketId(String marketId) {
        this.marketId = marketId;
    }

    public String getMarketId() {
        return marketId;
    }

    private double convertPrice(double price) {
        DecimalFormat mFormatUnder10 = new DecimalFormat("#.##");
        DecimalFormat mFormatUnder100 = new DecimalFormat("##.#");
        DecimalFormat mFormatUnder1_000 = new DecimalFormat("###");
        DecimalFormat mFormatUnder10_000 = new DecimalFormat("####");
        DecimalFormat mFormatUnder100_000 = new DecimalFormat("#####");
        DecimalFormat mFormatUnder1_000_000 = new DecimalFormat("######");
        DecimalFormat mFormatUnder10_000_000 = new DecimalFormat("#######");
        DecimalFormat mFormatUnder100_000_000 = new DecimalFormat("########");

        String result = null;
        double priceResult = 0;
        if (price < 10) {
            priceResult = Math.floor(price * 100) / 100;
            result = mFormatUnder10.format(priceResult);
        } else if (price < 100) {
            priceResult = Math.floor(price * 10) / 10;
            result = mFormatUnder100.format(priceResult);
        } else if (price < 1000) {
            priceResult = Math.floor(price);
            result = mFormatUnder1_000.format(priceResult);
        } else if (price < 10000) {
            // 5
            double extra = Math.round(((price % 10) * 2) / 10 ) / 2 * 10;
            priceResult = Math.floor(price / 10) * 10 + extra;
            result = mFormatUnder10_000.format(priceResult);
        } else if (price < 100000) {
            // 10
            double extra = Math.round(((price % 10)) / 10 ) * 10;
            priceResult = Math.floor(price / 100) * 100 + extra;
            result = mFormatUnder100_000.format(priceResult);
        } else if (price < 1000000) {
            // 50, 100
            double extra = 0;
            if (price < 500000) {
                extra = Math.round(((price % 100) * 2) / 100) / 2 * 100;
            } else {
                extra = Math.round(((price % 100)) / 100) * 100;
            }
            priceResult = Math.floor(price / 1000) * 1000 + extra;
            result = mFormatUnder1_000_000.format(priceResult) + extra;
        } else if (price < 10000000) {
            // 1000
            double extra = Math.round(((price % 1000)) / 1000) * 1000;
            priceResult = Math.floor(price / 10000) * 10000 + extra;
            result = mFormatUnder10_000_000.format(priceResult) + extra;
        } else if (price < 100000000) {
            // 1000
            double extra = Math.round(((price % 1000)) / 1000) * 1000;
            priceResult = Math.floor(price / 10000) * 10000 + extra;
            result = mFormatUnder100_000_000.format(priceResult) + extra;
        }
        return Double.parseDouble(result);
    }
}
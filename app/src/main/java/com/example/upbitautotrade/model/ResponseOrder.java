package com.example.upbitautotrade.model;

import com.google.gson.annotations.SerializedName;

public class ResponseOrder {

    @SerializedName("uuid")
    String	uuid;

    @SerializedName("side")
    String	side;

    @SerializedName("ord_type")
    String	orderType;

    @SerializedName("price")
    Number	price;

    @SerializedName("avg_price")
    Number	avgPrice;

    @SerializedName("state")
    String	state;

    @SerializedName("market")
    String	market;

    @SerializedName("created_at")
    String	created_at;

    @SerializedName("volume")
    Number	volume;

    @SerializedName("remaining_volume")
    Number	remainingVolume;

    @SerializedName("reserved_fee")
    Number	reservedFee;

    @SerializedName("remaining_fee")
    Number remainingFee;

    @SerializedName("paid_fee")
    Number	paid_fee;

    @SerializedName("locked")
    Number	locked;

    @SerializedName("executed_volume")
    Number	executedVolume;

    @SerializedName("trades_count")
    Integer	tradesCount;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getSide() {
        return side;
    }

    public String getOrderType() {
        return orderType;
    }

    public Number getPrice() {
        return price;
    }

    public Number getAvgPrice() {
        return avgPrice;
    }

    public String getState() {
        return state;
    }

    public String getMarket() {
        return market;
    }

    public String getCreated_at() {
        return created_at;
    }

    public Number getVolume() {
        return volume;
    }

    public Number getRemainingVolume() {
        return remainingVolume;
    }

    public Number getReservedFee() {
        return reservedFee;
    }

    public Number getRemainingFee() {
        return remainingFee;
    }

    public Number getPaid_fee() {
        return paid_fee;
    }

    public Number getLocked() {
        return locked;
    }

    public Number getExecutedVolume() {
        return executedVolume;
    }

    public Integer getTradesCount() {
        return tradesCount;
    }

    public void setVolume(Number volume) {
        this.volume = volume;
    }

    public void setRemainingVolume(Number remainingVolume) {
        this.remainingVolume = remainingVolume;
    }

    public void setLocked(Number locked) {
        this.locked = locked;
    }

    public void setExecutedVolume(Number executedVolume) {
        this.executedVolume = executedVolume;
    }

    public void setTradesCount(Integer tradesCount) {
        this.tradesCount = tradesCount;
    }
}

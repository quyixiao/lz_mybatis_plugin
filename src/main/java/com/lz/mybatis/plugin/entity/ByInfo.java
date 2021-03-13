package com.lz.mybatis.plugin.entity;


import com.lz.mybatis.plugin.annotations.OrderType;

public class ByInfo {
    private String by;
    private OrderType orderType;

    public ByInfo() {
    }

    public ByInfo(String by, OrderType orderType) {
        this.by = by;
        this.orderType = orderType;
    }

    public String getBy() {
        return by;
    }

    public void setBy(String by) {
        this.by = by;
    }

    public OrderType getOrderType() {
        return orderType;
    }

    public void setOrderType(OrderType orderType) {
        this.orderType = orderType;
    }
}


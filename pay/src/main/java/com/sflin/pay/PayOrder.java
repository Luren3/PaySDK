package com.sflin.pay;

public class PayOrder {

    //价格
    private String price;

    //商品描述
    private String body;

    //商品详情
    private String orderInfo;

    //商品id
    private String orderId;

    //随机字符串
    private String nonce;

    //微信支付所需
    private String prepayId;

    public String getPrice() {
        return price == null ? "":price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getBody() {
        return body == null ? "":body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getOrderInfo() {
        return orderInfo == null ? "":orderInfo;
    }

    public void setOrderInfo(String orderInfo) {
        this.orderInfo = orderInfo;
    }

    public String getOrderId() {
        return orderId == null ? "":orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getNonce() {
        return nonce == null ? "":nonce;
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
    }

    public String getPrepayId() {
        return prepayId == null ? "":prepayId;
    }

    public void setPrepayId(String prepayId) {
        this.prepayId = prepayId;
    }
}

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

    //微信支付所需(服务端统一下单返回，相当于APP端不用调用生成)
    private String WXPrepayId;

    //支付宝支付所需(服务端统一下单返回，相当于APP端不用调用生成)
    private String AliPayInfo;

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

    public String getWXPrepayId() {
        return WXPrepayId == null ? "":WXPrepayId;
    }

    public void setWXPrepayId(String WXPrepayId) {
        this.WXPrepayId = WXPrepayId;
    }

    public String getAliPayInfo() {
        return AliPayInfo == null ? "":AliPayInfo;
    }

    public void setAliPayInfo(String aliPayInfo) {
        AliPayInfo = aliPayInfo;
    }

    @Override
    public String toString() {
        return "PayOrder{" +
                "price='" + price + '\'' +
                ", body='" + body + '\'' +
                ", orderInfo='" + orderInfo + '\'' +
                ", orderId='" + orderId + '\'' +
                ", nonce='" + nonce + '\'' +
                ", WXPrepayId='" + WXPrepayId + '\'' +
                ", AliPayInfo='" + AliPayInfo + '\'' +
                '}';
    }
}

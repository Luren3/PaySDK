package com.sflin.pay;

public class PaySDK{

    public static <T extends Pay> T createPayAPI(Class<T> paramClass) {
        if (AliPay.class.isAssignableFrom(paramClass)) {
            return (T) new AliPay();
        }
        if (WXPay.class.isAssignableFrom(paramClass)) {
            return (T) WXPay.getInstance();
        }
        return null;
    }
}

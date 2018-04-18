package com.sflin.pay;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.alipay.sdk.app.PayTask;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AliPay extends Pay {

    private Activity context;

    private Map<String, Object> payParamMap;

    private PayOrder payOrder;

    private PayTask payTask;

    private OnPayListener payListener;

    @Override
    public void pay(Context context, PayOrder payOrder, OnPayListener payListener) {
        if (!(context instanceof Activity)){
            Log.e("pay_error","context is not instanceof Activity");
            return;
        }
        this.context = (Activity) context;
        this.payOrder = payOrder;
        this.payListener = payListener;
        stepOne();
    }

    @SuppressWarnings("unchecked")
    private void stepOne(){
        String json = PayUtils.getJson(context, "pay.json");
        Gson gson = new Gson();
        Map<String, Object> map = gson.fromJson(json, new TypeToken<Map<String, Object>>() {
        }.getType());
        payParamMap = (Map<String, Object>) map.get("AliPay");

        payTask = new PayTask(context);

        stepTwo();
    }

    private void stepTwo(){

        String orderInfo = "";
        if (payOrder.getAliPayInfo().length() == 0){
            LinkedHashMap<String, String> params = buildOrderParamMap();
            String orderParam = buildOrderParam(params);
            String sign = getSign(params);
            orderInfo = orderParam + "&" + sign;
        }else {
            orderInfo = payOrder.getAliPayInfo();
        }

        final String finalOrderInfo = orderInfo;
        new Thread(new Runnable() {
            @Override
            public void run() {
                Map<String, String> result = payTask.payV2(finalOrderInfo, true);

                Message msg = new Message();
                msg.what = 1;
                msg.obj = result;
                mHandler.sendMessage(msg);
            }
        }).start();
    }

    @SuppressLint("HandlerLeak")
    @SuppressWarnings("unchecked")
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1: {
                    Map<String, String> payResultMap = (Map<String, String>) msg.obj;
                    String resultInfo = payResultMap.get("result").toString();//同步返回需要验证的信息
                    String resultStatus = payResultMap.get("resultStatus").toString();
                    String memo = payResultMap.get("memo").toString();
                    switch (resultStatus){
                        case "8000":
                            payListener.onResult(PayResult.PAY_LOADING,"支付确认中");
                            break;
                        case "9000":
                            payListener.onResult(PayResult.PAY_SUCCESS,"支付成功");
                            break;
                        default:
                            payListener.onResult(PayResult.PAY_ERROR,"支付失败");
                            break;
                    }
                    break;
                }
                default:
                    break;
            }
        }
    };

    /**
     * 对支付参数信息进行签名
     *
     * @param map 待签名授权信息
     */
    private String getSign(Map<String, String> map) {
        List<String> keys = new ArrayList<String>(map.keySet());
        // key排序
        Collections.sort(keys);

        StringBuilder authInfo = new StringBuilder();
        for (int i = 0; i < keys.size() - 1; i++) {
            String key = keys.get(i);
            String value = map.get(key);
            authInfo.append(buildKeyValue(key, value, false));
            authInfo.append("&");
        }

        String tailKey = keys.get(keys.size() - 1);
        String tailValue = map.get(tailKey);
        authInfo.append(buildKeyValue(tailKey, tailValue, false));

        String oriSign = PayUtils.sign(authInfo.toString(), payParamMap.get("RSA_PRIVATE").toString(),true);
        String encodedSign = "";

        try {
            encodedSign = URLEncoder.encode(oriSign, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "sign=" + encodedSign;
    }

    /**
     * 构造支付订单参数列表
     */
    private LinkedHashMap<String, String> buildOrderParamMap() {
        LinkedHashMap<String, String> keyValues = new LinkedHashMap<String, String>();

        keyValues.put("app_id", payParamMap.get("app_id").toString());

        keyValues.put("biz_content", "{\"timeout_express\":\"30m\",\"product_code\":\"QUICK_MSECURITY_PAY\",\"total_amount\":\"" +
                payOrder.getPrice() + "\",\"subject\":\"" + (payOrder.getOrderInfo().length() == 0 ? payOrder.getBody():payOrder.getOrderInfo()) +
                "\",\"body\":\"" + payOrder.getBody() + "\",\"out_trade_no\":\"" + payOrder.getOrderId() + "\"}");

        keyValues.put("charset", "utf-8");

        keyValues.put("method", "alipay.trade.app.pay");

        keyValues.put("notify_url", payParamMap.get("notify_url").toString());

        keyValues.put("sign_type", payParamMap.get("sign_type").toString());

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        String timestamp = format.format(date);
        keyValues.put("timestamp", timestamp);
        String version = payTask.getVersion();
        keyValues.put("version", version);

        return keyValues;
    }

    /**
     * 拼接键值对
     */
    private String buildKeyValue(String key, String value, boolean isEncode) {
        StringBuilder sb = new StringBuilder();
        sb.append(key);
        sb.append("=");
        if (isEncode) {
            try {
                sb.append(URLEncoder.encode(value, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                sb.append(value);
            }
        } else {
            sb.append(value);
        }
        return sb.toString();
    }

    /**
     * 构造支付订单参数信息
     *
     * @param map 支付订单参数
     */
    private String buildOrderParam(Map<String, String> map) {
        List<String> keys = new ArrayList<String>(map.keySet());

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < keys.size() - 1; i++) {
            String key = keys.get(i);
            String value = map.get(key);
            sb.append(buildKeyValue(key, value, true));
            sb.append("&");
        }

        String tailKey = keys.get(keys.size() - 1);
        String tailValue = map.get(tailKey);
        sb.append(buildKeyValue(tailKey, tailValue, true));

        return sb.toString();
    }
}

package com.sflin.pay;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.Xml;
import android.widget.Toast;

import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.modelpay.PayReq;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import org.xmlpull.v1.XmlPullParser;

import java.io.StringReader;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class WXPay extends Pay {

    private static WXPay instance = null;

    private Context context;

    private PayReq payReq;

    private IWXAPI mIWXAPI;

    private Map<String, Object> payParamMap;

    private PayOrder payOrder;

    private OnPayListener payListener;

    private WXPay(){

    }

    public static WXPay getInstance(){
        if(instance == null){
            synchronized (WXPay.class){
                if(instance == null){
                    instance = new WXPay();
                }
            }
        }
        return instance;
    }

    @Override
    public void pay(Context context, PayOrder payOrder, OnPayListener payListener) {
        this.context = context;
        this.payOrder = payOrder;
        this.payListener = payListener;
        stepOne();
    }

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    stepTwo();
                    break;
                case 1:
                    Toast.makeText(context, msg.obj.toString(), Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    //第一步--初始化
    @SuppressWarnings("unchecked")
    private void stepOne() {
        payReq = new PayReq();
        mIWXAPI = WXAPIFactory.createWXAPI(context, null);

        String json = PayUtils.getJson(context, "pay.json");
        Gson gson = new Gson();
        Map<String, Object> map = gson.fromJson(json, new TypeToken<Map<String, Object>>() {
        }.getType());
        payParamMap = (Map<String, Object>) map.get("WXPay");

        mIWXAPI.registerApp(payParamMap.get("app_id").toString());

        if (payOrder.getWXPrepayId().length() == 0) {
            createWXOrder();
        } else {
            stepTwo();
        }
    }

    //第二步--发起支付
    private void stepTwo() {
        payReq.appId = payParamMap.get("app_id").toString();
        payReq.partnerId = payParamMap.get("mch_id").toString();
        payReq.prepayId = payOrder.getWXPrepayId();
        payReq.packageValue = "Sign=WXPay";
        payReq.nonceStr = payOrder.getNonce().length() == 0 ? payOrder.getBody():payOrder.getNonce();
        payReq.timeStamp = String.valueOf(System.currentTimeMillis() / 1000);

        LinkedHashMap<String,String> params = new LinkedHashMap<>();
        params.put("appid", payReq.appId);
        params.put("noncestr", payReq.nonceStr);
        params.put("package", payReq.packageValue);
        params.put("partnerid", payReq.partnerId);
        params.put("prepayid", payReq.prepayId);
        params.put("timestamp", payReq.timeStamp);

        payReq.sign = genSign(params);

        mIWXAPI.sendReq(payReq);
    }

    //生成微信支付订单
    private void createWXOrder() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpRequest request = HttpRequest.post("https://api.mch.weixin.qq.com/pay/unifiedorder");
                request.part("status[body]", getWXOrderParam());
                if (request.ok()) {
                    String result = request.body();
                    Map<String, String> data = decodeXml(result);

                    Log.e("dsdsd",data.toString());

                    if (data.get("return_code").equals("SUCCESS")) {
                        payOrder.setWXPrepayId(data.get("prepay_id"));
                        mHandler.sendEmptyMessage(0);
                    }
                    if (data.get("return_code").equals("FAIL")) {
                        String return_msg = data.get("return_msg");
                        Message msg = new Message();
                        msg.what = 1;
                        msg.obj = return_msg;
                        mHandler.sendMessage(msg);
                    }
                }
            }
        }).start();
    }

    //微信支付订单所需参数
    private String getWXOrderParam() {

        LinkedHashMap<String, String> params = new LinkedHashMap<>();
        params.put("appid", payParamMap.get("app_id").toString());
        params.put("body", payOrder.getBody());
        params.put("mch_id", payParamMap.get("mch_id").toString());
        params.put("nonce_str", payOrder.getNonce().length() == 0 ? payOrder.getBody():payOrder.getNonce());
        params.put("notify_url", payParamMap.get("notify_url").toString());
        params.put("out_trade_no", "sflin"+payOrder.getOrderId());
        params.put("spbill_create_ip", PayUtils.getLocalIpAddress());
        params.put("total_fee", Double.valueOf(Double.parseDouble(payOrder.getPrice()) * 100).intValue() + "");
        params.put("trade_type", "APP");

        String sign = genSign(params);
        params.put("sign", sign);

        String xmlstring = toXml(params);

        Log.e("dsds",xmlstring);

        return xmlstring;
    }

    //签名
    private String genSign(LinkedHashMap<String, String> params) {
        StringBuilder sb = new StringBuilder();
        for (LinkedHashMap.Entry<String, String> entry : params.entrySet()) {
            sb.append(entry.getKey());
            sb.append('=');
            sb.append(entry.getValue());
            sb.append('&');
        }
        sb.append("key=");
        sb.append(payParamMap.get("app_key").toString());

        String appSign = PayUtils.MD5(sb.toString().getBytes())
                .toUpperCase();
        return appSign;
    }

    //拼接字符串为Xml
    private String toXml(LinkedHashMap<String, String> params) {
        StringBuilder sb = new StringBuilder();
        sb.append("<xml>");

        for (LinkedHashMap.Entry<String, String> entry : params.entrySet()) {
            sb.append("<" + entry.getKey() + ">");

            sb.append(entry.getValue());
            sb.append("</" + entry.getKey() + ">");
        }
        sb.append("</xml>");
        return sb.toString();
    }

    //将Xml转换为Map
    private Map<String, String> decodeXml(String content) {
        try {
            Map<String, String> xml = new HashMap<>();
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(new StringReader(content));
            int event = parser.getEventType();
            while (event != XmlPullParser.END_DOCUMENT) {

                String nodeName = parser.getName();
                switch (event) {
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    case XmlPullParser.START_TAG:
                        if ("xml".equals(nodeName) == false) {
                            // 实例化student对象
                            xml.put(nodeName, parser.nextText());
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        break;
                }
                event = parser.next();
            }
            return xml;
        } catch (Exception e) {
            Log.e("decodeXmlError", e.toString());
        }
        return null;
    }

    //WXPayEntryActivity回调结果
    public void setBaseResp(BaseResp baseResp){
        if (payListener != null){
            switch (baseResp.errCode){
                case -1://支付失败
                    payListener.onResult(PayResult.PAY_ERROR,"支付失败");
                    break;
                case -2://取消支付
                    payListener.onResult(PayResult.PAY_CANCEL,"取消支付");
                    break;
                case 0://支付成功
                    payListener.onResult(PayResult.PAY_SUCCESS,"支付成功");
                    break;
                default://支付失败
                    payListener.onResult(PayResult.PAY_ERROR,"支付失败");
                    break;
            }
        }
    }
}

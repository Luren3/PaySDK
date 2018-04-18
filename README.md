# PaySDK
整合微信支付和支付宝支付

## 使用方法

1.添加gradle依赖

	compile 'com.sflin:pay:0.2.0'
	
2.在项目assets目录下创建pay.json文件

	{
	    "AliPay":{
	        "app_id":"支付宝APPID",
	        "RSA_PRIVATE":"应用私钥",
	        "sign_type":"签名类型:如RSA2",
	        "notify_url":"回调地址"
	    },
	    "WXPay":{
	        "app_id":"微信APPID",
	        "mch_id":"商户号",
	        "app_key":"APPKey",
	        "notify_url":"回调地址"
	    }
	}
	
3.使用如下：

	PayOrder payOrder = new PayOrder();
	payOrder.setPrice("0.01");//价格
	payOrder.setBody("内容");//具体内容，如：充值
	payOrder.setOrderId("123456");//订单号
	//如果支付宝和微信都是服务端统一支付下单，需要如下参数
	//微信服务端返回
	payOrder.setWXPrepayId("prepayid");//从返回数据取
	//支付宝服务端返回
	payOrder.setAliPayInfo("info");//从返回数据取
	
	//微信
	WXPay pay = PaySDK.createPayAPI(WXPay.class);
	
	//支付宝
	AliPay pay = PaySDK.createPayAPI(AliPay.class);
	
	pay.pay(this, payOrder2, new OnPayListener() {
        @Override
        public void onResult(PayResult result,String message) {
            if (result == PayResult.PAY_SUCCESS){...}
        }
    });
    
    //Payresult分为4个返回结果
    //PAY_SUCCESS(成功),PAY_ERROR(失败),
    //PAY_CANCEL(取消),PAY_LOADING(支付确认中)
    
    关于微信支付在根目录创建wxapi文件夹创建WXPayEntryActivity(根据官方demo来)
    在WXPayEntryActivity的onResp添加如下代码
    @Override
    public void onResp(BaseResp baseResp) {
        if (baseResp.getType() == ConstantsAPI.COMMAND_PAY_BY_WX) {
            WXPay.getInstance().setBaseResp(baseResp);
            finish();
        }

    }
    
   
    
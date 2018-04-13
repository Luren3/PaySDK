package com.sflin.pay;

import android.content.Context;

public interface IPay {
    public abstract void pay(Context context, PayOrder payOrder, OnPayListener payListener);
}

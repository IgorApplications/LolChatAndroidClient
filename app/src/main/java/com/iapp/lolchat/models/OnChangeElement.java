package com.iapp.lolchat.models;

import android.view.View;

public interface OnChangeElement<T> {

    void onChange(View view, T t);
}

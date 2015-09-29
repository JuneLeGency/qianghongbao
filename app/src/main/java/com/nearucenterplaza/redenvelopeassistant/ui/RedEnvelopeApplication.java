package com.nearucenterplaza.redenvelopeassistant.ui;

import android.app.Application;

import com.orm.SugarApp;

public class RedEnvelopeApplication extends SugarApp {
	private static RedEnvelopeApplication mInstance;
	
	public static Application getInstance(){
		return mInstance;
	}
	
	@Override
	public void onCreate(){
		super.onCreate();
		mInstance=this;
	}
	
	public void onTerminate(){
		super.onTerminate();
		mInstance=null;
	}
}

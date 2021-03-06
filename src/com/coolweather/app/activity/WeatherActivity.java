package com.coolweather.app.activity;

import com.coolweather.app.R;
import com.coolweather.app.util.HttpCallbackListener;
import com.coolweather.app.util.HttpUtil;
import com.coolweather.app.util.Utility;


import android.R.layout;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class WeatherActivity extends Activity {
	
	private LinearLayout weatherInfoLayout;
	private TextView cityNameText;
	private TextView publishText;
	private TextView weatherDespText;
	private TextView temp1Text;
	private TextView temp2Text;
	private TextView currentDateText;
	
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.weather_layout);
		//初始化各个控件
		weatherInfoLayout = (LinearLayout) findViewById(R.id.weather_info_layout);
		cityNameText = (TextView) findViewById(R.id.city_name);
		publishText = (TextView) findViewById(R.id.publish_text);
		weatherDespText = (TextView) findViewById(R.id.weather_desp);
		temp1Text = (TextView) findViewById(R.id.temp1);
		temp2Text = (TextView) findViewById(R.id.temp2);
		currentDateText = (TextView) findViewById(R.id.current_date);
		String countyCode = getIntent().getStringExtra("county_code");
		if(!TextUtils.isEmpty(countyCode)){
			//有县级代号时就去查询天气
			publishText.setText("信息同步中...");
			weatherInfoLayout.setVisibility(View.INVISIBLE);
			cityNameText.setVisibility(View.INVISIBLE);
			
			queryWeatherCode(countyCode);
		}else {
			//没有县级代号就显示本地天气
			showWeather();
		}
		
	}

	
	
	
	private void queryWeatherCode(String countyCode) {
		// 查询县级代号所对应的天气代号
		String address = "http://www.weather.com.cn/data/list3/city" + countyCode + ".xml";
		queryFromServer(address,"countyCode");
		
	}
	
	private void queryWeatherInfo(String weatherCode){
		String address = "http://www.weather.com.cn/data/cityinfo" + weatherCode + ".html";
		queryFromServer(address,"weatherCode");
	}




	private void queryFromServer(final String address, final String type) {
		// 根据传入的地址和类型去向服务器查询天气代号或者天气信息。
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
			
			@Override
			public void onFinish(final String response) {  
				
				if("countyCode".equals(type)){
					if(!TextUtils.isEmpty(response)){
						//从服务器返回的数据中解析出天气代号
						String array[] = response.split("\\|");
						if(array != null && array.length ==2 ){
							String weatherCode =array[1];
							queryWeatherInfo(weatherCode);
						}
					}
				}else if ("weatherCode".equals(type)){
					//处理服务器返回的数据信息。
					Utility.handleWeatherResponse(WeatherActivity.this, response);
					runOnUiThread(new Runnable() {
						
						@Override	
						public void run() {
							showWeather();
							
						}

					});
				}
			}
		
			@Override
			public void onError(Exception e) {
				//通过runOnUiThread()方法回到主线程处理逻辑。
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						publishText.setText("同步失败");
					}
				});
			}	
		});
		
	}
	
	private void showWeather() {
		// 从SharedPreferences文件中读取存储的天气信息，并显示到界面上。
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);	
		cityNameText.setText(prefs.getString("city_name", ""));	
		temp1Text.setText(prefs.getString("temp1", "")); 
		temp2Text.setText(prefs.getString("temp2", ""));  
		weatherDespText.setText(prefs.getString("weather_desp", ""));  
		publishText.setText("今天" + prefs.getString("publish_time", "") + "发布");
		currentDateText.setText(prefs.getString("current_date", "")); 
		weatherInfoLayout.setVisibility(View.VISIBLE);
		cityNameText.setVisibility(View.VISIBLE);
		
	}
}

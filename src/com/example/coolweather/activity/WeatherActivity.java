package com.example.coolweather.activity;

import com.example.coolweather.service.AutoUpdateService;
import com.example.coolweather.util.HttpCallbackListener;
import com.example.coolweather.util.HttpUtil;
import com.example.coolweather.util.Utility;

import android.R;
import android.R.string;
import android.app.Activity;
import android.app.DownloadManager.Query;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class WeatherActivity extends Activity implements OnClickListener{

	private LinearLayout weatherInfoLayout;
	/**
	* 用于显示城市名*/
	private TextView cityNameText;
	/**
	* 用于显示发布时间
	*/
	private TextView publishText;
	/**
	* 用于显示天气描述信息
	*/
	private TextView weatherDespText;
	/**
	* 用于显示气温1
	*/
	private TextView temp1Text;
	/**
	* 用于显示气温2
	*/
	private TextView temp2Text;
	/**
	* 用于显示当前日期
	*/
	private TextView currentDateText;
	/**
	* 切换城市按钮
	*/
	private Button switchCity;
	/**
	* 更新天气按钮
	*/
	private Button refreshWeather;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(com.example.coolweather.R.layout.weather_layout);
		
		weatherInfoLayout=(LinearLayout)findViewById(com.example.coolweather.R.id.weather_info_layout);
		cityNameText=(TextView)findViewById(com.example.coolweather.R.id.city_name);
		publishText=(TextView)findViewById(com.example.coolweather.R.id.publish_text);
		weatherDespText=(TextView)findViewById(com.example.coolweather.R.id.weather_desp);
		temp1Text=(TextView)findViewById(com.example.coolweather.R.id.temp1);
		temp2Text = (TextView) findViewById(com.example.coolweather.R.id.temp2);
		currentDateText = (TextView) findViewById(com.example.coolweather.R.id.current_date);
		switchCity = (Button) findViewById(com.example.coolweather.R.id.switch_city);
		refreshWeather = (Button) findViewById(com.example.coolweather.R.id.refresh_weather);
		switchCity.setOnClickListener(this);
		refreshWeather.setOnClickListener(this);
		String countyCode=getIntent().getStringExtra("county_code");//从chooseAreaActivity传过来的值
		
		if (!TextUtils.isEmpty(countyCode)) {
			publishText.setText("同步中ing。。。");
			weatherInfoLayout.setVisibility(View.INVISIBLE);
			cityNameText.setVisibility(View.INVISIBLE);
			queryWeatherCode(countyCode);
		}else {
			//显示本地天气
			showWeather();
		}
		
		
	}
	


	private void showWeather() {
		// TODO Auto-generated method stub
		SharedPreferences prefs=PreferenceManager.getDefaultSharedPreferences(this);
		cityNameText.setText( prefs.getString("city_name", ""));
		temp1Text.setText(prefs.getString("temp1", ""));
		temp2Text.setText(prefs.getString("temp2", ""));
		weatherDespText.setText(prefs.getString("weather_desp", ""));
		publishText.setText("今天" + prefs.getString("publish_time", "") + "发布");
		currentDateText.setText(prefs.getString("current_date", ""));
		weatherInfoLayout.setVisibility(View.VISIBLE);
		cityNameText.setVisibility(View.VISIBLE);
		
		Intent intent=new Intent(WeatherActivity.this, AutoUpdateService.class);
		startService(intent);
	}

	private void queryWeatherCode(String countyCode) {
		// TODO Auto-generated method stub
		String address = "http://www.weather.com.cn/data/list3/city" + countyCode + ".xml";
		queryFromServer(address, "countyCode");
	}

	private void queryFromServer(final String address, final String type) {
		// TODO Auto-generated method stub
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
			
			@Override
			public void onFinish(String response) {
				// TODO Auto-generated method stub
				if ("countyCode".equals(type)) {
					if (!TextUtils.isEmpty(response)) {
						String[] array=response.split("\\|");
						if (array!=null&&array.length==2) {
							String weatherCode=array[1];
							queryWeatherInfo(weatherCode);
						}
					}
				}else if ("weatherCode".equals(type)) {
					Utility.handleWeatherResponse(WeatherActivity.this, response);
					runOnUiThread(new Runnable() {
						public void run() {
							showWeather();
						}
					});
				}
			}
			
			@Override
			public void onError(Exception e) {
				// TODO Auto-generated method stub
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						Toast.makeText(WeatherActivity.this, "同步失败", Toast.LENGTH_SHORT).show();
						publishText.setText("同步失败");
					}
				});
			}
		});
	}
	private void queryWeatherInfo(String weatherCode) {
		// TODO Auto-generated method stub
		String address = "http://www.weather.com.cn/data/cityinfo/" + weatherCode + ".html";
		queryFromServer(address, "weatherCode");
	}
	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		Intent intent=new Intent(WeatherActivity.this,ChooseAreaActivity.class);
		startActivity(intent);
		finish();
	}



	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case com.example.coolweather.R.id.switch_city:
			Intent intent = new Intent(WeatherActivity.this, ChooseAreaActivity.class);
			intent.putExtra("from_weather_activity", true);
			startActivity(intent);
			finish();
			break;
		case com.example.coolweather.R.id.refresh_weather:
			publishText.setText("同步中...");
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
			String weatherCode = prefs.getString("weather_code", "");
			if (!TextUtils.isEmpty(weatherCode)) {
			queryWeatherInfo(weatherCode);
			}
			break;
		default:
			break;
		}
	}
}


package com.example.coolweather.activity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.example.coolweather.db.CoolWeatherDB;
import com.example.coolweather.model.City;
import com.example.coolweather.model.County;
import com.example.coolweather.model.Province;
import com.example.coolweather.util.HttpCallbackListener;
import com.example.coolweather.util.HttpUtil;
import com.example.coolweather.util.Utility;


import android.R;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ChooseAreaActivity extends Activity{

	public static final int LEVEL_PROVINCE=0;
	public static final int LEVEL_CITY=1;
	public static final int LEVEL_COUNTY=2;
	
	private ProgressDialog progressDialog;
	private TextView titleText;
	private ListView listView;
	private ArrayAdapter<String>adapter;
	private CoolWeatherDB coolWeatherDB;
	
	private List<String>datalist=new ArrayList<String>();
	private List<Province> provinceList;
	private List<City>cityList;
	private List<County>countyList;
	
	private Province selectedProvince;
	private City selectedCity;
	private int currentLevel;
	private boolean isFromWeatherActivity;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		isFromWeatherActivity=getIntent().getBooleanExtra("from_weather_activity", false);
		SharedPreferences prefs=PreferenceManager.getDefaultSharedPreferences(this);
		if (prefs.getBoolean("city_selected", false)&&!isFromWeatherActivity) {
			Intent intent = new Intent(this, WeatherActivity.class);
			startActivity(intent);
			finish();
			return;
		}
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(com.example.coolweather.R.layout.choose_area);
		
		listView=(ListView)findViewById(com.example.coolweather.R.id.list_view);
		titleText=(TextView)findViewById(com.example.coolweather.R.id.title_text);
		adapter=new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, datalist);
		listView.setAdapter(adapter);
		coolWeatherDB=CoolWeatherDB.getInstance(this);
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				if (currentLevel==LEVEL_PROVINCE) {
					selectedProvince = provinceList.get(position);
					queryCities();
				}else if (currentLevel==LEVEL_CITY) {
					selectedCity = cityList.get(position);
					queryCounties();
				}else if (currentLevel==LEVEL_COUNTY) {
					String countyCode=countyList.get(position).getCountyCode();
					Intent intent=new Intent(ChooseAreaActivity.this, WeatherActivity.class);
					intent.putExtra("county_code", countyCode);
					startActivity(intent);
					finish();
				}
			}

			
		});
		
		queryProvinces();//打开apk后显示所有的省级
	}
	//查询所有省
	private void queryProvinces() {
		// TODO Auto-generated method stub
		provinceList=coolWeatherDB.loadProvinces();
		if (provinceList.size()>0) {
			datalist.clear();
			for (Province province:provinceList) {
				datalist.add(province.getProvinceName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText("中国");
			currentLevel=LEVEL_PROVINCE;
		}else {
			queryFromServer(null, "province");
		}
	}
	//查询省对应的所有市
	private void queryCities() {
		// TODO Auto-generated method stub
		cityList=coolWeatherDB.loadCities(selectedProvince.getId());
		if (cityList.size()>0) {
			datalist.clear();
			for (City city:cityList) {
				datalist.add(city.getCityName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText(selectedProvince.getProvinceName());
			currentLevel=LEVEL_CITY;
		}else {
			queryFromServer(selectedProvince.getProvinceCode(), "city");
		}
	}
	
	public void queryCounties() {
		// TODO Auto-generated method stub
		countyList=coolWeatherDB.loadCounties(selectedCity.getId());
		if (countyList.size()>0) {
			datalist.clear();
			for(County county:countyList)
			{
				datalist.add(county.getCountyName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText(selectedCity.getCityName());
			currentLevel=LEVEL_COUNTY;
		}else {
			queryFromServer(selectedCity.getCityCode(), "county");
		}
	}


	
	private void queryFromServer(final String code, final String type) {
		// TODO Auto-generated method stub
		String address;
		if (!TextUtils.isEmpty(code)) {
			address="http://www.weather.com.cn/data/list3/city" + code + ".xml";
		}else {
			address="http://www.weather.com.cn/data/list3/city.xml";
		}
		//showProgressDialog();
		Log.d("start loading", "start loading"+"--address="+address);
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
			@Override
			public void onFinish(String response) {
			boolean result = false;
			if ("province".equals(type)) {
			result = Utility.handleProvinceResponse(coolWeatherDB,response);
			} else if ("city".equals(type)) {
			result = Utility.handleCitiesResponse(coolWeatherDB,
			response, selectedProvince.getId());
			} else if ("county".equals(type)) {
			result = Utility.handleCountiesResponse(coolWeatherDB,
			response, selectedCity.getId());
			}
			if (result) {
				// 通过runOnUiThread()方法回到主线程处理逻辑
				runOnUiThread(new Runnable() {
				@Override
				public void run() {
				closeProgressDialog();
				if ("province".equals(type)) {
				queryProvinces();
				} else if ("city".equals(type)) {
				queryCities();
				} else if ("county".equals(type)) {
				queryCounties();
				}
				}
				});
				}
			}
			@Override
			public void onError(Exception e) {
				// TODO Auto-generated method stub
				runOnUiThread(new Runnable() {
					public void run() {
					closeProgressDialog();
					Toast.makeText(ChooseAreaActivity.this,
					"加载失败", Toast.LENGTH_SHORT).show();
					}
				});
			}
		});
	}
	private void showProgressDialog() {
		// TODO Auto-generated method stub
		if (progressDialog == null) {
			progressDialog = new ProgressDialog(this);
			progressDialog.setMessage("正在加载...");
			progressDialog.setCanceledOnTouchOutside(false);
			}
		progressDialog.show();
	}
	private void closeProgressDialog() {
		// TODO Auto-generated method stub
		if (progressDialog!=null) {
			progressDialog.dismiss();
		}
	}
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		Log.d("back pressed", "back pressed currentLevel="+currentLevel);
		if (currentLevel==LEVEL_COUNTY) {
			queryCities();
		}
		else if (currentLevel==LEVEL_CITY) {
			queryProvinces();
		}else {
			if (isFromWeatherActivity) {
				Intent intent = new Intent(this, WeatherActivity.class);
				startActivity(intent);
				}
			finish();
		}
	}	
}
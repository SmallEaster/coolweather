package com.example.coolweather.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import android.util.Log;


public class HttpUtil {

	public static void sendHttpRequest(final String address,final HttpCallbackListener listener)
	{
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				
			HttpURLConnection connection=null;
			
			
			try {
				URL url=new URL(address);
				connection=(HttpURLConnection)url.openConnection();
				connection.setRequestMethod("GET");
				//connection.setReadTimeout(8000);
				//connection.setConnectTimeout(8000);
				Log.d("url", "url");
				InputStream in=connection.getInputStream();
				Log.d("InputStream", "InputStream");
				BufferedReader reader=new BufferedReader(new InputStreamReader(in));
				Log.d("BufferedReader", "BufferedReader");
				StringBuilder response = new StringBuilder();
				String line;
				while ((line=reader.readLine())!=null) {
					response.append(line);
				}
				Log.d("network data", response.toString());
				if (listener != null) {
					listener.onFinish(response.toString());
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				if (listener!=null) {
					listener.onError(e);
				}
			}finally
			{
				if (connection!=null) {
					connection.disconnect();
				}
			}
			}
		}).start();
	}
}

package com.example.coolweather.receiver;

import com.example.coolweather.service.AutoUpdateService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class AutoUpdateReceiver extends BroadcastReceiver{

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		Toast.makeText(context, "updating", Toast.LENGTH_SHORT).show();
		Intent i=new Intent(context, AutoUpdateService.class);
		context.startService(i);
	}

}

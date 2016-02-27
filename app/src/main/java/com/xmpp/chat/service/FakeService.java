package com.xmpp.chat.service;

import com.example.xmppsample.R;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

public class FakeService extends Service {

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onDestroy() {
		stopForeground(true);
		super.onDestroy();
	}

	@Override
	public void onCreate() {
		super.onCreate();
		if (!LiveAppService.isMyServiceRunning(this)) {
		
			startService(new Intent(FakeService.this, LiveAppService.class));
		}
	}
}

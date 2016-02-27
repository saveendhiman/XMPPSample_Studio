package com.xmpp.chat.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {
	public void onReceive(Context paramContext, Intent paramIntent) {
		if (!LiveAppService.isMyServiceRunning(paramContext)) {
			paramContext.startService(new Intent(paramContext, FakeService.class));
		}
	}
}

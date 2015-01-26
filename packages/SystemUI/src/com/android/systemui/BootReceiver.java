/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.systemui;

import android.app.Notification;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.database.ContentObserver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.os.Vibrator;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;
import android.widget.TextView;


/**
 * Performs a number of miscellaneous, non-system-critical actions
 * after the system has finished booting.
 */
public class BootReceiver extends BroadcastReceiver {
	    
    private static final String TAG = "SystemUIBootReceiver";
	private static final String MaxKhz =  "Max Khz: ";
	private static final String MinKhz = "Min Khz: ";
	private static final String Gov = "Governor: ";
	private static final String Density = "LCD Density: ";
	private static final String Scheduler = "IO Scheduler: ";
	private static final String TCP = "TCP Congestion Control: ";
	private static final String Cores1 = "Cores 1/2 | ";
	private static final String Cores2 = "Cores 3/4 | ";
		
		
    @Override
    public void onReceive(final Context context, Intent intent) {
		ContentResolver res = context.getContentResolver();
		String mPlainTweakEnable = Settings.System.getString(res, Settings.System.PLAIN_TWEAK_ENABLE);
        SystemProperties.set("enable_plaintweak", mPlainTweakEnable);
        try {
            // Start the load average overlay, if activated
            if (Settings.Global.getInt(res, Settings.Global.SHOW_PROCESSES, 0) != 0) {
                Intent loadavg = new Intent(context, com.android.systemui.LoadAverageService.class);
                context.startService(loadavg);
            }
        } catch (Exception e) {
            Log.e(TAG, "Can't start load average service", e);
        }
        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())){
			int mPlainTweakNotify = Settings.System.getInt(res, Settings.System.PLAIN_TWEAK_NOTIFICATIONS, 0);
			if (Settings.System.getInt(res, Settings.System.PLAIN_TWEAK_NOTIFICATIONS, 0) != 0) {
						switch (mPlainTweakNotify) {
							case 0:
								break;
							case 1:
								ShowNotificationTheRest("scheduler", "customdensity", "tcpcong", context, 1, "mGroup3");
								CpuNotification(context, 2, "mGroup3");							        
								break;
							case 2:
								ShowToast(context, Cores1, "gov", "maxkhz", "minkhz" );
								ShowToast(context, Cores2, "gov2", "maxkhz2", "minkhz2" );
								ShowToast(context, "Additional Properties", "scheduler", "customdensity", "tcpcong" );
								break;
				}
			}
		}
    }
        
    public void ShowNotificationTheRest(String property1, String property2, String property3, Context context, int i, String group ) {
			Intent newintent = new Intent();
			newintent.setClassName("com.android.settings", "com.android.settings.Settings$PlainTweakInfo");
			PendingIntent contentIntent = PendingIntent.getActivity(context, 0, newintent, PendingIntent.FLAG_ONE_SHOT);
			Notification.Builder mBuilder = new Notification.Builder(context)
					.setSmallIcon(R.drawable.ic_settings_plaintweak)
                    .setAutoCancel(true)
                    .setContentTitle("Additional properties")
                    .setContentText("Additional properties")
					.setStyle(new Notification.InboxStyle()
					.setBigContentTitle("Plain-Tweak Custom Values")
					.addLine(Scheduler+SystemProperties.get(property1, "Unset"))
					.addLine(Density+SystemProperties.get(property2, "Unset"))
					.addLine(TCP+SystemProperties.get(property3, "Unset")))
					.setContentIntent(contentIntent);
            NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(i, mBuilder.build());
    }
    public void CpuNotification(Context context, int i, String group ) {
			Intent newintent = new Intent();
			newintent.setClassName("com.android.settings", "com.android.settings.Settings$PlainTweakInfo");
			PendingIntent contentIntent = PendingIntent.getActivity(context, 0, newintent, PendingIntent.FLAG_ONE_SHOT);
			Notification.Builder mBuilder = new Notification.Builder(context)
					.setSmallIcon(R.drawable.ic_settings_plaintweak)
                    .setAutoCancel(true)
                    .setContentTitle("Plain-Tweak")
                    .setContentText("Configuration properties")
					.setStyle(new Notification.InboxStyle()					
					.setBigContentTitle("Plain-Tweak Custom Values")
					.addLine(Cores1+Gov+SystemProperties.get("gov", "Unset"))
					.addLine(Cores1+MaxKhz+SystemProperties.get("maxkhz", "Unset"))
					.addLine(Cores1+MinKhz+SystemProperties.get("minkhz", "Unset"))
					.addLine(Cores2+Gov+SystemProperties.get("gov2", "Unset"))
					.addLine(Cores2+MaxKhz+SystemProperties.get("maxkhz2", "Unset"))
					.addLine(Cores2+MinKhz+SystemProperties.get("minkhz2", "Unset")))
					.setContentIntent(contentIntent);
            NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(i, mBuilder.build());
    }
    public void ShowToast(Context context, String title, String property1, String property2, String property3){
						Toast mShowToast;
						mShowToast = Toast.makeText(context, title+"\n"+SystemProperties.get(property1)+SystemProperties.get(property2)+SystemProperties.get(property3), 20000);
						mShowToast.show();
	}
}

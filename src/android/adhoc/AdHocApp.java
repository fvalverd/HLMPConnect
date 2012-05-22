/*
*  This file is part of Barnacle Wifi Tether
*  Copyright (C) 2010 by Szymon Jakubczak
*
*  This program is free software: you can redistribute it and/or modify
*  it under the terms of the GNU General Public License as published by
*  the Free Software Foundation, either version 3 of the License, or
*  (at your option) any later version.
*
*  This program is distributed in the hope that it will be useful,
*  but WITHOUT ANY WARRANTY; without even the implied warranty of
*  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*  GNU General Public License for more details.
*
*  You should have received a copy of the GNU General Public License
*  along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package android.adhoc;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import android.HLMPConnect.R;
import android.HLMPConnect.Tabs;


public class AdHocApp extends android.app.Application {
    final static String TAG = "AdHocApp";
    public static String app_name;
    
    public final static int NOTIFY_RUNNING = 0;
    public final static int NOTIFY_ERROR = 1;
    public final static int ERROR_ROOT = 1;
    public final static int ERROR_OTHER = 2;
    public final static int ERROR_SUPPLICANT = 3;
    public final static int ERROR_ASSETS = 4;
    
    private SharedPreferences prefs;
    private AdHocService adHocService = null;
    protected AdHocActivity adHocActivity = null;
    private WifiManager wifiManager;
    private boolean previousWifiState;
    private Toast toast;
    private NotificationManager notificationManager;
    private Notification notification;
    private Notification notificationError;
	
    
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, String.format(getString(R.string.creating), this.getClass().getSimpleName()));
        NativeHelper.setup(this, getString(R.string.missedAssetsFiles));
        
        app_name = getString(R.string.app_name);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        this.prefs = PreferenceManager.getDefaultSharedPreferences(this);

        this.notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        this.toast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
        
        this.notification = new Notification(R.drawable.barnacle, "", 0);
        this.notification.flags |= Notification.FLAG_ONGOING_EVENT;
        
        String notify_error = getString(R.string.notify_error);
        PendingIntent pi = PendingIntent.getActivity(this, 0, new Intent(this, Tabs.class), 0);
        this.notificationError = new Notification(R.drawable.barnacle_error, notify_error, 0);
        this.notificationError.setLatestEventInfo(this, app_name, notify_error, pi);
        this.notificationError.flags = Notification.FLAG_AUTO_CANCEL;

        this.wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        if (this.previousWifiState = this.wifiManager.isWifiEnabled()) {
        	this.wifiManager.setWifiEnabled(false);
        }
        Log.d(TAG, String.format(getString(R.string.created), this.getClass().getSimpleName()));
        
        if (!NativeHelper.unzipAssets(this)) {
        	Log.e(TAG, getString(R.string.unpackerr));
        	this.adHocFailed(ERROR_ASSETS);
        }
    }

    @Override
    public void onTerminate() {
    	Log.i("AdHocApp", "AdHocApp distroying...");
        if (this.adHocService != null) {
        	Log.e(TAG, getString(R.string.stopAppRunningService));
            this.stopAdHoc();
        }
        super.onTerminate();
        Log.i("AdHocApp", "AdHocApp distroying... OK!");
    }
    
    
    protected void pickUpNewIP() {
    	SharedPreferences.Editor e = prefs.edit();
    	String ipFormat = getString(R.string.ipFormat);
    	ipFormat = String.format(ipFormat, (int)(Math.random()*255), (int)(Math.random()*255));
    	e.putString(getString(R.string.lan_gw), ipFormat);
    	e.commit();
    	Log.i(TAG, "Generated IP: " + ipFormat);
	}
    
    public void setAdHocActivity(AdHocActivity adHocActivity) {
        this.adHocActivity = adHocActivity;
    }

    public void setAdHocService(AdHocService adHocService) {
        this.adHocService = adHocService;
    }
    
    public void setLanWext(boolean value) {
    	this.prefs.edit().putBoolean(this.getString(R.string.lan_wext), value).commit();
        this.updateToast(this.getString(R.string.lanWextTryAgain), true);
	}
    
    public void startAdHoc() {
    	this.notificationManager.cancel(NOTIFY_ERROR);
    	this.adHocActivity.showDialog(AdHocActivity.DLG_STARTING);
    	this.pickUpNewIP();
    	if (this.adHocService == null) {
            this.startService(new Intent(this, AdHocService.class));
        }
    }
    
	public void stopAdHoc() {
		this.adHocActivity.showDialog(AdHocActivity.DLG_STOPPING);
		if (this.adHocService != null) {
			this.stopService(new Intent(this, AdHocService.class));
        }
    }
    
    protected int getAdHocServiceState() {
    	return this.adHocService==null ? AdHocService.STATE_STOPPED : this.adHocService.getState();
    }

    public boolean isRunning() {
        return getAdHocServiceState() == AdHocService.STATE_RUNNING;
    }

    public void requestUpdateAdHocActivity() {
		this.adHocUpdated(this.getAdHocServiceState());
	}

    protected String getIPAdress() {
    	return prefs.getString(this.getString(R.string.lan_gw), "127.0.0.1");
    }
    
    
    // AdHocService Notifications
    
    public void adHocStarted() {
    	try {
    		this.adHocActivity.dismissDialog(AdHocActivity.DLG_STARTING);
    	} catch(Exception e) {}
    	
    	String notifyRunningFormat = this.getString(R.string.notify_running);
        String essid = prefs.getString(this.getString(R.string.lan_essid), "");
        String runningMessage = String.format(notifyRunningFormat, essid);
        
        this.notification.tickerText = runningMessage; 
        Intent ni = new Intent(this, Tabs.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, ni, 0);
        this.notification.setLatestEventInfo(this, app_name, runningMessage, pi);
        this.notificationManager.notify(NOTIFY_RUNNING, notification);
        this.adHocService.startForegroundCompat(NOTIFY_RUNNING, notification);
        
        Log.d(TAG, this.getString(R.string.adhocStarted));
    }

    public void adHocStopped() {
    	this.notificationManager.cancel(NOTIFY_RUNNING);
    	this.adHocService = null;
        this.adHocUpdated(AdHocService.STATE_STOPPED);
        Log.d(TAG, this.getString(R.string.adhocStopped));
        if (this.previousWifiState) {
        	this.wifiManager.setWifiEnabled(true);
        }
        try{
        	this.adHocActivity.dismissDialog(AdHocActivity.DLG_STOPPING);
	    } catch(Exception e) {}
    }

    public void adHocFailed(int error) {
    	try {
    		this.adHocActivity.dismissDialog(AdHocActivity.DLG_STARTING);
    	} catch(Exception e) {}
    	try{
        	this.adHocActivity.dismissDialog(AdHocActivity.DLG_STOPPING);
	    } catch(Exception e) {}
        if (this.adHocActivity != null) {
            if (error == ERROR_ROOT) {
            	this.adHocActivity.showDialog(AdHocActivity.DLG_ROOT);
            } else if (error == ERROR_SUPPLICANT) {
            	this.adHocActivity.showDialog(AdHocActivity.DLG_SUPPLICANT);
            } else if (error == ERROR_OTHER) {
            	this.adHocActivity.showDialog(AdHocActivity.DLG_ERROR);
            } else if (error == ERROR_ASSETS) {
            	this.adHocActivity.showDialog(AdHocActivity.DLG_ASSETS);
            }
        }
        if ((this.adHocActivity == null) || !this.adHocActivity.hasWindowFocus()) {
            Log.d(TAG, this.getString(R.string.notifyingError));
            this.notificationManager.notify(NOTIFY_ERROR, notificationError);
        }
    }

    public void adHocUpdated(int state) {
        if (this.adHocActivity != null) {
        	this.adHocActivity.updateContent(state);
        }
    }
    
    
    void updateToast(String msg, boolean islong) {
        toast.setText(msg);
        toast.setDuration(islong ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT);
        toast.show();
    }

}

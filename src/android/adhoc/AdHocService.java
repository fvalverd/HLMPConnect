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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Map;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.util.Log;

import android.HLMPConnect.R;


public class AdHocService extends android.app.Service {
    final static String TAG = "AdHocService";
    
    private static final int THREAD_OUTPUT	= 0;
	private static final int THREAD_ERROR	= 1;
    final static int MSG_OUTPUT     = 1;
    final static int MSG_ERROR      = 2;
    final static int MSG_EXCEPTION  = 3;
    final static int MSG_NETSCHANGE = 4;
    final static int MSG_START      = 5;
    final static int MSG_STOP       = 6;
    public final static int STATE_FAILED  = -1;
    public final static int STATE_STOPPED  = 0;
    public final static int STATE_STARTING = 1;
    public final static int STATE_RUNNING  = 2;
    
    private AdHocApp adHocApp;
    private int state = STATE_STOPPED;
    private Process process = null;
    private Thread[] threads = new Thread[2];
    private PowerManager.WakeLock wakeLock;
    private WifiManager wifiManager;
    private Method mStartForeground = null;
    public static AdHocService singleton = null;  // WARNING: this is not entirely safe
   
    
    private class OutputMonitor implements Runnable {
    	final private int bufferedSize = 8192;
        private final java.io.BufferedReader br;
        private final int msg;
        public OutputMonitor(int msgType, java.io.InputStream is) {
            br = new java.io.BufferedReader(new java.io.InputStreamReader(is), bufferedSize);
            msg = msgType;
        }
        public void run() {
            try{
                String line;
                do {
                    line = br.readLine();
                    mHandler.obtainMessage(msg, line).sendToTarget(); // NOTE: the last null is also sent!
                } while(line != null);
            } catch (Exception e) {
                mHandler.obtainMessage(MSG_EXCEPTION, e).sendToTarget();
            }
        }
    }

    
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
        	handle(msg);
        }
    };
    
    private BroadcastReceiver connectivityReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mHandler.sendEmptyMessage(MSG_NETSCHANGE);
        }
    };


    @Override
    public void onCreate() {
    	super.onCreate();
    	Log.d(TAG, String.format(this.getString(R.string.creating), this.getClass().getSimpleName()));
    	
    	singleton = this;
        this.wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        
        try {
        	this.mStartForeground = this.getClass().getMethod("startForeground", new Class[] {
                    int.class, Notification.class});
        } catch (NoSuchMethodException e) {
        	this.mStartForeground = null;
        }

        this.state = STATE_STOPPED;
        this.adHocApp = (AdHocApp) this.getApplication();
        this.adHocApp.setAdHocService(this);
        this.mHandler.sendEmptyMessage(MSG_START);
        
        // Unlock recive UDP ports
        PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
        this.wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "AdHocService");
        this.wakeLock.acquire();

        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(connectivityReceiver, filter);
        Log.d(TAG, String.format(getString(R.string.created), this.getClass().getSimpleName()));
    }

    @Override
    public void onDestroy() {
    	Log.d(TAG, String.format(getString(R.string.stopping), this.getClass().getSimpleName()));
    	this.mHandler.sendEmptyMessage(MSG_STOP);

        this.wakeLock.release();
        try {
            unregisterReceiver(connectivityReceiver);
        } catch (Exception e) {
        }

        singleton = null;
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    
    private void handle(Message msg) {
        switch (msg.what) {
        case MSG_EXCEPTION:
            if (this.state == STATE_STOPPED || this.state == STATE_FAILED) {
            	return;
            }
            Throwable thr = (Throwable)msg.obj;
            Log.e(TAG, "", thr);
            this.stopNativeProcess();
            this.state = STATE_FAILED;
            break;
        case MSG_ERROR:
            if (this.state == STATE_STOPPED || this.state == STATE_FAILED || this.process == null) {
            	return;
            }
            if (msg.obj != null) {
                String line = (String)msg.obj;
                String errorFormat = this.getString(R.string.error);
                Log.e(TAG, String.format(errorFormat, line));
                if ((this.state == STATE_STARTING)) {
                    if (NativeHelper.isRootError(line)) {
                        this.adHocApp.adHocFailed(AdHocApp.ERROR_ROOT);
                    }
                    else if (NativeHelper.isSupplicantError(line)) {
                    	this.adHocApp.adHocFailed(AdHocApp.ERROR_SUPPLICANT);
                    }
                    else {
                    	this.adHocApp.adHocFailed(AdHocApp.ERROR_OTHER);
                    }
                }
                else {
                	this.adHocApp.adHocFailed(AdHocApp.ERROR_OTHER);
                }
            }
            else {
            	this.stopNativeProcess();
            }
			this.state = STATE_FAILED;
            break;
        case MSG_OUTPUT:
            if (this.state == STATE_STOPPED || this.state == STATE_FAILED || this.process == null){
            	return;
            }
            String line = (String)msg.obj;
            if (line == null) {
                break; // ignore it, wait for MSG_ERROR(null)
            }
            else if (NativeHelper.isWifiOK(line)) {
                if (this.state == STATE_STARTING) {
                	this.state = STATE_RUNNING;
                    String startedFormat = this.getString(R.string.started);
                    Log.d(TAG, String.format(startedFormat, this.getClass().getSimpleName()));
                    this.adHocApp.adHocStarted();
                }
            }
            else {
            	Log.i(TAG, line);
            }
            break;
        case MSG_START:
        	if (this.state != STATE_STOPPED && this.state != STATE_FAILED) {
        		return;
        	}
    		String startingFormat = this.getString(R.string.starting);
            Log.d(TAG, String.format(startingFormat, this.getClass().getSimpleName()));

            if (!NativeHelper.existAssets(this)) {
            	String format = this.getString(R.string.assetsProblem);
                Log.e(TAG, String.format(format, this.getClass().getSimpleName()));
                this.state = STATE_FAILED;
                break;
            }
            this.state = STATE_STARTING;
            break;
        case MSG_NETSCHANGE:
        	if (this.state == STATE_STOPPED || this.state == STATE_FAILED) {
        		return;
        	}
            int wifiState = this.wifiManager.getWifiState();
            String proccesID = this.process == null ? "null" : this.process.toString();
            String formatString = this.getString(R.string.netschange);
            String formatedString = String.format(formatString, wifiState, this.state, proccesID); 
            Log.d(TAG, formatedString);
            if (wifiState == WifiManager.WIFI_STATE_DISABLED) {
            	if ((this.state == STATE_STARTING) && (this.process == null)) {
            		if (!this.startNativeProcess()) {
                        Log.e(TAG, this.getString(R.string.starterr));
                        this.state = STATE_FAILED;
                        break;
                    }
                }
            }
            else {
            	if (wifiState == WifiManager.WIFI_STATE_DISABLING || wifiState == WifiManager.WIFI_STATE_ENABLING) {
            		break;
            	}
            	if (this.state == STATE_RUNNING) {
                	this.adHocApp.updateToast(this.getString(R.string.conflictwifi), true);
                    Log.w(TAG, this.getString(R.string.conflictwifi));
                    this.stopNativeProcess();
                    Log.d(TAG, this.getString(R.string.restarting));
                    this.wifiManager.setWifiEnabled(false);
                    this.state = STATE_STARTING;
                }
                else if (this.state == STATE_STARTING) {
                	if (wifiState == WifiManager.WIFI_STATE_UNKNOWN) {
                		this.wifiManager.setWifiEnabled(true);
                	}
                	else if (wifiState == WifiManager.WIFI_STATE_ENABLED) {
                    	this.adHocApp.updateToast(this.getString(R.string.disablewifi), false);
                        this.wifiManager.setWifiEnabled(false);
                        Log.d(TAG, this.getString(R.string.waitwifi));
                    }
                }
            }
            break;
        case MSG_STOP:
            if (this.state == STATE_STOPPED) {
            	return;
            }
            this.stopNativeProcess();
            if (this.state != STATE_FAILED) {
            	this.state = STATE_STOPPED;
            }
            this.adHocApp.adHocStopped();
        	String stoppedFormat = this.getString(R.string.stopped);
            Log.d(TAG, String.format(stoppedFormat, this.getClass().getSimpleName()));
            break;
        }
        this.adHocApp.adHocUpdated(this.state);
        if (this.state == STATE_FAILED) {
        	this.stopSelf();
        }
    }
    

    public int getState() {
        return state;
    }
    
    protected String[] getEnvironmentFromPrefs() {
    	ArrayList<String> envlist = new ArrayList<String>();

    	Map<String, String> env = System.getenv();
    	for (String envName : env.keySet()) {
    		envlist.add(envName + "=" + env.get(envName));
    	}

    	PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

    	final int[] ids = SettingsActivity.prefids;
    	for (int i = 0; i < ids.length; ++i) {
    		String k = getString(ids[i]);
    		String v = prefs.getString(k, null);
    		if (v != null && v.length() != 0) {
    			// TODO some chars need to be escaped, but this seems to add "" to the ESSID name
    			envlist.add("brncl_" + k + "=" + v);
    		}
    	}
    	// not included in prefids are checkboxes
    	final int[] checks = SettingsActivity.checks;
    	for (int i = 0; i < checks.length; ++i) {
    		String k = getString(checks[i]);
    		if (prefs.getBoolean(k, false))
    			envlist.add("brncl_" + k + "=1");
    	}
    	envlist.add("brncl_path=" + NativeHelper.app_bin.getAbsolutePath());

    	String[] ret = (String[]) envlist.toArray(new String[0]);
//    	for (String s : ret) {
//    		Log.d(TAG, "set env: " + s);
//    	}
    	return ret;
    }

    private boolean startNativeProcess() {
    	String cmd = NativeHelper.SU_C;
        try {
        	Runtime runtime = Runtime.getRuntime();
            process = runtime.exec(cmd, getEnvironmentFromPrefs(), NativeHelper.app_bin);
            threads[THREAD_OUTPUT] = new Thread(new OutputMonitor(MSG_OUTPUT, process.getInputStream()));
            threads[THREAD_ERROR] = new Thread(new OutputMonitor(MSG_ERROR, process.getErrorStream()));
            threads[THREAD_OUTPUT].start();
            threads[THREAD_ERROR].start();
        } catch (Exception e) {
        	String failedFormat = this.getString(R.string.failed);
        	Log.e(TAG, String.format(getString(R.string.execerr), cmd));
        	Log.e(TAG, String.format(failedFormat, this.getClass().getSimpleName()), e);
            return false;
        }
        return true;
    }

    private void stopNativeProcess() {
        if (process != null) {
            if (state != STATE_STOPPED && state != STATE_FAILED) {
                try {
                    process.getOutputStream().close();
                } catch (Exception e) {
                }
            }
            try {
                process.waitFor();
            } catch (InterruptedException e) {
            }

            try {
                int exit_status = process.exitValue();
                String formatString = getString(R.string.nativeProcess); 
                Log.i(TAG, String.format(formatString, exit_status));
            } catch (IllegalThreadStateException e) {
            	Log.e(TAG, getString(R.string.dirtystop), e);
            }
            process.destroy();
            process = null;
            threads[THREAD_OUTPUT].interrupt();
            threads[THREAD_ERROR].interrupt();
        }
    }

    
    public void startForegroundCompat(int id, Notification notification) {
        if (this.mStartForeground != null) {
            try {
            	this.mStartForeground.invoke(this, new Object[] {Integer.valueOf(id), notification});
            } catch (InvocationTargetException e) {
                Log.w(TAG, this.getString(R.string.unableStartForeground), e);
            } catch (IllegalAccessException e) {
            	Log.w(TAG, this.getString(R.string.unableStartForeground), e);
            }
            return;
        }
        // Fall back on the old API.
        setForeground(true);
    }
}


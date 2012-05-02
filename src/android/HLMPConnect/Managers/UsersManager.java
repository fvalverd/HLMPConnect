package android.HLMPConnect.Managers;

import android.os.Handler;
import android.util.Log;

import hlmp.CommLayer.NetUser;
import hlmp.CommLayer.Observers.AddUserEventObserverI;
import hlmp.CommLayer.Observers.RefreshLocalUserEventObserverI;
import hlmp.CommLayer.Observers.RefreshUserEventObserverI;
import hlmp.CommLayer.Observers.RemoveUserEventObserverI;

import android.HLMPConnect.UsersActivity;


public class UsersManager extends Thread implements AddUserEventObserverI, RemoveUserEventObserverI, RefreshUserEventObserverI, RefreshLocalUserEventObserverI {

	private static final String MSG_TAG = "HLMP -> UsersManager";
	
	protected Handler mHandler;

	public void refreshLocalUserEventUpdate(NetUser netUser) {
//		String format = "REFRESH LOCAL USER: %s";
//		Log.i(MSG_TAG, String.format(format, netUser.getName()));
		if (mHandler != null) {
			this.mHandler.obtainMessage(UsersActivity.REFRESH_LOCAL_USERS, netUser.getName()).sendToTarget();
		}
	}

	public void refreshUserEventUpdate(NetUser netUser) {
//		String format = "REFRES USER: %s";
//		Log.i(MSG_TAG, String.format(format, netUser.getName()));
		if (mHandler != null) {
			this.mHandler.obtainMessage(UsersActivity.REFRESH_USER, netUser.getName()).sendToTarget();
		}
	}

	public void removeUserEventUpdate(NetUser netUser) {
		String format = "REMOVE USER: %s";
		Log.i(MSG_TAG, String.format(format, netUser.getName()));
		if (mHandler != null) {
			this.mHandler.obtainMessage(UsersActivity.REFRESH_USER, netUser.getName()).sendToTarget();
		}
	}

	public void addUserEventUpdate(NetUser netUser) {
		String format = "ADD USER: %s";
		Log.i(MSG_TAG, String.format(format, netUser.getName()));
		if (mHandler != null) {
			this.mHandler.obtainMessage(UsersActivity.ADD_USER, netUser.getName()).sendToTarget();
		}
	}

	public void setHandler(Handler mHandler) {
		this.mHandler = mHandler;
	}
}

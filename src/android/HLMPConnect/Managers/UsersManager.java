package android.HLMPConnect.Managers;

import android.os.Handler;

import hlmp.CommLayer.NetUser;
import hlmp.CommLayer.Observers.AddUserEventObserverI;
import hlmp.CommLayer.Observers.RefreshLocalUserEventObserverI;
import hlmp.CommLayer.Observers.RefreshUserEventObserverI;
import hlmp.CommLayer.Observers.RemoveUserEventObserverI;

import android.HLMPConnect.UsersActivity;


public class UsersManager extends Thread implements AddUserEventObserverI, RemoveUserEventObserverI, RefreshUserEventObserverI, RefreshLocalUserEventObserverI {

	protected Handler mHandler;

	public void refreshLocalUserEventUpdate(NetUser netUser) {
		this.mHandler.obtainMessage(UsersActivity.REFRESH_LOCAL_USERS, netUser.getName()).sendToTarget();		
	}

	public void refreshUserEventUpdate(NetUser netUser) {
		this.mHandler.obtainMessage(UsersActivity.REFRESH_USER, netUser.getName()).sendToTarget();
	}

	public void removeUserEventUpdate(NetUser netUser) {
		// TODO Auto-generated method stub
		
	}

	public void addUserEventUpdate(NetUser netUser) {
		this.mHandler.obtainMessage(UsersActivity.ADD_USER, netUser.getName()).sendToTarget();
	}

	public void setHandler(Handler mHandler) {
		this.mHandler = mHandler;
	}
}

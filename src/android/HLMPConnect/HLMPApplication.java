package android.HLMPConnect;

import android.app.Application;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.net.InetAddress;
import java.net.UnknownHostException;

import hlmp.CommLayer.Communication;
import hlmp.CommLayer.Configuration;
import hlmp.CommLayer.SubProtocolList;
import hlmp.CommLayer.Messages.Message;
import hlmp.CommLayer.Observers.ErrorMessageEventObserverI;
import hlmp.CommLayer.Observers.ExceptionEventObserverI;
import hlmp.CommLayer.Observers.NetInformationEventObserverI;
//import hlmp.NetLayer.NetworkAdapter;
import hlmp.SubProtocol.Chat.ChatProtocol;

import android.HLMPConnect.Managers.ChatManager;
import android.HLMPConnect.Managers.UsersManager;


public class HLMPApplication extends Application implements ErrorMessageEventObserverI, ExceptionEventObserverI, NetInformationEventObserverI {
	
//	Android Wifi Manager
	protected WifiManager wifiManager;
	protected boolean previousWifiSate;

//	HLMP
	protected Communication communication;
	protected ChatManager chatManager;
	private UsersManager usersManager;

	public static final String MSG_TAG = "HLMP -> HLMPApplication";

	
	@Override
	public void onCreate() {
		super.onCreate();
		
//		this.wifiManager = (WifiManager) (this.getSystemService(Context.WIFI_SERVICE));
//		this.previousWifiSate = wifiManager.isWifiEnabled();
//		
////		Set HLMP Configurations
//		Configuration configuration = new Configuration();
//		this.usersManager = new UsersManager();
//		this.chatManager = new ChatManager();
//		
////		Set HLMP Subprotocols
//		SubProtocolList subProtocols = new SubProtocolList();
//		ChatProtocol chatProtocol = new ChatProtocol(chatManager);
//		subProtocols.add(hlmp.SubProtocol.Chat.Types.CHATPROTOCOL, chatProtocol);
//		this.chatManager.setChatProtocol(chatProtocol);
//		this.chatManager.setNetUser(configuration.getNetUser());
//		
////		Set HLMP Communication
//		this.communication = new Communication(configuration, subProtocols, null);
////		this.communication.getConfiguration().setNetworkAdapter(new NetworkAdapter(wifiManager));
//		
//		this.communication.subscribeAddUserEvent(this.usersManager);
//		this.communication.subscribeExceptionEvent(this);
//		this.communication.subscribeNetInformationEvent(this);
//		this.communication.subscribeRemoveUserEvent(this.usersManager);
//		this.communication.subscribeRefreshUserEvent(this.usersManager);
//		this.communication.subscribeRefreshLocalUserEvent(this.usersManager);
	}

	
	public ChatManager getChatManager() {
		return this.chatManager;
	}
	
	public UsersManager getUsersManager() {
		return this.usersManager;
	}

	public Communication getCommunication() {
		return this.communication;
	}


	public void startAdHocWithIpAndUsername(String ip, String username) {
		
		this.wifiManager = (WifiManager) (this.getSystemService(Context.WIFI_SERVICE));
		this.previousWifiSate = wifiManager.isWifiEnabled();
		
//		Set HLMP Configurations
		Configuration configuration = new Configuration();
		this.usersManager = new UsersManager();
		this.chatManager = new ChatManager();
		
//		Set HLMP Subprotocols
		SubProtocolList subProtocols = new SubProtocolList();
		ChatProtocol chatProtocol = new ChatProtocol(chatManager);
		subProtocols.add(hlmp.SubProtocol.Chat.Types.CHATPROTOCOL, chatProtocol);
		this.chatManager.setChatProtocol(chatProtocol);
		this.chatManager.setNetUser(configuration.getNetUser());
		
//		Set HLMP Communication
		this.communication = new Communication(configuration, subProtocols, null);
//		this.communication.getConfiguration().setNetworkAdapter(new NetworkAdapter(wifiManager));
		
		this.communication.subscribeAddUserEvent(this.usersManager);
		this.communication.subscribeErrorMessageEvent(this);
		this.communication.subscribeExceptionEvent(this);
		this.communication.subscribeNetInformationEvent(this);
		this.communication.subscribeRemoveUserEvent(this.usersManager);
		this.communication.subscribeRefreshUserEvent(this.usersManager);
		this.communication.subscribeRefreshLocalUserEvent(this.usersManager);
		
		
		
//		Configuration configuration = this.communication.getConfiguration();
		
		try {
			configuration.getNetData().setIpTcpListener(InetAddress.getByName(ip));
			configuration.getNetUser().setName(username);
			this.communication.startEventConsumer();
			this.communication.connect();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

	public void stopAdHoc() {
		if (communication != null) {
			this.communication.disconnect();
			this.chatManager.interrupt();
			this.usersManager.interrupt();
			this.communication.stopEventConsumer();
		}
	}


	
	
	public void netInformationEventUpdate(String s) {
		Log.i(MSG_TAG, s);
	}
	
	public void exceptionEventUpdate(Exception e) {
		Log.e(MSG_TAG, " EXCEPTION: " + e.toString());	
	}
	
	public void errorMessageEventUpdate(Message m) {
		Log.e(MSG_TAG, " ERROR: " + m.toString());
	}
}
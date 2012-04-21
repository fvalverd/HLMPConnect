package android.HLMPConnect;

import android.app.Application;
import android.content.Context;
import android.net.wifi.WifiManager;
import java.net.InetAddress;
import java.net.UnknownHostException;

import hlmp.CommLayer.Communication;
import hlmp.CommLayer.Configuration;
import hlmp.CommLayer.SubProtocolList;
import hlmp.NetLayer.NetworkAdapter;
import hlmp.SubProtocol.Chat.ChatProtocol;

import android.HLMPConnect.Managers.ChatManager;
import android.HLMPConnect.Managers.UsersManager;


public class HLMPApplication extends Application {
	
//	TODO: Agregar el onCreate
//	TODO: Agregar el onTerminate
	
//	Android Wifi Manager
	protected WifiManager wifiManager;
	protected boolean previousWifiSate;

//	HLMP
	protected Communication communication;
	protected ChatManager chatManager;
	private UsersManager usersManager;
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		this.wifiManager = (WifiManager) (this.getSystemService(Context.WIFI_SERVICE));
		this.previousWifiSate = wifiManager.isWifiEnabled();
		
//		Set HLMP Configurations
		Configuration configuration = new Configuration();
//		Set HLMP for Android 2.3
		configuration.getNetData().setOpSystem(hlmp.NetLayer.Constants.OpSystemType.ANDROID23);
		
//		Set HLMP Subprotocols
		SubProtocolList subProtocols = new SubProtocolList();
//		this.pingProtocol = new PingProtocol(this);
//		subProtocols.add(hlmp.SubProtocol.Ping.Types.PINGPROTOCOL, pingProtocol);
		this.chatManager = new ChatManager();
		ChatProtocol chatProtocol = new ChatProtocol(chatManager);
		subProtocols.add(hlmp.SubProtocol.Chat.Types.CHATPROTOCOL, chatProtocol);
		this.chatManager.setChatProtocol(chatProtocol);
		this.chatManager.setNetUser(configuration.getNetUser());
		
//		Create HLMP Communication
		this.communication = new Communication(configuration, subProtocols, null);
		this.communication.getConfiguration().setNetworkAdapter(new NetworkAdapter(wifiManager));
//		this.communication.subscribeNetInformationEvent(this);
//		this.communication.subscribeExceptionEvent(this);
		
		this.usersManager = new UsersManager();
		this.communication.subscribeAddUserEvent(this.usersManager);
		this.communication.subscribeRemoveUserEvent(this.usersManager);
		this.communication.subscribeRefreshUserEvent(this.usersManager);
		this.communication.subscribeRefreshLocalUserEvent(this.usersManager);
	}
	
	public void startAdHocWithIpAndUsername(String ip, String username) {
		Configuration configuration = this.communication.getConfiguration();
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
		this.communication.stopEventConsumer();
		this.communication.disconnect();
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
}
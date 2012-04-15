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


public class HLMPApplication extends Application {
	
//	TODO: Agregar el onCreate
//	TODO: Agregar el onTerminate
	
//	Android Wifi Manager
	protected WifiManager wifiManager;
	protected boolean previousWifiSate;
//	HLMP Communication
	protected Communication communication;
	
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
//		this.chatProtocol = new ChatProtocol(this);
//		subProtocols.add(hlmp.SubProtocol.Ping.Types.PINGPROTOCOL, pingProtocol);
//		subProtocols.add(hlmp.SubProtocol.Chat.Types.CHATPROTOCOL, chatProtocol);
		
//		Create HLMP Communication
		this.communication = new Communication(configuration, subProtocols, null);
		this.communication.getConfiguration().setNetworkAdapter(new NetworkAdapter(wifiManager));
//		this.communication.subscribeNetInformationEvent(this);
//		this.communication.subscribeExceptionEvent(this);
//		this.communication.subscribeAddUserEvent(this);
//		this.communication.subscribeRemoveUserEvent(this);
//		this.communication.subscribeRefreshUserEvent(this);
//		this.communication.subscribeRefreshLocalUserEvent(this);
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
}
package android.HLMPConnect;

import java.net.InetAddress;
import java.net.UnknownHostException;

import android.os.Handler;
import android.util.Log;

import hlmp.CommLayer.Communication;
import hlmp.CommLayer.Configuration;
import hlmp.CommLayer.SubProtocolList;
import hlmp.CommLayer.Messages.Message;
import hlmp.CommLayer.Observers.ErrorMessageEventObserverI;
import hlmp.CommLayer.Observers.ExceptionEventObserverI;
import hlmp.CommLayer.Observers.NetInformationEventObserverI;
import hlmp.NetLayer.Constants.IpState;
import hlmp.NetLayer.Constants.WifiConnectionState;
import hlmp.NetLayer.Interfaces.WifiHandler;
import hlmp.SubProtocol.Chat.ChatProtocol;
import hlmp.SubProtocol.Ping.PingProtocol;

import android.adhoc.AdHocApp;
import android.adhoc.AdHocService;
import android.HLMPConnect.Managers.ChatManager;
import android.HLMPConnect.Managers.UsersManager;
import android.HLMPConnect.Managers.PingManager;


public class HLMPApplication extends AdHocApp implements ErrorMessageEventObserverI, ExceptionEventObserverI, NetInformationEventObserverI, WifiHandler {
	public static final String MSG_TAG = "HLMP -> HLMPApplication";
	
	protected Communication communication;
	protected ChatManager chatManager;
	protected UsersManager usersManager;
	protected PingManager pingManager;

	static HLMPApplication self;
	
	final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(android.os.Message msg) {
			self.startAdHoc();
		};
    };
	
	
    @Override
    public void onCreate() {
    	super.onCreate();
    	self = this;
    };
	
	@Override
    public void onTerminate() {
    	Log.i("HLMPApplication", "HLMPApplication distroying...");
        super.onTerminate();
        Log.i("HLMPApplication", "HLMPApplication distroying... OK!");
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


	public void startHLMP(String username) {
		// Set HLMP Configurations
		Configuration configuration = new Configuration();
		if (this.usersManager == null) {
			this.usersManager = new UsersManager();
		}
		if (this.chatManager == null) {
			this.chatManager = new ChatManager();
		}
		if (this.pingManager == null) {
			this.pingManager = new PingManager();
		}
		
		// Set HLMP Subprotocols
		SubProtocolList subProtocols = new SubProtocolList();
		ChatProtocol chatProtocol = new ChatProtocol(this.chatManager);
		subProtocols.add(hlmp.SubProtocol.Chat.Types.CHATPROTOCOL, chatProtocol);
		this.chatManager.setChatProtocol(chatProtocol);
		this.chatManager.setNetUser(configuration.getNetUser());
		
		PingProtocol pingProtocol = new PingProtocol(this.pingManager);
		subProtocols.add(hlmp.SubProtocol.Ping.Types.PINGPROTOCOL, pingProtocol);
		
		// Set HLMP Communication
		this.communication = new Communication(configuration, subProtocols, null, this);
		
		this.communication.subscribeAddUserEvent(this.usersManager);
		this.communication.subscribeErrorMessageEvent(this);
		this.communication.subscribeExceptionEvent(this);
		this.communication.subscribeNetInformationEvent(this);
		this.communication.subscribeRemoveUserEvent(this.usersManager);
		this.communication.subscribeRefreshUserEvent(this.usersManager);
		this.communication.subscribeRefreshLocalUserEvent(this.usersManager);
		
		configuration.getNetUser().setName(username);
		this.communication.startEventConsumer();
		this.communication.connect();
	}

	public void stopHLMP() {
		if (communication != null) {
			this.communication.disconnect();
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

	
	public InetAddress getInetAddress() {
		InetAddress inetAddress = null;
		try {
			inetAddress = InetAddress.getByName(this.getIPAdress());
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return inetAddress;
	}

	
	
	// AdHocApp Overrides
	
	@Override
	public void adHocFailed(int error) {
		super.adHocFailed(error);
		if (error == AdHocApp.ERROR_ROOT) {
			//communication.disconnect();
		}
	}
	
	
	// HLMP WifiHandler API
	
	public void connect() {
		// Necessary Hanlder for context
		mHandler.sendEmptyMessage(0);
	}

	public void disconnect() {
		this.stopAdHoc();
	}

	public int getConnectionState() {
		int state = WifiConnectionState.STOP;
		switch (this.getAdHocServiceState()) {
			case AdHocService.STATE_FAILED : {
				state = WifiConnectionState.FAILED;
				break;
			}
			case AdHocService.STATE_STARTING : {
				state = WifiConnectionState.WAITING;
				break;
			}
			case AdHocService.STATE_RUNNING : {
				state = WifiConnectionState.CONNECTED;
				break;
			}
		}
		return state;
	}

	public int getIpState() {
		int state = IpState.NOTFOUND;
		switch (this.getAdHocServiceState()) {
			case AdHocService.STATE_FAILED : {
				state = IpState.INVALID;
				break;
			}
			case AdHocService.STATE_STARTING : {
				state = IpState.NOTFOUND;
				break;
			}
			case AdHocService.STATE_RUNNING : {
				state = IpState.VALID;
				break;
			}
		}
		return state;
	}

}
package android.HLMPConnect;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.Date;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;

import hlmp.CommLayer.Communication;
import hlmp.CommLayer.Configuration;
import hlmp.CommLayer.SubProtocolList;
import hlmp.CommLayer.Messages.Message;
import hlmp.CommLayer.Observers.ConnectEventObserverI;
import hlmp.CommLayer.Observers.ConnectingEventObserverI;
import hlmp.CommLayer.Observers.DisconnectEventObserverI;
import hlmp.CommLayer.Observers.DisconnectingEventObserverI;
import hlmp.CommLayer.Observers.ErrorMessageEventObserverI;
import hlmp.CommLayer.Observers.ExceptionEventObserverI;
import hlmp.CommLayer.Observers.NetInformationEventObserverI;
import hlmp.NetLayer.Constants.IpState;
import hlmp.NetLayer.Constants.WifiConnectionState;
import hlmp.NetLayer.Interfaces.WifiHandler;
import hlmp.SubProtocol.Chat.ChatProtocol;
import hlmp.SubProtocol.FileTransfer.FileData;
import hlmp.SubProtocol.FileTransfer.FileInformation;
import hlmp.SubProtocol.FileTransfer.FileTransferProtocol;
import hlmp.SubProtocol.FileTransfer.Interfaces.ManageDirectory;
import hlmp.SubProtocol.Ping.PingProtocol;

import android.adhoc.AdHocApp;
import android.adhoc.AdHocService;
import android.HLMPConnect.Managers.ChatManager;
import android.HLMPConnect.Managers.FilesManager;
import android.HLMPConnect.Managers.UsersManager;
import android.HLMPConnect.Managers.PingManager;


public class HLMPApplication extends AdHocApp implements ErrorMessageEventObserverI, ExceptionEventObserverI, NetInformationEventObserverI, WifiHandler, ConnectEventObserverI, ConnectingEventObserverI, DisconnectEventObserverI, DisconnectingEventObserverI, ManageDirectory {
	
	static final String MSG_TAG = "HLMPApplication";
	
	static final int HLMP_STARTING_SHOW = 0;
	static final int HLMP_STARTING_HIDE = 1;
	static final int HLMP_STOPPING_SHOW = 2;
	static final int HLMP_STOPPING_HIDE = 3;
	
	protected Communication communication;
	protected ChatManager chatManager;
	protected UsersManager usersManager;
	protected PingManager pingManager;
	protected FilesManager filesManager;
	protected Handler tabHostHandler;
	protected Handler sharedFilesHandler;
	private long startTimeMillis;
	static HLMPApplication self;
	
	static final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(android.os.Message msg) {
			self.startAdHoc();
		};
    };
	
    static final Handler hlmpDialogsHandler = new Handler() {
		@Override
		public void handleMessage(android.os.Message msg) {
			if (msg.what == HLMP_STARTING_SHOW) {
				self.adHocActivity.showDialog(ConnectionActivity.DLG_HLMP_STARTING);
			}
			else if (msg.what == HLMP_STARTING_HIDE) {
				try{
					self.adHocActivity.dismissDialog(ConnectionActivity.DLG_HLMP_STARTING);
			    } catch(Exception e) {}
			}
			else if (msg.what == HLMP_STOPPING_SHOW) {
				try{
					self.adHocActivity.dismissDialog(ConnectionActivity.DLG_HLMP_STARTING);
			    } catch(Exception e) {}
				try{
					self.adHocActivity.showDialog(ConnectionActivity.DLG_HLMP_STOPPING);
				} catch(Exception e) {}
			}
			else if (msg.what == HLMP_STOPPING_HIDE) {
				try{
					self.adHocActivity.dismissDialog(ConnectionActivity.DLG_HLMP_STOPPING);
			    } catch(Exception e) {}
			}
		};
    };
	
    
    
    
    
    @Override
    public void onCreate() {
    	super.onCreate();
    	self = this;
    };
	
	@Override
    public void onTerminate() {
    	super.onTerminate();
    }
	
	
	// HLMPConnect
	
	public ChatManager getChatManager() {
		return this.chatManager;
	}
	
	public UsersManager getUsersManager() {
		return this.usersManager;
	}

	public FilesManager getFilesManager() {
		return this.filesManager;
	}
	
	public void setTabHostHandler(Handler tabHostHandler) {
		this.tabHostHandler = tabHostHandler;
	}
	
	public Handler getTabHostHandler() {
		return this.tabHostHandler;
	}
	
	public void setSharedFilesHandler(Handler sharedFilesHandler) {
		this.sharedFilesHandler = sharedFilesHandler;
	}
	
	public Handler getSharedFilesHandler() {
		return this.sharedFilesHandler;
	}
	
	// AdHocApp Overrides
	
	@Override
	public void adHocFailed(int error) {
		super.adHocFailed(error);
		if (error == AdHocApp.ERROR_ROOT) {
			//communication.disconnect();
		}
	}
	
	
	// HLMP Access
	
	public void startHLMP(String username) {
		// Set HLMP Configurations
		Configuration configuration = new Configuration();
		
		this.usersManager = new UsersManager();
		if (this.filesManager == null) {
			this.filesManager = new FilesManager(this);
		}
		else {
			this.filesManager.clearData();
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
		
		FileData fileData = new FileData(this);
		FileTransferProtocol fileTransferProtocol = new FileTransferProtocol(this.filesManager, this.filesManager, fileData);
		subProtocols.add(hlmp.SubProtocol.FileTransfer.Constants.FileTransferProtocolType.FILETRANSFERPROTOCOL, fileTransferProtocol);
		this.filesManager.setFileTranfersProtocol(fileTransferProtocol);

		
		// Set HLMP Communication
		
		this.communication = new Communication(configuration, subProtocols, null, this);
		
		this.communication.subscribeAddUserEvent(this.usersManager);
		this.communication.subscribeAddUserEvent(fileTransferProtocol);
		this.communication.subscribeConnectEvent(this);
		this.communication.subscribeConnectEvent(fileTransferProtocol);
		this.communication.subscribeConnectingEvent(this);
		this.communication.subscribeDisconnectEvent(this);
		this.communication.subscribeDisconnectEvent(fileTransferProtocol);
		this.communication.subscribeDisconnectingEvent(this);
		this.communication.subscribeErrorMessageEvent(this);
		this.communication.subscribeExceptionEvent(this);
		this.communication.subscribeNetInformationEvent(this);
		this.communication.subscribeReconnectingEvent(fileTransferProtocol);
		this.communication.subscribeRemoveUserEvent(this.usersManager);
		this.communication.subscribeRemoveUserEvent(this.filesManager);
		this.communication.subscribeRefreshUserEvent(this.usersManager);
		this.communication.subscribeRefreshLocalUserEvent(this.usersManager);
		
		
		
		configuration.getNetUser().setName(username);
		
		this.filesManager.setCommunication(this.communication);
		
		this.communication.startEventConsumer();
		this.communication.connect();
	}

	public void stopHLMP() {
		if (communication != null) {
			this.communication.disconnect();
			this.communication.stopEventConsumer();
		}
	}

	public Communication getCommunication() {
		return this.communication;
	}
	
	
	// HLMP Events
	
	public void netInformationEventUpdate(String s) {
		Log.i(MSG_TAG, s);
	}
	
	public void exceptionEventUpdate(Exception e) {
		Log.e(MSG_TAG, " EXCEPTION: " + e.toString());	
	}
	
	public void errorMessageEventUpdate(Message m) {
		Log.e(MSG_TAG, " ERROR: " + m.toString());
	}

	public void connectingEventUpdate() {
		this.saveConnectTime(HLMP_STARTING_SHOW);
		hlmpDialogsHandler.sendEmptyMessage(HLMP_STARTING_SHOW);
	}

	public void connectEventUpdate() {
		this.saveConnectTime(HLMP_STARTING_HIDE);
		tabHostHandler.sendEmptyMessage(Tabs.ACTIVE);
		hlmpDialogsHandler.sendEmptyMessage(HLMP_STARTING_HIDE);
	}
	
	public void disconnectingEventUpdate() {
		hlmpDialogsHandler.sendEmptyMessage(HLMP_STOPPING_SHOW);
		
	}

	public void disconnectEventUpdate() {
		hlmpDialogsHandler.sendEmptyMessage(HLMP_STOPPING_HIDE);
	}
	
	
	// HLMP WifiHandler Implement
	
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

	public InetAddress getInetAddress() {
		InetAddress inetAddress = null;
		try {
			inetAddress = InetAddress.getByName(this.getIPAdress());
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return inetAddress;
	}

	
	// HLMP FileData of FileTransferProtocol Implemet
	
	public String createSharedDir() {
		// TODO: si es posible utilizar SDCARD
		File sharedDir = getDir(FilesActivity.SHARED_DIR_NAME_SUFIX, MODE_WORLD_READABLE);
		Log.d(MSG_TAG, "SharedDir = " + sharedDir.getAbsolutePath());
		return sharedDir.getAbsolutePath();
	}
	
	public String createDownloadDir() {
		// TODO: si es posible utilizar SDCARD
		File f = getCacheDir();
		Log.d(MSG_TAG, "DownloadCacheDirectory = " + f.getAbsolutePath());
		
		File downloadDir = getDir(FilesActivity.DOWNLOAD_DIR_NAME_SUFIX, MODE_WORLD_READABLE);
		Log.d(MSG_TAG, "DownloadDir = " + downloadDir.getAbsolutePath());
		return downloadDir.getAbsolutePath();
	}

	public void loadSharedFiles(FileData fileData) {
		// TODO: si es posible utilizar SDCARD
		
		File sharedDir = getDir(FilesActivity.SHARED_DIR_NAME_SUFIX, MODE_WORLD_READABLE);
		for (File file : sharedDir.listFiles()) {
            fileData.addFile(new FileInformation(file.getName(), file.length(), file.getAbsolutePath()));
        }
	}

	
	public void appendTextOnFilePath(String text, String fileNameSufix) {
		Date date = Calendar.getInstance().getTime();
		String results = date.toString() + "\t\t" + text + "\n";
		FileOutputStream download_time_stream = null;
		String fileName = this.communication.getConfiguration().getNetUser().getName();
		fileName += fileNameSufix;
		try {
			
			download_time_stream = openFileOutput(fileName, MODE_APPEND);
			download_time_stream.write(results.getBytes());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (download_time_stream != null) {
				try {
					download_time_stream.close();
				} catch (IOException e) {}
			}
		}
	}
	
	public void writeDowndloadTimeRecord(double seconds, double size_kb) {
		String results = "" + size_kb + "\t" + seconds + "\t" + size_kb/seconds;
		this.appendTextOnFilePath(results, FilesActivity.DOWNLOAD_DIR_NAME_SUFIX);
	}
	
	private void saveConnectTime(int hlmpStartingState) {
		if (hlmpStartingState == HLMP_STARTING_SHOW) {
			this.startTimeMillis = System.currentTimeMillis();
		}
		else if (hlmpStartingState == HLMP_STARTING_HIDE) {			
			this.appendTextOnFilePath("" + (System.currentTimeMillis()-this.startTimeMillis)/1000.0, FilesActivity.CONNECT_TIMES_FILENAME_SUFIX);
		}
	}
	
}
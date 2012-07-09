package android.HLMPConnect.Managers;

import java.net.InetAddress;
import java.util.Hashtable;
import android.os.Handler;
import android.util.Log;

import hlmp.CommLayer.Communication;
import hlmp.CommLayer.NetUser;
import hlmp.CommLayer.Observers.RemoveUserEventObserverI;
import hlmp.SubProtocol.FileTransfer.FileInformation;
import hlmp.SubProtocol.FileTransfer.FileInformationList;
import hlmp.SubProtocol.FileTransfer.FileTransferProtocol;
import hlmp.SubProtocol.FileTransfer.ControlI.FileHandlerI;
import hlmp.SubProtocol.FileTransfer.ControlI.FileListHandlerI;

import android.HLMPConnect.FileTransfer.CommunityFilesActivity;


public class FilesManager implements FileHandlerI, FileListHandlerI, RemoveUserEventObserverI {

	private static final String MSG_TAG = "HLMP -> FilesManager";
	
	protected Communication communication;
	protected FileTransferProtocol fileTransferProtocol;
	protected Handler communityFilesHandler;
	protected Hashtable<InetAddress, FileInformationList> communityFiles;
	
	
	public FilesManager() {
		this.communityFiles = new Hashtable<InetAddress, FileInformationList>();
	}
	
	
	public Hashtable<InetAddress, FileInformationList> getCommunityFiles() {
		return communityFiles;
	}
	
	public void setCommunityFilesHandler(Handler communityFilesHandler) {
		this.communityFilesHandler = communityFilesHandler;
	}

	public void setFileTranfersProtocol(FileTransferProtocol fileTransferProtocol) {
		this.fileTransferProtocol = fileTransferProtocol;
	}

	public void setCommunication(Communication communication) {
		this.communication = communication;
	}
	
	
	
	// FileTransferProtocol Manager API
	
	public void addFileInformationToFileData(FileInformation fileInformation) {
		fileTransferProtocol.getFileData().addFile(fileInformation);
	}
	
	public void sendFileList() {
		NetUser[] netUserList = this.communication.getNetUserList().userListToArray();
		for (NetUser netUser : netUserList) {
			this.fileTransferProtocol.sendFileList(netUser);
		}
	}
	
	public void sendFileRequest(NetUser netUser, FileInformation fileInformation) {
		this.fileTransferProtocol.sendFileRequest(netUser, fileInformation);
		
	}
	
	// Implements HLMP RemoveUserEventObserverI
	
	public void removeUserEventUpdate(NetUser netUser) {
		this.removeFileList(netUser);
	}
	
	
	
	// Implements HLMP FileListHandler
	
	public synchronized void addFileList(NetUser netUser, FileInformationList fileList) {
		if (fileList.toArray().length != 0) {
			Log.d(MSG_TAG, "addFileList user:" + netUser.getName());
			this.communityFiles.put(netUser.getIp(), fileList);
			if (this.communityFilesHandler != null) {
				this.communityFilesHandler.obtainMessage(CommunityFilesActivity.UPDATE_USERS_LIST).sendToTarget();
			}
		}
	}

	public void removeFileList(NetUser netUser) {
		Log.d(MSG_TAG, "removeFileList user:" + netUser.getName());
		this.communityFiles.remove(netUser.getIp());
		if (this.communityFilesHandler != null) {
			this.communityFilesHandler.obtainMessage(CommunityFilesActivity.UPDATE_USERS_LIST).sendToTarget();
		}
	}

	
	// Implements HLMP FiletHandler
	
	public void downloadFileQueued(NetUser netUser, String fileHandlerId, String fileName) {
		// TODO Auto-generated method stub
		
	}

	public void downloadFileOpened(String fileHandlerId) {
		// TODO Auto-generated method stub
		
	}

	public void downloadFileTransfer(String fileHandlerId, int percent) {
		// TODO Auto-generated method stub
		
	}

	public void downloadFileComplete(String fileHandlerId, String path) {
		// TODO Auto-generated method stub
		
	}

	public void downloadFileFailed(String fileHandlerId) {
		// TODO Auto-generated method stub
		
	}

	public void uploadFileQueued(NetUser netUser, String fileHandlerId, String fileName) {
		// TODO Auto-generated method stub
		
	}

	public void uploadFileOpened(String fileHandlerId) {
		// TODO Auto-generated method stub
		
	}

	public void uploadFileTransfer(String fileHandlerId, int percent) {
		// TODO Auto-generated method stub
		
	}

	public void uploadFileComplete(String fileHandlerId) {
		// TODO Auto-generated method stub
		
	}

	public void uploadFileFailed(String fileHandlerId) {
		// TODO Auto-generated method stub
	}
}
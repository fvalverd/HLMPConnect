package android.HLMPConnect.Managers;

import java.net.InetAddress;
import java.util.Hashtable;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import hlmp.CommLayer.Communication;
import hlmp.CommLayer.NetUser;
import hlmp.CommLayer.Observers.RemoveUserEventObserverI;
import hlmp.SubProtocol.FileTransfer.FileInformation;
import hlmp.SubProtocol.FileTransfer.FileInformationList;
import hlmp.SubProtocol.FileTransfer.FileTransferProtocol;
import hlmp.SubProtocol.FileTransfer.ControlI.FileHandlerI;
import hlmp.SubProtocol.FileTransfer.ControlI.FileListHandlerI;

import android.HLMPConnect.FilesActivity;
import android.HLMPConnect.FileTransfer.CommunityFilesActivity;
import android.HLMPConnect.FileTransfer.DownloadFilesActivity;
import android.HLMPConnect.FileTransfer.StateFilesActivity;


public class FilesManager implements FileHandlerI, FileListHandlerI, RemoveUserEventObserverI {

	private static final String MSG_TAG = "HLMP -> FilesManager";
	
	protected Communication communication;
	protected FileTransferProtocol fileTransferProtocol;
	protected Handler communityFilesHandler;
	protected Handler downloadFilesHandler;
	protected Handler stateFilesHandler;
	protected Hashtable<InetAddress, FileInformationList> communityFiles;
	protected Toast toast;
	protected FilesActivity filesActivity;
	
	
	public FilesManager() {
		this.communityFiles = new Hashtable<InetAddress, FileInformationList>();
	}
	
	
	public Hashtable<InetAddress, FileInformationList> getCommunityFiles() {
		return communityFiles;
	}

	public FilesActivity getFilesActivity() {
		return this.filesActivity;
	}
	
	public void setCommunityFilesHandler(Handler communityFilesHandler) {
		this.communityFilesHandler = communityFilesHandler;
	}
	
	public void setDownloadFilesHandler(Handler downloadFilesHandler) {
		this.downloadFilesHandler = downloadFilesHandler;
	}

	public void setStateFilesHandler(Handler stateFilesHandler) {
		this.stateFilesHandler = stateFilesHandler;
	}
	
	public void setFileTranfersProtocol(FileTransferProtocol fileTransferProtocol) {
		this.fileTransferProtocol = fileTransferProtocol;
	}

	public void setCommunication(Communication communication) {
		this.communication = communication;
	}
	
	public void setFilesActivity(FilesActivity filesActivity) {
		this.filesActivity = filesActivity;
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

	//	// Implements HLMP RemoveUserEventObserverI
	
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
	
	//	// Download
	
	public void downloadFileQueued(NetUser netUser, String fileHandlerId, String fileName) {
		String size = null;
		for (FileInformation fileInformation : communityFiles.get(netUser.getIp()).toArray()) {
			if (fileInformation.getName() == fileName) {
				size = "" + fileInformation.getSize();
			}
		}
		if (this.stateFilesHandler != null) {
			this.stateFilesHandler.obtainMessage(
					StateFilesActivity.ADD_DOWNLOAD,
					new String[] {fileHandlerId, fileName, size}).sendToTarget();
		}
	}

	public void downloadFileOpened(String fileHandlerId) {
		// TODO Auto-generated method stub
		
	}

	public void downloadFileTransfer(String fileHandlerId, int percent) {
		if (this.stateFilesHandler != null) {
			this.stateFilesHandler.obtainMessage(
					StateFilesActivity.UPDATE_DOWNLOAD_PERCENT,
					percent,
					0,
					fileHandlerId).sendToTarget();
		}
	}

	public void downloadFileComplete(String fileHandlerId, String path) {
		if (this.downloadFilesHandler != null) {
			this.downloadFilesHandler.obtainMessage(DownloadFilesActivity.UPDATE_USERS_LIST).sendToTarget();
		}
		if (this.stateFilesHandler != null) {
			this.stateFilesHandler.obtainMessage(
					StateFilesActivity.UPDATE_DOWNLOAD_PERCENT,
					100,
					0,
					fileHandlerId).sendToTarget();
		}
	}

	public void downloadFileFailed(String fileHandlerId) {
		// TODO: Notificar en el state que hubo un problema
	}

	//	// Upload
	
	public void uploadFileQueued(NetUser netUser, String fileHandlerId, String fileName) {
		String size = null;
		for (FileInformation fileInformation : fileTransferProtocol.getFileData().getFileList().toArray()) {
			if (fileInformation.getName() == fileName) {
				size = "" + fileInformation.getSize();
			}
		}
		if (this.stateFilesHandler != null) {
			this.stateFilesHandler.obtainMessage(
					StateFilesActivity.ADD_UPLOAD,
					new String[] {fileHandlerId, fileName, size}).sendToTarget();
		}
	}

	public void uploadFileOpened(String fileHandlerId) {
		// TODO Auto-generated method stub
		
	}

	public void uploadFileTransfer(String fileHandlerId, int percent) {
		if (this.stateFilesHandler != null) {
			this.stateFilesHandler.obtainMessage(
					StateFilesActivity.UPDATE_UPLOAD_PERCENT,
					percent,
					0,
					fileHandlerId).sendToTarget();
		}
	}

	public void uploadFileComplete(String fileHandlerId) {
		if (this.stateFilesHandler != null) {
			this.stateFilesHandler.obtainMessage(
					StateFilesActivity.UPDATE_UPLOAD_PERCENT,
					100,
					0,
					fileHandlerId).sendToTarget();
		}
	}

	public void uploadFileFailed(String fileHandlerId) {
		// TODO: Notificar en la lista de estados que no se puede seguir subiendo
	}
}
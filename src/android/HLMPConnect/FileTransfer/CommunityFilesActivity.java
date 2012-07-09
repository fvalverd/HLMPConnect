package android.HLMPConnect.FileTransfer;

import android.app.ExpandableListActivity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.SimpleExpandableListAdapter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import hlmp.CommLayer.Communication;
import hlmp.CommLayer.NetUser;
import hlmp.SubProtocol.FileTransfer.FileInformation;
import hlmp.SubProtocol.FileTransfer.FileInformationList;

import android.HLMPConnect.HLMPApplication;
import android.HLMPConnect.Managers.FilesManager;


public class CommunityFilesActivity extends ExpandableListActivity implements OnChildClickListener {

	protected static final String MSG_TAG = "HLMP -> CommunityFilesActivity";
	
	private static final String NAME = "NAME";
	private static final String SIZE = "SIZE";
	private static final String USER_IP = "USER_IP";
	private static final String ID = "ID";
	
	public static final int UPDATE_USERS_LIST =	0;
	
	private CommunityFilesActivity self;
	private Communication communication;
	private FilesManager filesManager;
	private Hashtable<InetAddress, FileInformationList> communityFiles;
	private ExpandableListAdapter mAdapter;
	private List<Map<String, String>> groupData;
	private List<List<Map<String, String>>> childData; 
	
	
	private final Handler communityFilesHandler = new Handler() {
        @Override
        public synchronized void handleMessage(Message msg) {
        	if (msg.what == UPDATE_USERS_LIST) {
        		List<Map<String, String>> groupData_old = groupData;
        		List<List<Map<String, String>>> childData_old = childData;
        		groupData = new ArrayList<Map<String, String>>();
                childData = new ArrayList<List<Map<String, String>>>();
                
        		Set<InetAddress> usersInetAddress = communityFiles.keySet();
        		for (InetAddress userInetAddress : usersInetAddress) {
        			Map<String, String> curGroupMap = new HashMap<String, String>();
                    groupData.add(curGroupMap);
                    
                    NetUser netUser = communication.getNetUserList().getUser(userInetAddress);
                    if (netUser == null) {
                    	groupData = groupData_old;
                    	childData = childData_old;
                    	return;
                    }
                    curGroupMap.put(NAME, netUser.getName());
                    curGroupMap.put(USER_IP, userInetAddress.getHostAddress());
                    
                    List<Map<String, String>> children = new ArrayList<Map<String, String>>();
        			for (FileInformation fileInformation : communityFiles.get(userInetAddress).toArray()) {
        				Map<String, String> curChildMap = new HashMap<String, String>();
                        children.add(curChildMap);
                        curChildMap.put(NAME, fileInformation.getName());
                        curChildMap.put(SIZE, "" + fileInformation.getSize()/1024 + " KB");
                        curChildMap.put(ID, fileInformation.getId().toString());
        			}
        			childData.add(children);
        		}
        		
        		mAdapter = new SimpleExpandableListAdapter(
        				self,
        				groupData,
        				android.R.layout.simple_expandable_list_item_1,
        				new String[] { NAME, USER_IP },
        				new int[] {android.R.id.text1, android.R.id.text2},
        				childData,
        				android.R.layout.simple_expandable_list_item_2,
        				new String[] { NAME, SIZE, ID },
        				new int[] {android.R.id.text1, android.R.id.text2, android.R.id.addToDictionary}
        				);
        		self.setListAdapter(mAdapter);
        		
        		self.getExpandableListView().setOnChildClickListener(self);
        		
        		Log.d(MSG_TAG, "update users list");
        	}
        }
    };
	
    
    
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		HLMPApplication application = (HLMPApplication)getApplicationContext();
		this.communication = application.getCommunication();
        this.filesManager = application.getFilesManager();
        this.communityFiles = filesManager.getCommunityFiles();
        this.filesManager.setCommunityFilesHandler(communityFilesHandler);
        this.self = this;
    }
	
	@Override
	public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
		Map<String, String> usersMap = groupData.get(groupPosition);
		Log.d(MSG_TAG, usersMap.get(NAME));
		Log.d(MSG_TAG, usersMap.get(USER_IP));
		
		List<Map<String, String>> filesList = childData.get(groupPosition);
		Map<String, String> fileMap = filesList.get(childPosition);
		Log.d(MSG_TAG, fileMap.get(NAME));
		
		
		UUID fileInformationId = UUID.fromString(fileMap.get(ID));
		InetAddress userInetAddress;
		try {
			userInetAddress = InetAddress.getByName(usersMap.get(USER_IP));
		} catch (UnknownHostException e) {
			return false;
		}
		
		NetUser netUser = communication.getNetUserList().getUser(userInetAddress);
		FileInformationList fileInformationlist = communityFiles.get(userInetAddress);
		FileInformation fileInformation = fileInformationlist.getFileInformation(fileInformationId);
		this.filesManager.sendFileRequest(netUser, fileInformation);
		
		return true;
    }
}
package android.HLMPConnect.FileTransfer;

import android.app.ExpandableListActivity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListAdapter;
import android.widget.SimpleExpandableListAdapter;
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


public class CommunityFilesActivity extends ExpandableListActivity {

	protected static final String MSG_TAG = "HLMP -> CommunityFilesActivity";
	
	private static final String NAME = "NAME";
	private static final String SIZE = "SIZE";
	public static final int UPDATE_USERS_LIST =	0;
	
	private CommunityFilesActivity self;
	private Communication communication;
	private FilesManager filesManager;
	private Hashtable<UUID, FileInformationList> communityFiles;
	private ExpandableListAdapter mAdapter;
	private List<Map<String, String>> groupData;
	private List<List<Map<String, String>>> childData; 
	
	
	private final Handler communityFilesHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
        	if (msg.what == UPDATE_USERS_LIST) {
        		groupData = new ArrayList<Map<String, String>>();
                childData = new ArrayList<List<Map<String, String>>>();
                
        		Set<UUID> userIds = communityFiles.keySet();
        		for (UUID userId : userIds){
        			Map<String, String> curGroupMap = new HashMap<String, String>();
                    groupData.add(curGroupMap);
                    String userName = "Unknown";
                    for (NetUser netUser : communication.getNetUserList().userListToArray()) {
                    	if (netUser.getId() == userId) {
                    		userName = netUser.getName(); 
                    		break;
                    	}
                    }
                    curGroupMap.put(NAME, userName);
                    
                    List<Map<String, String>> children = new ArrayList<Map<String, String>>();
        			for (FileInformation fileInformationList : communityFiles.get(userId).toArray()) {
        				Map<String, String> curChildMap = new HashMap<String, String>();
                        children.add(curChildMap);
                        curChildMap.put(NAME, fileInformationList.getName());
                        curChildMap.put(SIZE, "" + fileInformationList.getSize()/1024 + " KB");
        			}
        			childData.add(children);
        		}
        		
        		mAdapter = new SimpleExpandableListAdapter(
        				self,
        				groupData,
        				android.R.layout.simple_expandable_list_item_1,
        				new String[] { NAME },
        				new int[] { android.R.id.text1},
        				childData,
        				android.R.layout.simple_expandable_list_item_2,
        				new String[] { NAME, SIZE },
        				new int[] { android.R.id.text1, android.R.id.text2}
        				);
        		self.setListAdapter(mAdapter);
        		
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
}
package android.HLMPConnect.FileTransfer;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import android.app.ListActivity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.SimpleAdapter;

import android.HLMPConnect.FilesActivity;
import android.HLMPConnect.HLMPApplication;
import android.HLMPConnect.Managers.FilesManager;
import android.HLMPConnect.R;


public class DownloadFilesActivity extends ListActivity {
	
	private static final String MSG_TAG = "HLMP -> DownloadFilesActivity";

	private static final String FILENAME =	"FILENAME";
	private static final String SIZE = 		"SIZE";
	public static final int UPDATE_USERS_LIST = 0;
	
	private ArrayList<HashMap<String, String>> files;
	private SimpleAdapter adapter;
	private FilesManager fileManager;

	
	private final Handler downloadFilesHandler = new Handler() {
        @Override
        public synchronized void handleMessage(Message msg) {
        	if (msg.what == UPDATE_USERS_LIST) {
        		Log.d(MSG_TAG, "Update download list");
        		files.clear();
        		File sharedDir = getDir(FilesActivity.DOWNLOAD_DIR_NAME_SUFIX, MODE_WORLD_READABLE);
        		for (File file : sharedDir.listFiles()) {
                    HashMap<String, String> fileMap = new HashMap<String, String>();
                    fileMap.put(FILENAME, file.getName());
                    fileMap.put(SIZE, "" + file.length()/1024 + " KB");
                    files.add(fileMap);
                }
        		adapter.notifyDataSetChanged();
        	}
        }
	};
	
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        HLMPApplication application = (HLMPApplication)getApplicationContext();
    	this.fileManager = application.getFilesManager();
    	this.fileManager.setDownloadFilesHandler(this.downloadFilesHandler);

        files = new ArrayList<HashMap<String, String>>();
        
        File sharedDir = getDir(FilesActivity.DOWNLOAD_DIR_NAME_SUFIX, MODE_WORLD_READABLE);
		for (File file : sharedDir.listFiles()) {
            HashMap<String, String> fileMap = new HashMap<String, String>();
            fileMap.put(FILENAME, file.getName());
            fileMap.put(SIZE, "" + file.length()/1024 + " KB");
            files.add(fileMap);
        }
        
        this.adapter = new SimpleAdapter(
					this,
					files,
					R.layout.list_two_info_per_item,
					new String[] {FILENAME, SIZE},
					new int[] {R.id.text_1, R.id.text_2});
        this.setListAdapter(this.adapter);
    }
}
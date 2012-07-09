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

//import hlmp.SubProtocol.FileTransfer.FileInformation;
//import com.lamerman.FileDialog;
//import com.lamerman.SelectionMode;

import android.HLMPConnect.FilesActivity;
import android.HLMPConnect.HLMPApplication;
//import android.HLMPConnect.HLMPApplication;
import android.HLMPConnect.R;
//import android.HLMPConnect.Managers.FilesManager;
import android.HLMPConnect.Managers.FilesManager;


//public class SharedFilesActivity extends ListActivity implements OnClickListener {
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

//	public void onClick(View v) {
//		Intent intent = new Intent().setClass(this, FileDialog.class);
//		
//		File sharedDir = getDir(FilesActivity.SHARED_DIR_NAME_SUFIX, MODE_WORLD_READABLE);
//		
//		intent.putExtra(FileDialog.START_PATH, sharedDir.getAbsolutePath());
//        intent.putExtra(FileDialog.SELECTION_MODE, SelectionMode.MODE_OPEN);
//        
//        startActivityForResult(intent, 0);
//	}
//	
//	public synchronized void onActivityResult(final int requestCode, int resultCode, final Intent data) {
//		if (resultCode == Activity.RESULT_OK) {
//			Log.d(MSG_TAG, "sent FileList...");
//        	String filePath = data.getStringExtra(FileDialog.RESULT_PATH);
//        	
//        	File file = new File(filePath);
//        	FileInformation fileInformation = new FileInformation(file.getName(), file.length(), file.getAbsolutePath());
//        	
//        	this.fileManager.addFileInformationToFileData(fileInformation);
//        	this.fileManager.sendFileList();
//        	
//        	Log.d(MSG_TAG, "sent FileList... OK");
//        }
//    }
}
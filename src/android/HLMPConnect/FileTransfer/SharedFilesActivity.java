package android.HLMPConnect.FileTransfer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import hlmp.SubProtocol.FileTransfer.FileInformation;

import android.HLMPConnect.FilesActivity;
import android.HLMPConnect.HLMPApplication;
import android.HLMPConnect.Tabs;
import android.HLMPConnect.Managers.FilesManager;
import android.HLMPConnect.R;


public class SharedFilesActivity extends Activity implements OnClickListener, OnItemClickListener {
	
	private static final String MSG_TAG = "HLMP -> SharedFilesActivity";

	private static final String FILENAME =	"FILENAME";
	private static final String SIZE = 		"SIZE";
	private static final String ID = 		"ID";

	public static final int UPDATE_LIST	= 0;
	public static final int ADD_FILE	= 1;
	public static final int REMOVE_FILE	= 2;
	
	private HLMPApplication application;
	private Button addFile;
	private FilesManager fileManager;
	private FilesActivity filesActivity;
	private ArrayList<HashMap<String, String>> filesList;
	private SimpleAdapter adapter;
	
	

//	TODO: remove from fileData on FileTransferProtocol
	
	private final Handler sharedFilesHandler = new Handler(){
		@Override
		public synchronized void handleMessage(android.os.Message msg) {
			if (msg.what == ADD_FILE) {
				String filePath = (String)msg.obj;
				Log.d(MSG_TAG, "Adding to shared files: " + filePath);				
	        	File file = new File(filePath);
	        	if (!file.exists()) {
	        		Log.w(MSG_TAG, "file: " + filePath + " does not exist!");
	        		return;
	        	}
	        	
	        	Toast.makeText(filesActivity, file.getName() + " is shared !", Toast.LENGTH_SHORT).show();
	        	
	        	FileInformation fileInformation = new FileInformation(file.getName(), file.length(), file.getAbsolutePath());
	        	fileManager.addFileInformationToProtocol(fileInformation);
	        	fileManager.sendFileList();
	        	
	        	HashMap<String, String> fileMap = new HashMap<String, String>();
	            fileMap.put(FILENAME, file.getName());
	            fileMap.put(SIZE, "" + file.length()/1024 + " KB");
	            fileMap.put(ID, "" + fileInformation.getId().toString());
	            filesList.add(fileMap);
			}
			else if (msg.what == REMOVE_FILE) {
				String fileName = filesList.get(msg.arg1).get(FILENAME);
				UUID fileInformationId = UUID.fromString(filesList.get(msg.arg1).get(ID));
				filesList.remove(msg.arg1);
				Toast.makeText(filesActivity, fileName + " was removed from Shared Files!", Toast.LENGTH_SHORT).show();
				fileManager.removeFileInformationFromProtocol(fileInformationId);
	        	fileManager.sendFileList();
			}
			
			adapter.notifyDataSetChanged();
		}
	};


	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        this.application = (HLMPApplication)getApplicationContext();
        this.fileManager = this.application.getFilesManager();
        this.filesActivity = this.fileManager.getFilesActivity();
    	
    	this.setContentView(R.layout.shared_files);

    	this.addFile = (Button) this.findViewById(R.id.addFile);
        this.addFile.setOnClickListener(this);
        
        this.filesList = new ArrayList<HashMap<String, String>>();
        File sharedDir = getDir(FilesActivity.SHARED_DIR_NAME_SUFIX, MODE_WORLD_READABLE);
		for (File file : sharedDir.listFiles()) {
            HashMap<String, String> fileMap = new HashMap<String, String>();
            fileMap.put(FILENAME, file.getName());
            fileMap.put(SIZE, "" + file.length()/1024 + " KB");
            filesList.add(fileMap);
        }
        
        this.adapter = new SimpleAdapter(
					this,
					this.filesList,
					R.layout.list_two_info_per_item,
					new String[] {FILENAME, SIZE},
					new int[] {R.id.text_1, R.id.text_2});
        ListView fileListView = (ListView)findViewById(R.id.fileList);
        fileListView.setAdapter(this.adapter);
        fileListView.setOnItemClickListener(this);
		
		this.application.setSharedFilesHandler(this.sharedFilesHandler);
    }
	
	@Override
	protected void onResume() {
		super.onResume();
		this.sharedFilesHandler.obtainMessage(UPDATE_LIST).sendToTarget();
	}

	
	public void onClick(View view) {
		this.application.getTabHostHandler().obtainMessage(Tabs.PICK_UP_FILE).sendToTarget();
	}

	public void onItemClick(AdapterView<?> adapter, View view, final int position, long id) {

		final String fileName = filesList.get(position).get(FILENAME);
		final File sharedDir = getDir(FilesActivity.SHARED_DIR_NAME_SUFIX, MODE_WORLD_READABLE);
		final File fileOnSharedDir = new File(sharedDir, fileName);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(filesActivity);
	    builder.setMessage("Are you sure you want to remove " + fileName + " from Shared Files ?")
        .setCancelable(false)
        .setPositiveButton("Delete", new android.content.DialogInterface.OnClickListener() {
			
        	public void onClick(DialogInterface dialog, int arg1) {
        		if (fileOnSharedDir.exists()) {
        			try {
	        			fileOnSharedDir.getCanonicalFile().delete();
	        		} catch (IOException e) {}
        		}
        		sharedFilesHandler.obtainMessage(REMOVE_FILE, position, 0, null).sendToTarget();        		
			}
		})
        .setNegativeButton("Cancel", null);

        AlertDialog alert = builder.create();
        alert.show();
	}
}
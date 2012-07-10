package android.HLMPConnect.FileTransfer;

import java.util.ArrayList;
import java.util.HashMap;
import android.app.ListActivity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.SimpleAdapter.ViewBinder;

import android.HLMPConnect.HLMPApplication;
import android.HLMPConnect.R;
import android.HLMPConnect.Managers.FilesManager;


public class StateFilesActivity extends ListActivity {
	
	private static final String ID =	"ID";
	private static final String FILENAME =	"FILENAME";
	private static final String SIZE = 		"SIZE";
	private static final String PROGRESS = 	"PROGRESS";

	public static final int ADD_DOWNLOAD			= 0;
	public static final int ADD_UPLOAD				= 1;
	public static final int UPDATE_DOWNLOAD_PERCENT	= 2;
	public static final int UPDATE_UPLOAD_PERCENT	= 3;
	
	
	private ArrayList<HashMap<String, String>> adapterFiles;
	private HashMap<String,HashMap<String, String>> files;
	private SimpleAdapter adapter;
	private FilesManager fileManager;

	// TODO: add clear option
	
	private final Handler stateFilesHandler = new Handler() {
        @Override
        public synchronized void handleMessage(Message msg) {
        	if (msg.what == ADD_DOWNLOAD || msg.what == ADD_UPLOAD) {
        		String[] file_data = (String[]) msg.obj;
        		String fileHandlerId = file_data[0];
        		String fileName = file_data[1];
        		if (msg.what == ADD_DOWNLOAD) {
        			fileName = "(DOWNLOAD) " + fileName;
        		}
        		else {
        			fileName = "(TRANSFER) " + fileName;
        		}
        		String size = file_data[2];
        		long size_long = 0;
        		if (size != null) {
        			size_long = Long.valueOf(size).longValue();
        		}
        		size = "" + size_long/1024 + " KB";
        		String progress = "0";
        		
        		HashMap<String, String> fileMap = new HashMap<String, String>();
        		fileMap.put(ID, fileHandlerId);
        		fileMap.put(FILENAME, fileName);
				fileMap.put(SIZE, size);
				fileMap.put(PROGRESS, progress);
				
				files.put(fileHandlerId, fileMap);
        	}
        	else if (msg.what == UPDATE_DOWNLOAD_PERCENT || msg.what == UPDATE_UPLOAD_PERCENT) {
        		String fileHandlerId = (String) msg.obj;
        		String percent = "" + msg.arg1;
        		files.get(fileHandlerId).put(PROGRESS, percent);
        	}
        	
        	adapterFiles.clear();
        	for (HashMap<String, String> fileMap : files.values()) {
        		adapterFiles.add(fileMap);
    		}
        	adapter.notifyDataSetChanged();
        }
	};
	
	private final ViewBinder viewBinder = new ViewBinder() {
        public boolean setViewValue(View view, Object data, String textRepresentation) {
            if (view.getId() == R.id.downloadProgressBar) {
            	Integer theProgress =  Integer.decode((String)data);
            	((ProgressBar)view).setProgress(theProgress); 
            	return true;
            }
            return false;
        }
    };
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        HLMPApplication application = (HLMPApplication)getApplicationContext();
    	this.fileManager = application.getFilesManager();
    	
    	files = new HashMap<String, HashMap<String, String>>();
        adapterFiles = new ArrayList<HashMap<String, String>>();
    	
    	this.fileManager.setStateFilesHandler(this.stateFilesHandler);
    	
        this.adapter = new SimpleAdapter(
					this,
					adapterFiles,
					R.layout.download_state,
					new String[] {FILENAME, SIZE, PROGRESS},
					new int[] {R.id.file_name, R.id.size, R.id.downloadProgressBar});
        this.adapter.setViewBinder(viewBinder);
        this.setListAdapter(this.adapter);
    }
}
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
	
	public static final String ID =	"ID";
	public static final String FILENAME =	"FILENAME";
	public static final String SIZE = 		"SIZE";
	public static final String PROGRESS = 	"PROGRESS";

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
        	if (!adapter.isEmpty()) {
        		adapter.notifyDataSetChanged();
        	}
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
    	
    	files = this.fileManager.getStateFiles();
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
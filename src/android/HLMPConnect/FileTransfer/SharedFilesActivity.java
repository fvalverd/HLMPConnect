package android.HLMPConnect.FileTransfer;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import hlmp.SubProtocol.FileTransfer.FileInformation;
import com.lamerman.FileDialog;
import com.lamerman.SelectionMode;

import android.HLMPConnect.FilesActivity;
import android.HLMPConnect.HLMPApplication;
import android.HLMPConnect.R;
import android.HLMPConnect.Managers.FilesManager;


public class SharedFilesActivity extends Activity implements OnClickListener {
	
	private static final String MSG_TAG = "HLMP -> SharedFilesActivity";
	
	private Button addFile;
	private FilesManager fileManager;

	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.shared_files);
        
        HLMPApplication application = (HLMPApplication)getApplicationContext();
    	this.fileManager = application.getFilesManager();
        
        this.addFile = (Button) this.findViewById(R.id.addFile);
        this.addFile.setOnClickListener(this);
    }

	public void onClick(View v) {
		Intent intent = new Intent().setClass(this, FileDialog.class);
		
		File sharedDir = getDir(FilesActivity.SHARED_DIR_NAME_SUFIX, MODE_WORLD_READABLE);
		
		intent.putExtra(FileDialog.START_PATH, sharedDir.getAbsolutePath());
        intent.putExtra(FileDialog.SELECTION_MODE, SelectionMode.MODE_OPEN);
        
        startActivityForResult(intent, 0);
	}
	
	public synchronized void onActivityResult(final int requestCode, int resultCode, final Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			Log.d(MSG_TAG, "sent FileList...");
        	String filePath = data.getStringExtra(FileDialog.RESULT_PATH);
        	
        	File file = new File(filePath);
        	FileInformation fileInformation = new FileInformation(file.getName(), file.length(), file.getAbsolutePath());
        	
        	this.fileManager.addFileInformationToFileData(fileInformation);
        	this.fileManager.sendFileList();
        	
        	Log.d(MSG_TAG, "sent FileList... OK");
        }
    }
}
package android.HLMPConnect;

import android.app.AlertDialog;
import android.app.TabActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.widget.TabHost;

import android.HLMPConnect.FileTransfer.CommunityFilesActivity;
import android.HLMPConnect.FileTransfer.SharedFilesActivity;
import android.HLMPConnect.FileTransfer.TransferStateFilesActivity;
import android.HLMPConnect.Managers.FilesManager;


public class FilesActivity extends TabActivity implements OnClickListener {
	
	public static final String DOWNLOAD_DIR_NAME_SUFIX = "downloads";
	public static final String SHARED_DIR_NAME_SUFIX = "downloads";
	
	protected FilesManager filesManager;
	private TabHost tabHost;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.files);
        
        this.tabHost = this.getTabHost();
	    TabHost.TabSpec spec;
	    Intent intent;

	    intent = new Intent().setClass(this, SharedFilesActivity.class);
	    spec = this.tabHost.newTabSpec("shared").setIndicator("Shared"
//	                      ,res.getDrawable(R.drawable.ic_tab_shared)
	    		).setContent(intent);
	    this.tabHost.addTab(spec);

	    intent = new Intent().setClass(this, CommunityFilesActivity.class);
	    spec = this.tabHost.newTabSpec("community").setIndicator("Community"
//	                      ,res.getDrawable(R.drawable.ic_tab_community)
	    		).setContent(intent);
	    this.tabHost.addTab(spec);
	    
	    intent = new Intent().setClass(this, TransferStateFilesActivity.class);
	    spec = this.tabHost.newTabSpec("state").setIndicator("State"
//	                      ,res.getDrawable(R.drawable.ic_tab_state)
	    		).setContent(intent);
	    this.tabHost.addTab(spec);
        
        HLMPApplication application = (HLMPApplication)getApplicationContext();
        
        this.filesManager = application.getFilesManager();
    }
    
    @Override
	public void onBackPressed() {
	    AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    builder.setMessage("Are you sure you want to exit?")
        .setCancelable(false)
        .setPositiveButton("YES", this)
        .setNegativeButton("NO", this);

        AlertDialog alert = builder.create();
        alert.show();
	}
    
    public void onClick(DialogInterface dialog, int which) {
		if (DialogInterface.BUTTON_POSITIVE == which) {
			super.onBackPressed();
		}
		else if (DialogInterface.BUTTON_NEGATIVE == which) {
			dialog.cancel();
		}
	}

}
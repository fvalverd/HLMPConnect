package android.HLMPConnect;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import hlmp.CommLayer.Communication;
import hlmp.CommLayer.NetUser;

import android.HLMPConnect.Managers.UsersManager;

public class UsersActivity extends Activity implements OnClickListener {
    
    public static final int ADD_USER = 0;
    public static final int REFRESH_USER = 1;
	public static final int REFRESH_LOCAL_USERS = 2;
	public static final int REMOVE_USER = 3;
    
	private Communication communication;
	
	protected UsersManager userManagers;
	protected ArrayAdapter<String> users;

	private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
        	if (msg.what != REFRESH_LOCAL_USERS) {
	        	users.clear();
				for(NetUser netUser: communication.getNetUserList().userListToArray()) {
					users.add(netUser.getName());
				}
        	}
        }
    };
    
	
	@SuppressWarnings("unchecked")
	@Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	
        HLMPApplication application = (HLMPApplication)getApplicationContext();
        this.userManagers = application.getUsersManager();
        this.communication = application.getCommunication();
        
        
        this.setContentView(R.layout.users);
        
        ListView userList = (ListView)findViewById(R.id.userList);
        userList.setTextFilterEnabled(true);
        this.users = (ArrayAdapter<String>) (this.getLastNonConfigurationInstance());
        if (this.users == null) {
        	this.users = new ArrayAdapter<String>(this, R.layout.list_item);
        }
        userList.setAdapter(this.users);
        
        this.userManagers.setHandler(mHandler);
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
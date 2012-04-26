package android.HLMPConnect;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import hlmp.CommLayer.Communication;
import hlmp.CommLayer.NetUser;

import android.HLMPConnect.Managers.UsersManager;

public class UsersActivity extends Activity {
    
    public static final int ADD_USER = 0;
    public static final int REFRESH_USER = 1;
	public static final int REFRESH_LOCAL_USERS = 2;
    
	private Communication communication;
	
	protected UsersManager userManagers;
	protected ArrayAdapter<String> users;

	private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
        	switch (msg.what) {
        		case ADD_USER :  {
        			users.clear();
        			for(NetUser netUser: communication.getNetUserList().userListToArray()) {
                		users.add(netUser.getName());
            		}
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
//        this.userManager.setUserArrayAdapter(this.users);
        
        this.userManagers.setHandler(mHandler);
        if (this.userManagers.isDaemon()) {
        	this.userManagers.start();
        }
    }
}
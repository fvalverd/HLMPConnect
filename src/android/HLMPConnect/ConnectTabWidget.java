package android.HLMPConnect;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;

public class ConnectTabWidget extends TabActivity {
	
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.main);

//	    Resources res = getResources(); // Resource object to get Drawables
	    TabHost tabHost = getTabHost();  // The activity TabHost
	    TabHost.TabSpec spec;  // Resusable TabSpec for each tab
	    Intent intent;  // Reusable Intent for each tab

	    // Create an Intent to launch an Activity for the tab (to be reused)
	    intent = new Intent().setClass(this, ConnectionsActivity.class);

	    // Initialize a TabSpec for each tab and add it to the TabHost
	    spec = tabHost.newTabSpec("connections").setIndicator("Connections"
//	                      ,res.getDrawable(R.drawable.ic_tab_connections)
	    		)
	                  .setContent(intent);
	    tabHost.addTab(spec);

	    // Do the same for the other tabs
	    intent = new Intent().setClass(this, UsersActivity.class);
	    spec = tabHost.newTabSpec("users").setIndicator("Users"
//	                      ,res.getDrawable(R.drawable.ic_tab_users)
	    		)
	                  .setContent(intent);
	    tabHost.addTab(spec);

	    intent = new Intent().setClass(this, ChatActivity.class);
	    spec = tabHost.newTabSpec("chat").setIndicator("Chat"
//	                      ,res.getDrawable(R.drawable.ic_tab_transfers)
	    		)
	                  .setContent(intent);
	    tabHost.addTab(spec);

//	    tabHost.setCurrentTab(2);
	}

}

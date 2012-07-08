package android.HLMPConnect;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TabHost;


public class Tabs extends TabActivity {
	public static final int ACTIVE = 0;
	public static final int INACTIVE = 1;
	
	private static int TAB_USERS = 1;
	private static int TAB_CHAT = 2;
	private static int TAB_FILES = 3;
	
	private TabHost tabHost;
	
	final Handler tabHostHandler = new Handler() {
		@Override
		public void handleMessage(android.os.Message msg) {
			if (msg.what == ACTIVE) {
				tabHost.getTabWidget().getChildTabViewAt(TAB_USERS).setEnabled(true);
			    tabHost.getTabWidget().getChildTabViewAt(TAB_CHAT).setEnabled(true);
			    tabHost.getTabWidget().getChildTabViewAt(TAB_FILES).setEnabled(true);
			}
			else if (msg.what == INACTIVE) {
				tabHost.getTabWidget().getChildTabViewAt(TAB_USERS).setEnabled(false);
			    tabHost.getTabWidget().getChildTabViewAt(TAB_CHAT).setEnabled(false);
			    tabHost.getTabWidget().getChildTabViewAt(TAB_FILES).setEnabled(false);
			}
		};
    };
	
    
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.main);
	    
	    HLMPApplication application = (HLMPApplication) this.getApplicationContext();
		
//	    Resources res = getResources(); // Resource object to get Drawables
	    this.tabHost = this.getTabHost();
	    application.setTabHostHandler(tabHostHandler);
	    TabHost.TabSpec spec;
	    Intent intent;

	    intent = new Intent().setClass(this, ConnectionActivity.class);
	    spec = tabHost.newTabSpec("connection").setIndicator("Connection"
//	                      ,res.getDrawable(R.drawable.ic_tab_connections)
	    		).setContent(intent);
	    tabHost.addTab(spec);

	    intent = new Intent().setClass(this, UsersActivity.class);
	    spec = tabHost.newTabSpec("users").setIndicator("Users"
//	                      ,res.getDrawable(R.drawable.ic_tab_users)
	    		).setContent(intent);
	    tabHost.addTab(spec);

	    intent = new Intent().setClass(this, ChatActivity.class);
	    spec = tabHost.newTabSpec("chat").setIndicator("Chat"
//	                      ,res.getDrawable(R.drawable.ic_tab_chat)
	    		).setContent(intent);
	    tabHost.addTab(spec);
	    
	    intent = new Intent().setClass(this, FilesActivity.class);
	    spec = tabHost.newTabSpec("files").setIndicator("Files"
//	                      ,res.getDrawable(R.drawable.ic_tab_files)
	    		).setContent(intent);
	    tabHost.addTab(spec);
	    
	    if (!application.isRunning()) {
		    tabHost.getTabWidget().getChildTabViewAt(TAB_USERS).setEnabled(false);
		    tabHost.getTabWidget().getChildTabViewAt(TAB_CHAT).setEnabled(false);
		    tabHost.getTabWidget().getChildTabViewAt(TAB_FILES).setEnabled(false);
	    }
	}
}
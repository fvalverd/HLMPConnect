package android.HLMPConnect;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import java.util.ArrayList;
import java.util.HashMap;

import android.HLMPConnect.Managers.ChatManager;


public class ChatActivity extends Activity implements OnKeyListener, android.content.DialogInterface.OnClickListener, android.view.View.OnClickListener {
	
	public static final int GLOBAL_MESSAGE = 0;
	
	private static final String USERNAME	= "USERNAME";
	private static final String MESSAGE		= "MESSAGE";
	
	protected ChatManager chatManager;
	protected EditText message;
	protected Button send;
	protected ArrayList<HashMap<String, String>> messages;
	protected SimpleAdapter adapter;
	
	private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
        	switch (msg.what) {
	        	case GLOBAL_MESSAGE: {
		        	String[] message_data = (String[]) msg.obj;
		        	String userName = message_data[0]; 
		        	String messageText = message_data[1];
		        	
		        	HashMap<String, String> messageMap = new HashMap<String, String>();
		        	messageMap.put(USERNAME, userName);
		        	messageMap.put(MESSAGE, messageText);
		        	messages.add(messageMap);
		        	
		        	adapter.notifyDataSetChanged();
	            }
        	}
        }
    };
	

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        HLMPApplication application = (HLMPApplication)getApplicationContext();
        this.chatManager = application.getChatManager();
        
        
        this.setContentView(R.layout.chat);
        
        this.send = (Button)findViewById(R.id.send);
        this.send.setOnClickListener(this);
        this.message = (EditText)findViewById(R.id.msg);
        this.message.setOnKeyListener(this);
        
        
        this.messages = new ArrayList<HashMap<String, String>>();
        this.adapter = new SimpleAdapter( 
                this, 
                this.messages,
                R.layout.list_two_info_per_item,
                new String[] {USERNAME, MESSAGE},
                new int[] {R.id.text_1, R.id.text_2});
        ListView msgListView = (ListView)findViewById(R.id.msgList);
        msgListView.setAdapter(this.adapter);
        
        
        this.chatManager.setHandler(mHandler);
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
    
    
    public void sendMessage() {
    	String text = this.message.getText().toString();
    	this.chatManager.sendMessage(text);
		this.message.setText("");
		InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
	    imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
    }
    
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		if ((event.getAction() == KeyEvent.ACTION_UP) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
			this.sendMessage();
			return true;
		}
		return false;
	}

	public void onClick(View v) {
		this.sendMessage();
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
package android.HLMPConnect;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import android.HLMPConnect.Managers.ChatManager;

public class ChatActivity extends Activity implements OnClickListener, OnKeyListener {
	
	public static final int GLOBAL_MESSAGE = 0;

	protected ChatManager chatManager;
	
	protected EditText message;
	protected Button send;
	protected ArrayAdapter<String> messages;	
	
	private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
        	switch (msg.what) {
	            case GLOBAL_MESSAGE: {
		        	String incomingMessage = (String) msg.obj;
		        	messages.add(incomingMessage);
	            }
        	}
        }
    };

    @SuppressWarnings("unchecked")
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        HLMPApplication application = (HLMPApplication)getApplicationContext();
        this.chatManager = application.getChatManager();
        
        
        this.setContentView(R.layout.chat);
        
        this.send = (Button)findViewById(R.id.send);
        this.send.setOnClickListener(this);
        
        ListView msgList = (ListView)findViewById(R.id.msgList);
        msgList.setTextFilterEnabled(true);
        this.messages = (ArrayAdapter<String>) (this.getLastNonConfigurationInstance());
        if (this.messages == null) {
        	this.messages = new ArrayAdapter<String>(this, R.layout.list_item);
        }
        msgList.setAdapter(this.messages);
        this.chatManager.setMessageArrayAdapter(this.messages);
        
        this.message = (EditText)findViewById(R.id.msg);
        this.message.setOnKeyListener(this);
        
        this.chatManager.setHandler(mHandler);
        
        if (!this.chatManager.isAlive()) {
        	this.chatManager.start();
        }
    }
    
    
    @Override
    public Object onRetainNonConfigurationInstance() {
        return this.messages;
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
}
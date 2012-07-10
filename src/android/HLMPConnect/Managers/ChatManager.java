package android.HLMPConnect.Managers;

import java.util.ArrayList;
import java.util.HashMap;

import android.HLMPConnect.ChatActivity;
import android.os.Handler;

import hlmp.CommLayer.NetUser;
import hlmp.SubProtocol.Chat.ChatProtocol;
import hlmp.SubProtocol.Chat.ControlI.ChatHandlerI;
import hlmp.SubProtocol.Chat.Messages.GroupChatMessage;

public class ChatManager implements ChatHandlerI {

	protected final String MSG_TAG = "HLMP -> ChatManager";
	
	protected Handler mHandler;
	protected ChatProtocol chatProtocol;
	protected NetUser netUser;
	protected ArrayList<HashMap<String, String>> globalMessages;
	
	public ChatManager() {
		this.globalMessages = new ArrayList<HashMap<String, String>>();
	}
	
	public void chatMessageReceived(NetUser netUser, String message) {
		// TODO Auto-generated method stub
		
	}

	public void groupChatMessageReceived(NetUser netUser, String text) {
		this.showMessage(netUser, text);
	}

	public void chatWarninglInformation(String text) {
		// TODO Auto-generated method stub
		
	}

	public void showMessage(NetUser netUser, String message) {
		HashMap<String, String> messageMap = new HashMap<String, String>();
    	messageMap.put(ChatActivity.USERNAME, netUser.getName());
    	messageMap.put(ChatActivity.MESSAGE, message);
    	globalMessages.add(messageMap);
    	
		if (this.mHandler != null ) {
			this.mHandler.obtainMessage(ChatActivity.GLOBAL_MESSAGE).sendToTarget();
		}
	}

	public void sendMessage(String text) {
		this.chatProtocol.sendMessageEvent(new GroupChatMessage(text));
		this.showMessage(this.netUser, text);
	}

	public void setChatProtocol(ChatProtocol chatProtocol) {
		this.chatProtocol = chatProtocol;
	}

	public void setNetUser(NetUser netUser) {
		this.netUser = netUser;		
	}

	public void setHandler(Handler mHandler) {
		this.mHandler = mHandler;
	}

	
	public ArrayList<HashMap<String, String>> getGlobalMessages() {
		return this.globalMessages; 
	}
}
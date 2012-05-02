package android.HLMPConnect.Managers;

import android.HLMPConnect.ChatActivity;
import android.os.Handler;
import android.widget.ArrayAdapter;

import hlmp.CommLayer.NetUser;
import hlmp.SubProtocol.Chat.ChatProtocol;
import hlmp.SubProtocol.Chat.ControlI.ChatHandlerI;
import hlmp.SubProtocol.Chat.Messages.GroupChatMessage;

public class ChatManager extends Thread implements ChatHandlerI {

	protected Handler mHandler;
	protected ArrayAdapter<String> messages;
	protected ChatProtocol chatProtocol;
	protected NetUser netUser;

	protected final String MSG_TAG = "HLMP -> ChatManager";
	
	
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
		String message_format = "[ %s ]: %s";
		message_format = String.format(message_format, netUser.getName(), message);
		if (this.mHandler != null ) {
			this.mHandler.obtainMessage(ChatActivity.GLOBAL_MESSAGE, message_format).sendToTarget();
		}
	}

	public void setMessageArrayAdapter(ArrayAdapter<String> messages) {
		this.messages = messages;
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
}
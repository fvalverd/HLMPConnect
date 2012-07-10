package android.HLMPConnect.Managers;

import android.HLMPConnect.ChatActivity;
import android.os.Handler;

import hlmp.CommLayer.NetUser;
import hlmp.SubProtocol.Chat.ChatProtocol;
import hlmp.SubProtocol.Chat.ControlI.ChatHandlerI;
import hlmp.SubProtocol.Chat.Messages.GroupChatMessage;

public class ChatManager implements ChatHandlerI {

	protected Handler mHandler;
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
		if (this.mHandler != null ) {
			this.mHandler.obtainMessage(ChatActivity.GLOBAL_MESSAGE,
					new String[] {netUser.getName(), message}).sendToTarget();
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
}
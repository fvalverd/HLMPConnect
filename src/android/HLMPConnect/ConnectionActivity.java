package android.HLMPConnect;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import android.adhoc.AdHocApp;
import android.adhoc.basic.OnOffActivity;
import android.HLMPConnect.HLMPApplication;


public class ConnectionActivity extends OnOffActivity implements OnClickListener {

	public static final int DLG_HLMP_STARTING = 10;
	public static final int DLG_HLMP_STOPPING = 11;

	
	public void onDestroy() {
		Log.i("ConnectionsActivity", "ConnectionsActivity distroying...");
		HLMPApplication application = (HLMPApplication)getApplicationContext();
		application.stopHLMP();
		super.onDestroy();
		Log.i("ConnectionsActivity", "ConnectionsActivity distroying... OK!");
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
	
	@Override
    protected Dialog onCreateDialog(int id) {
		Dialog dialog = super.onCreateDialog(id);
		if (dialog == null) {
			if(id == DLG_HLMP_STARTING) {
				ProgressDialog progressDialog = new ProgressDialog(this);
		    	progressDialog.setTitle(this.getString(R.string.hlmpStarting));
		    	progressDialog.setMessage(this.getString(R.string.hlmpStartingMessage));
		    	progressDialog.setIndeterminate(false);
		    	progressDialog.setCancelable(true);
		        return progressDialog;
			}
			else if(id == DLG_HLMP_STOPPING) {
				ProgressDialog progressDialog = new ProgressDialog(this);
		    	progressDialog.setTitle(this.getString(R.string.hlmpStopping));
		    	progressDialog.setMessage(this.getString(R.string.hlmpStoppingMessage));
		    	progressDialog.setIndeterminate(false);
		    	progressDialog.setCancelable(true);
		        return progressDialog;
			}
		}
		return dialog; 
	}
	
	
	@Override
    protected void requestStartAdHoc() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
		final HLMPApplication application = (HLMPApplication)getApplicationContext();
		final View layout = inflater.inflate(R.layout.connection, (ViewGroup) findViewById(R.id.layout_root));
		EditText username_input = (EditText) layout.findViewById(R.id.username_input);
		
		String username = "";
		username = android.os.Build.MODEL + "_" + android.os.Build.SERIAL; // from API 9
//		username = android.os.Build.MODEL; // API 7
		username = username.replaceAll("\\s","");
		username_input.setText(username);
		
		builder.setView(layout)
		.setPositiveButton("Connect", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				String username = ((EditText) layout.findViewById(R.id.username_input)).getText().toString();
				application.startHLMP(username);
			}
		})
		.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				application.requestUpdateAdHocActivity();
			}
		})
		.create().show();
	}
	
	@Override
	protected void requestStopAdHoc() {
		HLMPApplication application = (HLMPApplication)this.getApplicationContext();
		application.stopHLMP();
	}

	public void onClick(DialogInterface dialog, int which) {
		if (DialogInterface.BUTTON_POSITIVE == which) {
			
			NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
			notificationManager.cancel(AdHocApp.NOTIFY_ERROR);
			super.onBackPressed();
		}
		else if (DialogInterface.BUTTON_NEGATIVE == which) {
			dialog.cancel();
		}
	}
}
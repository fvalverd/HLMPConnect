package android.HLMPConnect;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import android.adhoc.basic.OnOffActivity;
import android.HLMPConnect.HLMPApplication;


public class ConnectionsActivity extends OnOffActivity implements OnClickListener {

	@Override
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
    protected void requestStartAdHoc() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
		final HLMPApplication application = (HLMPApplication)getApplicationContext();
		final View layout = inflater.inflate(R.layout.connections, (ViewGroup) findViewById(R.id.layout_root));
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
			super.onBackPressed();
		}
		else if (DialogInterface.BUTTON_NEGATIVE == which) {
			dialog.cancel();
		}
	}
}
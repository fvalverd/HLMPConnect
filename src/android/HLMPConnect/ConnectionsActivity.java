package android.HLMPConnect;

import android.HLMPConnect.HLMPApplication;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;


public class ConnectionsActivity extends ListActivity {
	
	static final String CREATE_NEW_CONNECTION = "Create new MANET";
	static String[] connections = new String[] {CREATE_NEW_CONNECTION};
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setListAdapter(new ArrayAdapter<String>(this, R.layout.list_item, connections));
        ListView lv = getListView();
        lv.setTextFilterEnabled(true);

        lv.setOnItemClickListener(new OnItemClickListener() {
        	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        		final View layout;
        		AlertDialog.Builder builder;
    			AlertDialog alertDialog;
        		Context dialog_context = parent.getContext();
        		
        		LayoutInflater inflater = (LayoutInflater) dialog_context.getSystemService(LAYOUT_INFLATER_SERVICE);
    			layout = inflater.inflate(R.layout.connections, (ViewGroup) findViewById(R.id.layout_root));
        		
    			if(connections[position] == CREATE_NEW_CONNECTION) {
        			builder = new AlertDialog.Builder(dialog_context);
        			builder.setView(layout);
        			
        			builder.setPositiveButton("Connect", new DialogInterface.OnClickListener() {
        				public void onClick(DialogInterface dialog, int whichButton) {
        					String ssid = ((EditText) layout.findViewById(R.id.ssid_input)).getText().toString();
        					String username = ((EditText) layout.findViewById(R.id.username_input)).getText().toString();
        					String ip = ((EditText) layout.findViewById(R.id.ip_input)).getText().toString();
        					
        					HLMPApplication application = (HLMPApplication)getApplicationContext();
//        					application.stopAdHoc();
        					application.startAdHocWithIpAndUsername(ip, username);
        					
        					String values = "SSID: %s, Username: %s, IP: %s";
        					values = String.format(values, ssid, username, ip);
        					Toast.makeText(getApplicationContext(), values, Toast.LENGTH_SHORT).show();
        				}
        			});
        			

        			builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
        				public void onClick(DialogInterface dialog, int whichButton) {
        				}
        			});
        			
        			alertDialog = builder.create();
        			alertDialog.show();
        		}
        		else {
        		}
        	}
        });
	}
}
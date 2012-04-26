package android.HLMPConnect.system;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;

import android.util.Log;

public class Native {
	
	public static final String MSG_TAG = "HLMP -> Native";

	static public boolean chmod(String file, String mode) {
		String command = "chmod %s %s";
		command = String.format(command, mode, file);
		if (NativeTask.runCommand(command) == 0) {
			return true;
		}
		return false;
	}

	static public String copyFile(String filename, String permission, InputStream is) {
		String result = copyFile(filename, is);
		if (result != null) {
			return result;
		}
		if (chmod(filename, permission) != true) {
			result = "Can't change file-permission for '"+filename+"'!";
		}
		return result;
	}

	static protected String copyFile(String filename, InputStream is) {
		File outFile = new File(filename);
		Log.d(MSG_TAG, "Copying file '"+filename+"' ...");
		byte buf[] = new byte[1024];
		int len;
		try {
			OutputStream out = new FileOutputStream(outFile);
			while((len = is.read(buf))>0) {
				out.write(buf,0,len);
			}
			out.close();
			is.close();
		} catch (IOException e) {
			Log.d(MSG_TAG, "Couldn't install file '"+filename+"' ...");
			return "Couldn't install file - "+filename+"!";
		}
		return null;
	}

	static protected ArrayList<String> readLinesFromFile(String filename) {
    	String line = null;
    	BufferedReader br = null;
    	InputStream ins = null;
    	ArrayList<String> lines = new ArrayList<String>();
    	File file = new File(filename);
    	if (file.canRead() == false)
    		return lines;
    	try {
    		ins = new FileInputStream(file);
    		br = new BufferedReader(new InputStreamReader(ins), 8192);
    		while((line = br.readLine())!=null) {
    			lines.add(line.trim());
    		}
    	} catch (Exception e) {
    		Log.d(MSG_TAG, "Unexpected error - Here is what I know: "+e.getMessage());
    	}
    	finally {
    		try {
    			ins.close();
    			br.close();
    		} catch (Exception e) {
    			// Nothing.
    		}
    	}
    	return lines;
    }
	
	static protected boolean writeLinesToFile(String filename, String lines) {
		OutputStream out = null;
		boolean returnStatus = false;
		Log.d(MSG_TAG, "Writing " + lines.length() + " bytes to file: " + filename);
		try {
			out = new FileOutputStream(filename);
        	out.write(lines.getBytes());
        	out.flush();
		} catch (Exception e) {
			Log.d(MSG_TAG, "Unexpected error - Here is what I know: "+e.getMessage());
		}
		finally {
        	try {
        		if (out != null)
        			out.close();
        		returnStatus = true;
			} catch (IOException e) {
				returnStatus = false;
			}
		}
		return returnStatus;
    }
	
	static public synchronized void updateDnsmasqFilepath(String dnsmasqConf, String data_application_path) {
    	String newDnsmasq = new String();
    	boolean writeconfig = false;
    	
    	ArrayList<String> lines = readLinesFromFile(dnsmasqConf);
    	
    	for (String line : lines) {
    		if (line.contains("dhcp-leasefile=") && !line.contains(data_application_path)){
    			line = "dhcp-leasefile="+data_application_path+"/var/dnsmasq.leases";
    			writeconfig = true;
    		}
    		else if (line.contains("pid-file=") && !line.contains(data_application_path)){
    			line = "pid-file="+data_application_path+"/var/dnsmasq.pid";
    			writeconfig = true;
    		}
    		newDnsmasq += line+"\n";
    	}

    	if (writeconfig == true)
    		writeLinesToFile(dnsmasqConf, newDnsmasq);
    }

	public static void checkDirs(String data_application_path) {
    	File dir = new File(data_application_path);
    	if (dir.exists() == false) {
    			Log.d(MSG_TAG, "Directory '"+dir.getAbsolutePath()+"' does not exist!");
    	}
    	else {
    		String[] dirs = { "/bin", "/var", "/conf" };
    		for (String dirname : dirs) {
    			dir = new File(data_application_path + dirname);
    	    	if (dir.exists() == false) {
    	    		if (!dir.mkdir()) {
    	    			Log.d(MSG_TAG, "Couldn't create '"+dir.getAbsolutePath()+"' directory!");
    	    		}
    	    	}
    	    	else {
    	    		Log.d(MSG_TAG, "Directory '"+dir.getAbsolutePath()+"' already exists!");
    	    	}
    		}
    	}
    }

	
	public static boolean runRootCommand(String command) {
		Log.d(MSG_TAG, "Root-Command ==> su -c \""+command+"\"");
		int returncode = NativeTask.runCommand("su -c \""+command+"\"");
    	if (returncode == 0) {
			return true;
		}
    	Log.d(MSG_TAG, "Root-Command error, return code: " + returncode);
		return false;
	}

}

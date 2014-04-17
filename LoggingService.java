package iis.ds.processlogging;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

public class LoggingService extends Service {
	private Handler handler = new Handler();
  
	@Override
	public void onCreate() {
		super.onCreate();		
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		Log.d("LoggingService", "Service Start!");
	    
		//android.os.Debug.waitForDebugger();

		handler.postDelayed(logProcess, 1000);
			
	    // want this service to continue running until it is explicitly
	    // stopped, so return sticky.
	    return START_STICKY;
	}

	@Override
    public void onDestroy() {
		
		Log.d("LoggingService", "Service Stop!");

		handler.removeCallbacks(logProcess);		
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	
	final Runnable logProcess = new Runnable() {
		@Override
		public void run() {
			
		    //android.os.Debug.waitForDebugger();
			
			Intent retIntent = new Intent("logging-event");
			String result;
			
			// get current time first
			result = CmdExec("date");		
						
			if(!result.equals("")){
				Log.i("logging",result);
				
				result = "--" + result;
				// write to file
				recordResult(result);
				
				// put results into bundle
				retIntent.putExtra("time", result);
			}
			else{
				Log.i("logging","NO_RESULT");								
				retIntent.putExtra("time", "Error!");				
			}
			
			// execute pidstat
			result = CmdExec("/data/data/iis.ds.processlogging/lib/libpidstat.so");
			
			if(!result.equals("")){
				// print result to Log
				//Log.i("logging",result);
				
				// write to file
				recordResult(result);
				
				// put result into bundle
				retIntent.putExtra("procInfo", result);
			}
			else{
				//Log.i("logging","NO_RESULT");
				retIntent.putExtra("procInfo", "Error!");				
			}
			
						
			// send result back to main activity
			sendBroadcast(retIntent);
			
			handler.postDelayed(logProcess, 1000);
		}
	};
	
	private String CmdExec(String cmd){
		
		String result = "";			
		
		// execute command, and retrieve result from buffer 
		try{
			Process p = Runtime.getRuntime().exec(cmd);
			BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line = null;
			while((line = in.readLine()) != null){
				result += line + "\n";
			};
			
			in.close();					
		}
		catch(IOException e){
			// TODO Auto-generated catch block  
            e.printStackTrace();				
		}
		
		return result;
	}
	
	
	private void recordResult(String input) {
		//TODO
		FileOutputStream fOut = null;
		
		// check if there is SDcard
		String targetFile;
		if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
			// write to SDcard
			targetFile = Environment.getExternalStorageDirectory().getPath();
		}
		else{
			targetFile = Environment.getRootDirectory().getPath();
		}
		targetFile += "/processlog.txt";
		
		try{
			fOut = new FileOutputStream(targetFile, true);
			//fOut = openFileOutput(targetFile, MODE_APPEND);
			OutputStreamWriter osw = new OutputStreamWriter(fOut);
		    osw.write(input);
		    osw.flush();
		    osw.close();
		}
		catch(FileNotFoundException e){
			// TODO
		}
		catch(IOException e){
			// TODO
			e.printStackTrace();
		}		
	}

}

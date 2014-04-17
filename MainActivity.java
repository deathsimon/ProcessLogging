package iis.ds.processlogging;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {
	
	private Button SwitchButton;
	private TextView infoText;
	private Boolean ServiceAlive = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_main);
		
		// start/stop button
		SwitchButton = (Button) findViewById(R.id.SBtn1);
		SwitchButton.setOnClickListener(startClickListener);
		// show results
		infoText = (TextView) findViewById(R.id.infoText);		
	}
	
	protected void onResume(){
		super.onResume();
		// broadcast listener, for receiving results from service    
		this.registerReceiver(mMessageReceiver, new IntentFilter("logging-event"));
 
		// check if service is running
		if(!isMyServiceRunning()){
			SwitchButton.setText(String.valueOf("Start"));			
			ServiceAlive = false;
		}
		else{
			SwitchButton.setText(String.valueOf("Stop"));
			ServiceAlive = true;
		}		
	}
		
	private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
		  @Override
		  public void onReceive(Context context, Intent intent) {
		    // Extract data included in the Intent
		    String message = ""; 
		    message = intent.getStringExtra("time") + "\n";
		    message += (intent.getStringExtra("procInfo") + "\n");
		    infoText.setText(message);
		  }
	};

	@Override
	protected void onPause() {
		// Unregister since the activity is not visible
		this.unregisterReceiver(mMessageReceiver);
		super.onPause();
	} 

	private Button.OnClickListener startClickListener = new Button.OnClickListener() {
		public void onClick(View arg0) {
			Intent intent = new Intent(MainActivity.this, LoggingService.class);			
			if(!ServiceAlive){
				startService(intent);
				ServiceAlive = true;
				SwitchButton.setText(String.valueOf("Stop"));
			}
			else{
				stopService(intent);
				ServiceAlive = false;
				SwitchButton.setText(String.valueOf("Start"));
			}
		}
	};
	
	private boolean isMyServiceRunning() {
		ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if ("iis.ds.processlogging.LoggingService".equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}

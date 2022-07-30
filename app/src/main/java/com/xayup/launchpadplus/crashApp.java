package com.xayup.launchpadplus;
import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.os.Bundle;
import android.service.autofill.OnClickAction;
import android.text.ClipboardManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class crashApp extends Activity {
	@Override
	protected void onCreate(Bundle savedBundle){
		super.onCreate(savedBundle);
		setContentView(R.layout.crash);
		TextView logView = findViewById(R.id.logText);
		
		
	}
}
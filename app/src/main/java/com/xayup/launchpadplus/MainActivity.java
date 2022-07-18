package com.xayup.launchpadplus;

import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.os.*;
import android.util.*;
import android.view.*;
import android.widget.*;
import java.io.*;


public class MainActivity extends Activity
{
	String[] pastadeprojetos;
	ListView listaprojetos;
	File info;

	DisplayMetrics display = new DisplayMetrics();
	int height;
	File rootFolder = new File(Environment.getExternalStorageDirectory() + "/LaunchpadPlus/Projects");
	final String[] per = new String[]{"android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.READ_EXTERNAL_STORAGE"};
	final int STORAGE_PERMISSION = 1000;
	@Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
				setContentView(R.layout.main);
				getWindowManager().getDefaultDisplay().getMetrics(display);
				if(display.heightPixels < display.widthPixels){
					height = display.heightPixels;
				} else{
					height = display.widthPixels;
				}
				checarPermissao();
	}

	public void makeActivity(boolean granted)
	{
		if(granted){
			if(!rootFolder.exists()){
				rootFolder.mkdirs();
			}
		}
		Readers getInfo = new Readers();
		customArray arrayCustom = new customArray(MainActivity.this, getInfo.readInfo(rootFolder, granted));
	    listaprojetos = findViewById(R.id.listViewProjects);
		listaprojetos.setAdapter(arrayCustom);
		listaprojetos.setOnItemClickListener(new AdapterView.OnItemClickListener(){
				@Override
				public void onItemClick(AdapterView<?> adapterView, View view, int Int, long Long)
				{
					TextView pathTextv = view.findViewById(R.id.pathText);
					ImageView itemStt = view.findViewById(R.id.currentItemState);
					switch ((Integer)itemStt.getTag())
					{
						case 0:
							Intent playPads = new Intent(getBaseContext(), playPads.class);
							playPads.putExtra("currentPath", pathTextv.getText().toString());
							playPads.putExtra ("height", height);
							startActivity(playPads);
							break;
						case 2:
							checarPermissao();
							break;
					}
				}
			});
	}
	public void checarPermissao()
	{
			if ((checkCallingPermission(per[0]) & checkCallingPermission(per[1])) != PackageManager.PERMISSION_GRANTED)
		{
			requestPermissions(per, STORAGE_PERMISSION);
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
	{
		if (STORAGE_PERMISSION == requestCode)
		{
			if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
			{
				makeActivity(true);
			}
			else
			{
				makeActivity(false);
			}
		}
	}
}

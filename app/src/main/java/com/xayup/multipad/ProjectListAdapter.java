package com.xayup.multipad;
import android.content.*;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.*;
import android.widget.*;

import java.util.*;

public class ProjectListAdapter extends BaseAdapter
{
	public static final String KEY_CHAINS = "chains";
	public static final String KEY_TITLE = "title";
	public static final String KEY_PRODUCER_NAME = "producerName";
	public static final String KEY_TYPE = "type", TYPE_GOOD = "good", TYPE_BAD = "bad", TYPE_CONTROLLER = "controller";
	public static final String KEY_PATH = "path";
	public static final String KEY_EMPTY = "empty";
	public static final String KEY_STORAGE_PERMISSION = "SP";

	List<String> names;
	Map<String, Map> thisMap;
	Context thisContexto;
	public ProjectListAdapter(Context context, Map<String, Map> map){
		this.thisContexto = context;
		this.thisMap = map;
		/*Alphabetic order*/
		this.names = new ArrayList<>(thisMap.keySet());
		Collections.sort(names, (n1, n2)->{return n1.toLowerCase().compareTo(n2.toLowerCase());});

		//Controller option
		if(context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_MIDI)){
			names.add(0, context.getString(R.string.controller_mode_option_title));
			thisMap.put(names.get(0), Map.of(KEY_TITLE, names.get(0), KEY_PRODUCER_NAME, context.getString(R.string.controller_mode_option_description), KEY_PATH, "", KEY_TYPE, TYPE_CONTROLLER));
		}
	}
	
	@Override
	public int getCount()
	{
		// TODO: Implement this method
		return thisMap.size();
	}

	@Override
	public Object getItem(int p1)
	{
		// TODO: Implement this method
		return thisMap.get(names.get(p1));
	}

	@Override
	public long getItemId(int p1)
	{
		// TODO: Implement this method
		return 0;
	}

	public int getChains(int pos){ return (int) thisMap.get(names.get(pos)).get(KEY_CHAINS); }
	
	@Override
	public View getView(int p1, View p2, ViewGroup p3)
	{
		if(p2 == null){
			p2 = LayoutInflater.from(thisContexto).inflate(R.layout.custom_list_projects, p3, false);
		}
	
		TextView producerName = p2.findViewById(R.id.projectAutor);
		TextView title = p2.findViewById(R.id.projectTitle);
		View currentState = p2.findViewById(R.id.currentItemState);
		
		if(names.get(p1).equals(ProjectListAdapter.KEY_STORAGE_PERMISSION)){
			title.setText(thisContexto.getString(R.string.get_storage));
			producerName.setText(thisContexto.getString(R.string.get_storage_subtitle));
			currentState.setTag(2);
		}else{
			TextView path = p2.findViewById(R.id.pathText);
			RelativeLayout itemList = p2.findViewById(R.id.itemInfoList);
			currentState.setTag(1); //BAD = 1, NOT BAD = 0, STORAGE_REQUEST = 2
			
			if (names.get(p1).equals(ProjectListAdapter.KEY_EMPTY)){
				title.setText(thisContexto.getString(R.string.without_projects));
				producerName.setText(thisContexto.getString(R.string.without_project_subtitle));
				itemList.setAlpha(0.5f);
				currentState.setAlpha(0);
			} else{
				String t = thisMap.get(names.get(p1)).get(ProjectListAdapter.KEY_TITLE).toString();
				String p = thisMap.get(names.get(p1)).get(ProjectListAdapter.KEY_PRODUCER_NAME).toString();
				path.setText(thisMap.get(names.get(p1)).get(ProjectListAdapter.KEY_PATH).toString());

				title.setText(t);
				producerName.setText(p);

				if(!thisMap.get(names.get(p1)).get(ProjectListAdapter.KEY_TYPE).equals(TYPE_GOOD)){
					currentState.setTag(0);
				} else if(!thisMap.get(names.get(p1)).get(ProjectListAdapter.KEY_TYPE).equals(TYPE_BAD)){
					currentState.setBackground(thisContexto.getDrawable(R.drawable.project_file_state_bad));
				} else {
					currentState.setBackground(new ColorDrawable(Color.GREEN));
				}
			}
		}
		return p2;
    }
}

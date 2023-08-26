package com.xayup.multipad;
import android.content.*;
import android.view.*;
import android.widget.*;

import java.util.*;

public class ProjectListAdapter extends BaseAdapter
{
	public static final String KEY_CHAINS = "chains";
	public static final String KEY_TITLE = "title";
	public static final String KEY_PRODUCER_NAME = "producerName";
	public static final String KEY_BAD = "bad";
	public static final String KEY_PATH = "path";
	public static final String KEY_EMPTY = "empty";
	public static final String KEY_STORAGE_PERMISSION = "SP";

	List<String> names;
	Map<String, Map> thisMap;
	Context thisContexto;
	public ProjectListAdapter(Context contexto, Map<String, Map> map){
		this.thisContexto = contexto;
		this.thisMap = map;
		/*Alphabetic order*/
		this.names = new ArrayList<>(thisMap.keySet());
		Collections.sort(names, (n1, n2)->{return n1.toLowerCase().compareTo(n2.toLowerCase());});
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
		p2 = LayoutInflater.from(thisContexto).inflate(R.layout.custom_list_projects, p3, false);
	
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
			
			if(!thisMap.get(names.get(p1)).get(ProjectListAdapter.KEY_BAD).equals("True")){
				title.setText(t);
				producerName.setText(p);
				currentState.setTag(0);
			} else{
				title.setText(t);
				producerName.setText(p);
				currentState.setBackground(thisContexto.getDrawable(R.drawable.project_file_state_bad));
			}
		}
		}
		return p2;
    }
}

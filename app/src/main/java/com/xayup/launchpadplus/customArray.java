package com.xayup.launchpadplus;
import android.content.*;
import android.view.*;
import android.widget.*;
import java.util.*;

public class customArray extends BaseAdapter
{
	Map<String, Map> thisMap;
	Context thisContexto;
	public customArray(Context contexto, Map<String, Map> map){
		this.thisContexto = contexto;
		this.thisMap = map;
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
		return thisMap.keySet().toArray()[p1];
	}

	@Override
	public long getItemId(int p1)
	{
		// TODO: Implement this method
		return 0;
	}
	
	@Override
	public View getView(int p1, View p2, ViewGroup p3)
	{
		p2 = LayoutInflater.from(thisContexto).inflate(R.layout.custom_list_projects, p3, false);
	
		TextView producerName = p2.findViewById(R.id.projectAutor);
		TextView title = p2.findViewById(R.id.projectTitle);
		View currentState = p2.findViewById(R.id.currentItemState);
		
		if(thisMap.keySet().toArray()[p1].equals("pr")){
			title.setText(thisContexto.getString(R.string.get_storage));
			producerName.setText(thisContexto.getString(R.string.get_storage_subtitle));
			currentState.setTag(2);
		}else{
			TextView path = p2.findViewById(R.id.pathText);
			RelativeLayout itemList = p2.findViewById(R.id.itemInfoList);
			currentState.setTag(1); //BAD = 1, NOT BAD = 0, STORAGE_REQUEST = 2
			
		if (thisMap.keySet().toArray()[p1].equals("Empyt")){
			title.setText(thisContexto.getString(R.string.without_projects));
			producerName.setText(thisContexto.getString(R.string.without_project_subtitle));
			itemList.setAlpha(0.5f);
			currentState.setAlpha(0);
		} else{
			String t = thisMap.get(thisMap.keySet().toArray()[p1]).get("title").toString();
			String p = thisMap.get(thisMap.keySet().toArray()[p1]).get("producerName").toString();
			path.setText(thisMap.get(thisMap.keySet().toArray()[p1]).get("local").toString());
			
			if(!thisMap.get(thisMap.keySet().toArray()[p1]).get("bad").equals("True")){
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

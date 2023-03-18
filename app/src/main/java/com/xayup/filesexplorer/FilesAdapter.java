package com.xayup.filesexplorer;

import com.xayup.multipad.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.xayup.filesexplorer.FilesAdapter;
import java.io.File;
import java.util.Arrays;

public class FilesAdapter extends BaseAdapter {
  File[] files;
  Context context;

  File file;
  String file_name;

  public FilesAdapter(Context context, File[] files) {
    this.files = files;
    this.context = context;
    Arrays.sort(this.files);
    Arrays.sort(this.files, (a, b) -> Boolean.compare(b.isDirectory(), a.isDirectory()));
  }

  @Override
  public int getCount() {
    return files.length;
  }

  @Override
  public Object getItem(int arg0) {
    return files[arg0];
  }

  @Override
  public long getItemId(int arg0) {
    return 0;
  }

  @Override
  public View getView(int pos, View v, ViewGroup parent) {
    v = LayoutInflater.from(context).inflate(R.layout.layout_files_adapter, null);
    ImageView icon = v.findViewById(R.id.file_item_icon);
    TextView name = v.findViewById(R.id.file_item_name);
    TextView size = v.findViewById(R.id.file_item_size);

    file = ((File) getItem(pos));
    file_name = file.getName();

    if (file.isFile()) {
      icon.setImageDrawable(context.getDrawable(R.drawable.icon_file));
      size.setText("" + file.length());
    } else if (file_name.lastIndexOf(".zip") != -1) {
      icon.setImageDrawable(context.getDrawable(R.drawable.icon_file));
    } else {
      icon.setImageDrawable(context.getDrawable(R.drawable.icon_folder));
    }
    name.setText("" + file.getName());

    return v;
  }
}

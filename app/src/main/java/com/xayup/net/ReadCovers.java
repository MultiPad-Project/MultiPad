package com.xayup.net;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.widget.ImageView;
import com.xayup.multipad.ProjectsAdapter;

import java.io.*;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class ReadCovers implements Runnable {
    protected AtomicBoolean running;
    protected Map<String, Drawable> try_download_covers;
    Context context;

    public ReadCovers(Context context) {
        this.context = context;
        running = new AtomicBoolean(false);
    }

    public Drawable getCover(String project_name) {
        return try_download_covers.get(project_name);
    }

    public void findAndSetLocalCover(ImageView img, ProjectsAdapter projects_adapter, int adapter_index){
        File cover = new File(projects_adapter.getPath(adapter_index), "cover.png");
        if(cover.exists()){
            img.setImageURI(Uri.fromFile(cover));
        } else {
            try_download_covers.put(projects_adapter.getPath(adapter_index), null);
        }
    }

    @Override
    public void run() {
        BufferedInputStream url_input = null;
        FileOutputStream output_file = null;
        File local_file = new File(Environment.getExternalStorageDirectory() + File.separator + "cover_map.xml");

        //Ready from remote file
        try {
            url_input = new BufferedInputStream(new URL("https://raw.githubusercontent.com/XayUp/MultiPad/data/project_covers/cover_map.xml").openStream());
            if (!local_file.exists()) {
                local_file.mkdirs();
            }
            output_file = new FileOutputStream(local_file);
            byte[] bytes = new byte[1024];
            int byteIndex = 0;
            while ((byteIndex = url_input.read(bytes, 0, bytes.length)) != -1) {
                output_file.write(bytes, 0, byteIndex);
            }
            output_file.close();
            url_input.close();
        } catch (IOException io) {
            running.set(false);
            return;
        }

    }
}

package com.xayup.multipad.load.thread;

import android.content.Context;
import com.xayup.multipad.Readers;
import com.xayup.multipad.load.Project;
import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class LoadProject extends Project implements Runnable {
    
    Context context;
    
    /* For Read Leds */
    protected List<File> ledT1;
    protected List<File> ledT2;

    public LoadProject(Context context) {
        this.context = context;
        new Thread(this).start();
    }

    @Override
    public void run() {
        if (keyleds_paths != null) {
            int ident = 0;
            for (File path : keyleds_paths){ //Order
                List<File> files = Arrays.asList(path.listFiles());
		        Collections.sort(files, new Comparator<File>() {
			        @Override
			        public int compare(File f1, File f2) {
			        	return f1.getName().compareTo(f2.getName());
		        	}
	        	});
                for (File led : files){
                    if (ident == 0){
                        ledT1.add(led);
                        ident = 1;
                    } else {
                        ledT2.add(led);
                        ident = 0;
                    }
                }
            }
            new Thread(() -> {ledRead(ledT1);}).start();
            new Thread(() -> {ledRead(ledT2);}).start();
        }
        if (sound)
    }

    protected Runnable ledRead(List<File> led_files) {
        return () -> {
            for (File led : led_files){
                Readers.readKeyLEDs(context, led_files);
            }
        };
    }
}

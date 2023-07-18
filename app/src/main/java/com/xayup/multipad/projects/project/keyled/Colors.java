package com.xayup.multipad.projects.project.keyled;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;
import com.xayup.color.table.utils.ColorTable;
import com.xayup.multipad.configs.GlobalConfigs;
import com.xayup.utils.Utils;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class Colors {
    protected Activity context;
    protected int[] colors;
    protected ColorTable mColorTable;
    protected String current_table;
    protected String default_table_name;
    protected String table_path;

    public Colors(Activity context) {
        this.context = context;
        this.mColorTable = new ColorTable(context);
        this.table_path = GlobalConfigs.DefaultConfigs.COLOR_TABLE_PATH;
        this.default_table_name = GlobalConfigs.PlayPadsConfigs.default_table_name;
        this.current_table = GlobalConfigs.PlayPadsConfigs.current_table;
        try {
            this.assetExported();
            this.getTable(current_table);
        } catch (IOException e) {
            Log.e("eh..", e.toString());
            context.runOnUiThread(() -> Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show());
            colors = new int[128];
        }
    }

    protected void assetExported() throws IOException {
        String[] default_tables = context.getAssets().list("color_tables");
        for(String table : default_tables){
            File file = new File(table_path, table);
            if (file.getParentFile().mkdirs() || !file.exists()) {
                try {
                    if (!file.createNewFile())
                        throw new IOException("Make file");
                    Utils.FileWriter fw = new Utils.FileWriter(file);
                    InputStream is;
                    fw.write(
                            (is = context.getAssets().open("color_tables/".concat(table))),
                            0,
                            new byte[1024]);
                    fw.close();
                    is.close();
                } catch (IOException e) {
                    Log.e("Try read asset", e.toString());
                }
            }
        }
    }

    public void getTable(File table) throws IOException {
        if (table.exists()) {
            colors = mColorTable.toColorWithAlpha(mColorTable.parse(table));
        } else {
            colors = mColorTable.toColorWithAlpha(mColorTable.parse(new File(default_table_name)));
        }
        if (colors == null) {
            throw new IOException(table_path + "/" + table.getName() + " does not exist!");
        }
    }

    public void getTable(String table_name) throws IOException {
        getTable(new File(table_path, table_name));

    }

    public int colorFromVelocity(byte velocity) {
        return colors[velocity];
    }
}
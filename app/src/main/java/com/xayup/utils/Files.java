package com.xayup.utils;

import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;

public class Files {
    public static JSONObject readJson(File json_file) throws IOException {
        try(FileReader info_stream = new FileReader(json_file)){
            try(BufferedReader buffer = new BufferedReader(info_stream)){
                StringBuilder content = new StringBuilder();
                String a;
                while((a = buffer.readLine()) != null) content.append(a);
                return new JSONObject(content.toString());

        } catch (IOException ignored){ throw new FileNotFoundException(json_file.getPath() + " not found"); }
        } catch (JSONException e) { throw new IOException(e.getCause()); }
    }
}

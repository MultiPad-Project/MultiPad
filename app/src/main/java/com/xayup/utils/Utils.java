package com.xayup.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class Utils {
    public static class FileWriter extends FileOutputStream {

        public FileWriter(File file) throws IOException, FileNotFoundException {
            super(file);
        }

        public void write(InputStream is, int offset, byte[] bytes) throws IOException {
            int length;
            while ((length = is.read(bytes)) != -1) this.write(bytes, offset, length);
        }
        
        public void close() throws IOException {
            super.close();
        }
    }
    
    public static class Math {
        public static int positiveSubtraction(int value1, int value2){
            return (value1 > value2) ? value1 - value2 : value2 - value1;
        }
    }
}

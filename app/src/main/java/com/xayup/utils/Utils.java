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
            while (is.read(bytes) != -1) {
                this.write(bytes, offset, bytes.length);
                offset++;
            }
        }
        
        public void close() throws IOException {
            super.close();
        }
    }
}

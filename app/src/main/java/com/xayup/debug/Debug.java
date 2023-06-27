package com.xayup.debug;

import android.app.*;
import android.content.*;
import android.util.Log;

import java.io.*;

public class Debug {
    public static class StackTrace {
        protected StringBuilder trace;

        public StackTrace(Context context){
                XLog.v("Stack Trace", "Exists");
                try {
                    InputStreamReader mISR = new InputStreamReader(context.openFileInput("stack.trace"));
                    BufferedReader mBR = new BufferedReader(mISR);
                    String line;
                    StringBuilder trace = new StringBuilder();
                    while ((line = mBR.readLine()) != null) {
                        trace.append(line).append("\n");
                    }
                    mBR.close();
                    mISR.close();
                    context.deleteFile("stack.trace");
                } catch (FileNotFoundException fnfe){
                    XLog.v("Stack Trace", "Not Exists");
                }
                catch (IOException ioe) {
                    Log.e("Debug.stackTrace()", ioe.toString());
                }
        }

        public String getStackTrace(){
            return (trace == null) ? null : trace.toString();
        }
    }

    public Debug(Activity context) {
        Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler(context));
    }
    /*
    * TopExceptionHandler obtained from https://stackoverflow.com/q/7385957
    */
    protected static class TopExceptionHandler implements Thread.UncaughtExceptionHandler {
        private final Thread.UncaughtExceptionHandler defaultUEH;
        private final Activity app;

        public TopExceptionHandler(Activity app) {
            this.defaultUEH = Thread.getDefaultUncaughtExceptionHandler();
            this.app = app;
        }

        @Override
        public void uncaughtException(Thread t, Throwable e) {
            StackTraceElement[] arr = e.getStackTrace();
            StringBuilder report = new StringBuilder(e + "\n\n");
            report.append("--------- ").append("Stack trace").append(" ---------\n\n");
            for (StackTraceElement stackTraceElement : arr) {
                report.append("    ").append(stackTraceElement.toString()).append("\n");
            }
            report.append("-------------------------------\n\n");

            // If the exception was thrown in a background thread inside
            // AsyncTask, then the actual exception can be found with getCause

            Throwable cause = e.getCause();
            if (cause != null) {
                report.append("--------- Cause ---------\n\n");
                report.append(cause).append("\n\n");
                arr = cause.getStackTrace();
                for (StackTraceElement stackTraceElement : arr) {
                    report.append("    ").append(stackTraceElement.toString()).append("\n");
                }
                report.append("-------------------------------\n\n");
                report.append(">>>>> Please copy and send this to Developer <<<<<");
            }

            try {
                FileOutputStream trace = app.openFileOutput("stack.trace", Context.MODE_PRIVATE);
                trace.write(report.toString().getBytes());
                trace.close();
            } catch (IOException ioe) {
                Log.e("Debug.uncaughtException()", ioe.toString());
            }

            defaultUEH.uncaughtException(t, e);
        }
    }
}

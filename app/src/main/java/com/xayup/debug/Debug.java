package com.xayup.debug;

import android.app.*;
import android.content.*;
import android.util.Log;

import java.io.*;

public class Debug {
    public static String stackTrace(Context context) {
        if (context.getFileStreamPath("stack.trace").exists()) {
            try {
                BufferedReader reader =
                        new BufferedReader(
                                new InputStreamReader(context.openFileInput("stack.trace")));
                String line;
                StringBuilder trace_log = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    trace_log.append(line).append("\n");
                }
                context.deleteFile("stack.trace");
                return trace_log.toString();

            } catch (IOException ioe) {
                Log.e("Debug.stackTrace()", ioe.toString());
            }
        }
        return null;
    }

    public Debug(Activity context) {
        Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler(context));
    }
    /*
    * TopExceptionHandler obtained from https://stackoverflow.com/q/7385957
    */
    protected static class TopExceptionHandler implements Thread.UncaughtExceptionHandler {
        private final Thread.UncaughtExceptionHandler defaultUEH;
        private Activity app = null;

        public TopExceptionHandler(Activity app) {
            this.defaultUEH = Thread.getDefaultUncaughtExceptionHandler();
            this.app = app;
        }

        public void uncaughtException(Thread t, Throwable e) {
            StackTraceElement[] arr = e.getStackTrace();
            StringBuilder report = new StringBuilder(e.toString() + "\n\n");
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

package com.xayup.debug;

import android.app.*;
import android.content.*;
import android.os.Build;
import android.util.Log;

import java.io.*;

public class Debug {
    protected static final String STACK_TRACE_FILE_NAME = "stack.trace";
    public static class StackTrace {
        protected StringBuilder trace;
        public StackTrace(Context context){
                try {
                    InputStreamReader mISR = new InputStreamReader(context.openFileInput(STACK_TRACE_FILE_NAME));
                    XLog.v("Stack Trace", "Exists");
                    BufferedReader mBR = new BufferedReader(mISR);
                    String line;
                    trace = new StringBuilder();
                    while ((line = mBR.readLine()) != null) {
                        trace.append(line).append("\n");
                    }
                    mBR.close();
                    mISR.close();
                    context.deleteFile(STACK_TRACE_FILE_NAME);
                } catch (FileNotFoundException fnfe){
                    XLog.e("Stack Trace", "Not Exists");
                }
                catch (IOException ioe) {
                    Log.e("Debug.stackTrace()", ioe.toString());
                }
        }

        public String getStackTrace(){
            XLog.v("Stack Trace", "Get");
            return (trace == null) ? null : trace.toString();
        }
    }

    public void afterCrash(){}

    public Debug(Activity context) {
        Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler(context));
    }
    /*
    * TopExceptionHandler obtained from https://stackoverflow.com/q/7385957
    */
    protected class TopExceptionHandler implements Thread.UncaughtExceptionHandler {
        private final Thread.UncaughtExceptionHandler defaultUEH;
        private final Activity app;

        public TopExceptionHandler(Activity app) {
            this.defaultUEH = Thread.getDefaultUncaughtExceptionHandler();
            this.app = app;
            XLog.v("Stack Trace", "Start");
        }

        @Override
        public void uncaughtException(Thread t, Throwable e) {
            XLog.v("Stack Trace", "uncaughtException()");

            StackTraceElement[] arr = e.getStackTrace();
            StringBuilder report = new StringBuilder("--------- Device Info ---------");
            report.append("\n\n");
            report.append("       Android version: ").append(Build.VERSION.RELEASE).append("\n");
            report.append("       Manufacturer: ").append(" ").append(Build.MANUFACTURER).append("\n");
            report.append("       Model: ").append(Build.MODEL).append(" (").append(Build.DEVICE).append(")");
            report.append("\n\n");
            report.append("--------- Crash summary ---------");
            report.append("\n\n");
            report.append("       ").append(e);
            report.append("\n\n");
            report.append("--------- ").append("Stack trace").append(" ---------\n\n");
            for (StackTraceElement stackTraceElement : arr) {
                report.append("       ").append(stackTraceElement.toString()).append("\n");
            }
            report.append("\n-------------------------------\n\n");

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
            }

            report.append(">>>>> Please COPY and SEND this to Developer (@XayUp) <<<<<");

            try {
                //if(!(new File(STACK_TRACE_FILE_NAME).createNewFile())) throw new IOException("Create File error");
                FileOutputStream trace = app.openFileOutput(STACK_TRACE_FILE_NAME, Context.MODE_PRIVATE);
                trace.write(report.toString().getBytes());
                trace.close();
            } catch (FileNotFoundException fnfe){
                XLog.e("Stack Trace", "Log with file not exists");
            }
            catch (IOException ioe) {
                Log.e("Debug.uncaughtException()", ioe.toString());
            }
            afterCrash();
            defaultUEH.uncaughtException(t, e);
        }
    }
}

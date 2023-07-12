package com.xayup.multipad;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.*;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.xayup.debug.Debug;
import com.xayup.storage.FileManagerPermission;
import com.xayup.storage.ManagePermission;

public class Main extends Activity {

    protected FileManagerPermission fmp;

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        Debug.StackTrace stackTrace = new Debug.StackTrace(this);
        if (stackTrace.getStackTrace() != null) {
            setContentView(R.layout.crash);

            TextView textLog = findViewById(R.id.logText);
            textLog.setText(stackTrace.getStackTrace());

            Button copyToClipboard = findViewById(R.id.copyLog);
            Button finishApp = findViewById(R.id.exitcrash);
            Button restartApp = findViewById(R.id.restartApp);

            copyToClipboard.setOnClickListener((v) -> {
                ((ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE)).setPrimaryClip(
                        new ClipData(
                                "MultiPad error log",
                                new String[]{ClipDescription.MIMETYPE_TEXT_PLAIN},
                                new ClipData.Item(stackTrace.getStackTrace())
                        )
                );
                Toast.makeText(this, R.string.cop, Toast.LENGTH_SHORT).show();
            });
            finishApp.setOnClickListener((v) -> finishAffinity());
            restartApp.setOnClickListener((v) -> recreate());
        } else {
            new Debug(this) {
                @Override
                public void afterCrash(){
                    super.afterCrash();
                    context.runOnUiThread(()-> Toast.makeText(
                            context,
                            context.getString(R.string.msg_after_crash),
                            Toast.LENGTH_SHORT).show());
                }
            };
            checkPermissions(this);
        }
    }

    public void startActivity(){
        this.startActivity(new Intent(this, MainActivity.class));
        this.finish();
    }

    public void checkPermissions(Context context){
        fmp = new FileManagerPermission(context) {
            @Override
            public void onStorageGranted() { startActivity(); }

            @Override
            public void onStorageDenied(int attempts, FileManagerPermission managerPermission) {
                if(attempts == 0) {
                    managerPermission.showSimpleAlertDialog(
                            context.getString(R.string.storage_permission_title),
                            context.getString(R.string.storage_permission_msg),
                            context.getString(R.string.cancel),
                            context.getString(R.string.ok)).create().show();
                } else if (attempts == 1){
                    managerPermission.showSimpleAlertDialog(
                            context.getString(R.string.storage_permission_title_1),
                            context.getString(R.string.storage_permission_msg_1),
                            context.getString(R.string.cancel),
                            context.getString(R.string.ok)).create().show();
                } else {
                    Toast.makeText(
                            context,
                            context.getString(R.string.storage_permission_msg_2) + " " +
                            context.getString(R.string.storage_permission_title_2),
                            Toast.LENGTH_LONG).show();
                    context.finish();
                }
            }
        };
        fmp.checkPermission();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (fmp.resultIsStoragePermission(requestCode)) {
            if(fmp.permissionGranted()) startActivity();
            else fmp.checkPermission();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (fmp.resultIsStoragePermission(requestCode)) {
            if (fmp.permissionGranted()) startActivity();
            else fmp.checkPermission();
        }
    }
}

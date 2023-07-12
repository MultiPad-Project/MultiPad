package com.xayup.storage;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.provider.Settings;
import android.os.Build;
import android.os.Environment;

public abstract class FileManagerPermission {
    protected Activity context;
    protected ManagePermission mPermission;
    protected int denied = 0;

    public final String[] per =
            new String[] {
                "android.permission.MANAGER_EXTERNAL_STORAGE",
                "android.permission.WRITE_EXTERNAL_STORAGE",
                "android.permission.READ_EXTERNAL_STORAGE"
            };

    public final int STORAGE_PERMISSION = 1000;
    public final int ANDROID_11_REQUEST_PERMISSION_AMF = 1001;
    public final int android11per = 1;

    public abstract void onStorageGranted();
    public abstract void onStorageDenied(int attempts, FileManagerPermission managerPermission);

    public FileManagerPermission(Context context) {
        this.context = (Activity) context;
    }

    public void checkPermission() {
        boolean storage_granted = permissionGranted();
        if (storage_granted) {
            onStorageGranted();
        } else {
            onStorageDenied(denied++, this);
        }
    }

    protected void getTotalStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                Uri uri = Uri.fromParts("package", context.getPackageName(), null);
                intent.setData(uri);
                System.out.println("\n\n" + intent);
                context.startActivityForResult(intent, ANDROID_11_REQUEST_PERMISSION_AMF);
            }
        } else {
            if (context.checkCallingPermission(per[android11per])
                            != PackageManager.PERMISSION_GRANTED
                    | context.checkCallingPermission(per[1 + android11per])
                            != PackageManager.PERMISSION_GRANTED)
                context.requestPermissions(per, STORAGE_PERMISSION);
        }
    }

    public boolean permissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        } else {
            PackageManager pm = context.getPackageManager();
            return (PackageManager.PERMISSION_GRANTED
                    == pm.checkPermission(
                            per[android11per], pm.getNameForUid(Binder.getCallingUid())));
        }
    }

    public AlertDialog.Builder showSimpleAlertDialog(String title, String msg, String cancel, String ok) {
        // Crie um dialogo
        AlertDialog.Builder alert_get_storage_permission = new AlertDialog.Builder(context);
        alert_get_storage_permission.setTitle(title);
        alert_get_storage_permission.setMessage(msg);
        alert_get_storage_permission.setPositiveButton(
                ok, (DialogInterface arg0, int arg1) -> getTotalStoragePermission());
        alert_get_storage_permission.setNegativeButton(
                cancel, (DialogInterface arg0, int arg1) -> context.finishAffinity());
        alert_get_storage_permission.setCancelable(false);
        return alert_get_storage_permission;
    }

    public boolean resultIsStoragePermission(int requestCode){
        return requestCode == ANDROID_11_REQUEST_PERMISSION_AMF || requestCode == STORAGE_PERMISSION;
    }
}

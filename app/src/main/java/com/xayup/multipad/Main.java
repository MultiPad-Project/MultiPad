package com.xayup.multipad;

import android.app.Activity;
import android.content.*;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.xayup.debug.Debug;

public class Main extends Activity {
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
            Context context = this;
            Debug debug = new Debug(this){
                @Override
                public void afterCrash() {
                    super.afterCrash();
                    //context.startActivity(new Intent(context, Main.class));
                }
            };
            context.startActivity(new Intent(context, MainActivity.class));
            this.finish();
        }
    }
}

package com.xayup.multipad;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import java.io.File;
import java.util.HashMap;
import javax.xml.namespace.QName;

public class GetFilesTask implements Runnable {
  private Activity context;
  private AlertDialog bar;
  private int started_led_thread;
  private int ended_led_thread;
  private long started_time;
  public long time;
  private View dialog_loading;

  public GetFilesTask(Activity context) {
    this.context = context;
  }

  protected void onPreExecute() {
    started_led_thread = 0;
    ended_led_thread = -1;
    dialog_loading =
        context.getLayoutInflater().inflate(R.layout.playpads_alert_loading_project, null);
    bar = new AlertDialog.Builder(context).setView(dialog_loading).create();
    bar.setCancelable(false);
    XayUpFunctions.showDiagInFullscreen(bar);
  }

  protected void onPostExecute() {
    bar.dismiss();
  }

  public void getFiles() {
    onPreExecute();
    new Thread(this).start();
  }
  /*
   * op - 0 = setText, 1 = setVisibility
   * view - view onde sera feita mudancas
   * value - dependendo da operacao, aqui devera ter uma string especifica
   */
  protected void changeView(final int op, final View view, final String value) {
    switch (op) {
      case 0:
        {
          context.runOnUiThread(
              () -> {
                ((TextView) view).setText(value);
              });
          break;
        }
      case 1:
        {
          final int state = (value.equals("VISIBLE")) ? View.VISIBLE : View.GONE;
          context.runOnUiThread(
              () -> {
                view.setVisibility(state);
              });
          break;
        }
    }
  }

  public void run() {
    started_time = SystemClock.uptimeMillis();
    File file_keysound = null;
    File file_autoplay = null;
    View text_keyled = null;
    View text_keysound;
    View text_autoplay;
    File[] root_project = new File(PlayPads.getCurrentPath).listFiles(Readers.projectFiles);
    for (File file : root_project) {
      switch (file.getName().toLowerCase()) {
        case "keysound":
          file_keysound = file;
          break;
        case "autoplay":
          file_autoplay = file;
          break;
        case "keyled":
          text_keyled = dialog_loading.findViewById(R.id.playpads_alert_loading_project_keyleds);
          changeView(0, text_keyled, "keyLeds: Reading...");
          changeView(1, text_keyled, "VISIBLE");
          PlayPads.ledFiles = new HashMap<>();
          int end_index = Math.round((float) file.listFiles().length / 2);
          started_led_thread = 0;
          ended_led_thread = 0;
          new Thread(
                  () -> {
                    started_led_thread++;
                    PlayPads.ledFiles.putAll(Readers.readKeyLEDs(context, file, 0, end_index));
                    ended_led_thread++;
                    Log.v("Thread 1", "finished");
                  })
              .start();
          new Thread(
                  () -> {
                    started_led_thread++;
                    PlayPads.ledFiles.putAll(
                        Readers.readKeyLEDs(context, file, end_index, file.listFiles().length));
                    ended_led_thread++;
                    Log.v("Thread 2", "finished");
                  })
              .start();
          break;
      }
    }
    if (file_keysound != null) {
      text_keysound = dialog_loading.findViewById(R.id.playpads_alert_loading_project_keysounds);
      changeView(0, text_keysound, "keySounds: Reading...");
      changeView(1, text_keysound, "VISIBLE");
      Readers.readKeySoundsNew(
          context, file_keysound, file_keysound.getParent() + File.separator + "sounds");
      changeView(0, text_keysound, "keySounds: Done!");
    }
    if (file_autoplay != null) {
      text_autoplay = dialog_loading.findViewById(R.id.playpads_alert_loading_project_autoplay);
      changeView(0, text_autoplay, "AutoPlay: Reading...");
      changeView(1, text_autoplay, "VISIBLE");
      PlayPads.autoPlay = Readers.readautoPlay(context, file_autoplay);
      PlayPads.progressAutoplay = context.findViewById(R.id.seekBarProgressAutoplay);
      PlayPads.progressAutoplay.setMax(PlayPads.autoPlay.size() - 1);
      PlayPads.progressAutoplay.setContext(context);
      changeView(0, text_autoplay, "AutoPlay: Done!");
    }
    if (ended_led_thread != -1) {
      while (ended_led_thread < started_led_thread) {}
    }
    if (text_keyled != null) changeView(0, text_keyled, "keyLeds: Done!");
    time = SystemClock.uptimeMillis() - started_time;
    onPostExecute();
  }
}

package com.xayup.filesexplorer;

import com.xayup.debug.XLog;
import com.xayup.multipad.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.app.AlertDialog;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;
import android.os.*;
import android.util.Log;
import android.content.Context;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class FileExplorerDialog {
  private Activity context;
  private View explorer;

  File current_file = Environment.getExternalStorageDirectory();
  File[] files_directory;
  ListView list_files;
  Button fv_prev;
  Button fv_close;
  RelativeLayout infos;
  LinearLayout swith;
  ProgressBar bar;
  TextView fv_title;
  String leds_count = "";
  String sounds_count = "";
  String title = "";
  String producer = "";
  String size = "";
  String info_info;
  int leds = 0;
  int sounds = 0;

  final String KEYLED = "keyled";
  final String SOUNDS = "sounds";
  final String INFO = "info";
  final String PRODUCER = "producer";
  final String TITLE = "title";
  final String ENTRY = "entry";

  ViewFlipper project_infos;
  Map<String, Map<String, String>> project_in_zip;
  List<String> zip_entries_list;

  public FileExplorerDialog(Context context) {
    this.context = (Activity) context;
  }

  public View getExplorer() {
    prepare();
    return explorer;
  }

  public void getExplorerDialog() {
    final AlertDialog.Builder import_dialog = new AlertDialog.Builder(context);
    import_dialog.setView(getExplorer());
    import_dialog.setCancelable(true);
    Button close = explorer.findViewById(R.id.fv_close);
    AlertDialog import_dialog_show = import_dialog.create();
    close.setOnClickListener(
        new Button.OnClickListener() {
          @Override
          public void onClick(View v) {
            import_dialog_show.dismiss();
          }
        });
    import_dialog_show.show();
  }

  private void prepare() {
    explorer = LayoutInflater.from(context).inflate(R.layout.file_explorer, null);

    list_files = explorer.findViewById(R.id.fv_list_files);
    fv_prev = explorer.findViewById(R.id.fv_prev);
    fv_close = explorer.findViewById(R.id.fv_close);
    fv_title = explorer.findViewById(R.id.fv_current_folder);
    infos = explorer.findViewById(R.id.fv_project_infos);
    swith = explorer.findViewById(R.id.fv_project_swith);
    bar = explorer.findViewById(R.id.fv_progress_bar);
    project_infos = explorer.findViewById(R.id.fv_flipper);
    fileListToListView(context, current_file, list_files);
    list_files.setOnItemClickListener(
        new AdapterView.OnItemClickListener() {
          @Override
          public void onItemClick(AdapterView<?> adapter, View view, int pos, long id) {
            infos.setVisibility(View.GONE);
            swith.setVisibility(View.GONE);
            current_file = (File) adapter.getItemAtPosition(pos);
            if (current_file.isFile()) {
              String file_name = current_file.getName();
              if (file_name.lastIndexOf(".zip") != -1) {
                Log.v("Zip file", file_name);
                try {
                  final ZipFile zipFile = new ZipFile(current_file);
                  zip_entries_list = new ArrayList<String>();
                  project_in_zip = new HashMap<String, Map<String, String>>();
                  size = "" + zipFile.size();
                  project_infos.removeAllViews();
                  swith.setVisibility(View.GONE);
                  RelativeLayout.LayoutParams params =
                      (RelativeLayout.LayoutParams) infos.getLayoutParams();
                  params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                  params.removeRule(RelativeLayout.ABOVE);
                  infos.setLayoutParams(params);
                  infos.setVisibility(View.VISIBLE);
                  bar.setVisibility(View.VISIBLE);
                  new Thread(
                          new Runnable() {
                            @Override
                            public void run() {
                              readFilesByZipEntry(zipFile);
                              if (zip_entries_list.size() > 0) {
                                int project_counts = 0;
                                show_info:
                                for (String key : project_in_zip.keySet()) {
                                  leds_count = getValueFromMap(key, KEYLED);
                                  sounds_count = getValueFromMap(key, SOUNDS);
                                  title = getValueFromMap(key, TITLE);
                                  producer = getValueFromMap(key, PRODUCER);

                                  if (!title.equals("") || !producer.equals("")) {
                                    View v =
                                        LayoutInflater.from(context)
                                            .inflate(R.layout.project_infos, null);
                                    ((TextView) v.findViewById(R.id.fv_project_name))
                                        .setText(title);
                                    ((TextView) v.findViewById(R.id.fv_project_author))
                                        .setText(producer);
                                    ((TextView) v.findViewById(R.id.fv_project_size)).setText(size);
                                    ((Button) v.findViewById(R.id.fv_project_import))
                                        .setOnClickListener(
                                            new Button.OnClickListener() {
                                              public void onClick(View v) {
                                                final int EXTRACT = 0;
                                                final int REPLACE = 1;
                                                AlertDialog.Builder progress_dialog =
                                                    new AlertDialog.Builder(context);
                                                // layout do progresso da extração
                                                View layout =
                                                    LayoutInflater.from(context)
                                                        .inflate(
                                                            R.layout.dialog_extract_file, null);
                                                TextView mensage_view =
                                                    layout.findViewById(R.id.extract_mensage);
                                                ProgressBar extract_progress =
                                                    layout.findViewById(R.id.extract_progress_bar);
                                                // Layout de arquivo existente
                                                View replace_layout =
                                                    LayoutInflater.from(context)
                                                        .inflate(
                                                            R.layout.dialog_replace_file, null);
                                                CheckBox replace_all =
                                                    replace_layout.findViewById(
                                                        R.id.override_all_check);
                                                Button replace_yes =
                                                    replace_layout.findViewById(R.id.override_yes);
                                                Button replace_no =
                                                    replace_layout.findViewById(R.id.override_no);
                                                TextView replace_title =
                                                    replace_layout.findViewById(
                                                        R.id.override_title);
                                                // Fliper
                                                ViewFlipper flipper = new ViewFlipper(context);
                                                flipper.addView(layout, EXTRACT);
                                                flipper.addView(replace_layout, REPLACE);

                                                // Progresso layout
                                                extract_progress.setMax(
                                                    Integer.parseInt(
                                                        project_in_zip.get(key).get(ENTRY)));

                                                // Propriedades AlertDialog
                                                progress_dialog.setView(flipper);
                                                progress_dialog.setCancelable(false);
                                                final AlertDialog progress_dialog_show =
                                                    progress_dialog.create();
                                                progress_dialog_show.show();
                                                new Thread(
                                                        new Runnable() {
                                                          @Override
                                                          public void run() {
                                                            byte[] bytes = new byte[1024];
                                                            File root =
                                                                Environment
                                                                    .getExternalStorageDirectory();
                                                            File export_to =
                                                                new File(
                                                                    root
                                                                        + "/MultiPad/Projects/"
                                                                        + key);
                                                            if (!export_to.exists()) {
                                                              export_to.mkdirs();
                                                            }
                                                            String mensage = "";
                                                            File file_out;
                                                            AtomicBoolean replace_all_checked =
                                                                new AtomicBoolean(false);
                                                            extract:
                                                            for (final String entry :
                                                                zip_entries_list) {
                                                              try {
                                                                if (entry.contains(
                                                                    project_in_zip
                                                                        .keySet()
                                                                        .toArray()[
                                                                        project_infos
                                                                            .getDisplayedChild()]
                                                                        .toString())) {
                                                                  ZipEntry zipEntry =
                                                                      zipFile.getEntry(entry);
                                                                  final String project_file =
                                                                      (entry.lastIndexOf(key) == -1)
                                                                          ? entry.substring(0)
                                                                          : entry.substring(
                                                                              entry.lastIndexOf(
                                                                                  key));
                                                                  context.runOnUiThread(
                                                                      new Runnable() {
                                                                        @Override
                                                                        public void run() {
                                                                          extract_progress
                                                                              .setProgress(
                                                                                  extract_progress
                                                                                          .getProgress()
                                                                                      + 1);
                                                                          mensage_view.setText(
                                                                              context.getString(
                                                                                      R.string
                                                                                          .progress_extract)
                                                                                  + " "
                                                                                  + project_file);
                                                                        }
                                                                      });
                                                                  file_out =
                                                                      new File(
                                                                          export_to.getParent()
                                                                              + File.separator
                                                                              + project_file);
                                                                  if (zipEntry.isDirectory()) {
                                                                    if (!file_out.exists())
                                                                      file_out.mkdirs();
                                                                  } else {
                                                                    if (!replace_all_checked.get()
                                                                        && file_out.exists()) {
                                                                      // Substituir?
                                                                      AtomicBoolean Break =
                                                                          new AtomicBoolean(true);
                                                                      AtomicBoolean replace_file =
                                                                          new AtomicBoolean(false);
                                                                      replace_all
                                                                          .setOnClickListener(
                                                                              new CheckBox
                                                                                  .OnClickListener() {
                                                                                @Override
                                                                                public void onClick(
                                                                                    View v) {
                                                                                  replace_all_checked
                                                                                      .set(
                                                                                          replace_all
                                                                                              .isChecked());
                                                                                }
                                                                              });
                                                                      replace_yes
                                                                          .setOnClickListener(
                                                                              new View
                                                                                  .OnClickListener() {
                                                                                @Override
                                                                                public void onClick(
                                                                                    View v) {
                                                                                  replace_file.set(
                                                                                      true);
                                                                                  Break.set(false);
                                                                                }
                                                                              });
                                                                      replace_no.setOnClickListener(
                                                                          new View
                                                                              .OnClickListener() {
                                                                            public void onClick(
                                                                                View v) {
                                                                              Break.set(false);
                                                                            }
                                                                          });
                                                                      context.runOnUiThread(
                                                                          new Runnable() {
                                                                            @Override
                                                                            public void run() {
                                                                              replace_title.setText(
                                                                                  context.getString(
                                                                                          R.string
                                                                                              .dialog_replace_title)
                                                                                      + ": "
                                                                                      + entry
                                                                                          .substring(
                                                                                              entry
                                                                                                      .lastIndexOf(
                                                                                                          "/")
                                                                                                  + 1));
                                                                              flipper
                                                                                  .setDisplayedChild(
                                                                                      REPLACE);
                                                                            }
                                                                          });
                                                                      while (Break.get()) {
                                                                        /*Wait*/
                                                                      }
                                                                      if (!replace_file.get()) {
                                                                        if (replace_all
                                                                            .isChecked()) {
                                                                          break extract;
                                                                        }
                                                                        continue extract;
                                                                      }
                                                                      context.runOnUiThread(
                                                                          new Runnable() {
                                                                            @Override
                                                                            public void run() {
                                                                              flipper
                                                                                  .setDisplayedChild(
                                                                                      EXTRACT);
                                                                            }
                                                                          });
                                                                    }
                                                                    Log.v(
                                                                        "EXTRACT",
                                                                        context.getString(
                                                                                R.string
                                                                                    .progress_extract)
                                                                            + entry);
                                                                    InputStream input_from_zip =
                                                                        zipFile.getInputStream(
                                                                            zipEntry);
                                                                    FileOutputStream out_from_zip =
                                                                        new FileOutputStream(
                                                                            file_out, false);
                                                                    int length;
                                                                    while ((length =
                                                                            input_from_zip.read(
                                                                                bytes))
                                                                        != -1) {
                                                                      out_from_zip.write(
                                                                          bytes, 0, length);
                                                                    }
                                                                    out_from_zip.close();
                                                                    input_from_zip.close();
                                                                  }
                                                                }
                                                              } catch (NullPointerException e) {
                                                                Log.i(
                                                                    "NULL",
                                                                    e.toString()
                                                                        + e.getMessage()
                                                                        + " "
                                                                        + e.getStackTrace()[0]
                                                                            .getLineNumber());
                                                              } catch (IOException i) {
                                                                Log.i(
                                                                    "IO",
                                                                    i.toString()
                                                                        + " "
                                                                        + i.getStackTrace()[0]
                                                                            .getLineNumber());
                                                              }
                                                            }
                                                            progress_dialog_show.dismiss();
                                                          }
                                                        })
                                                    .start();
                                              }
                                            });
                                    context.runOnUiThread(
                                        new Runnable() {
                                          @Override
                                          public void run() {
                                            project_infos.addView(v);
                                          }
                                        });
                                    project_counts++;
                                  }
                                }
                                if (project_counts != 0) {
                                  if (project_counts > 1) {
                                    final int last_child = project_counts - 1;
                                    TextView count = explorer.findViewById(R.id.fv_project_count);
                                    count.setText("0 - " + project_counts);
                                    ((Button) explorer.findViewById(R.id.fv_project_prev))
                                        .setOnClickListener(
                                            new Button.OnClickListener() {
                                              @Override
                                              public void onClick(View v) {
                                                if (project_infos.getDisplayedChild() == 0) {
                                                  project_infos.setDisplayedChild(last_child);
                                                } else {
                                                  project_infos.setDisplayedChild(
                                                      project_infos.getDisplayedChild() - 1);
                                                }
                                                count.setText(
                                                    (project_infos.getDisplayedChild() + 1)
                                                        + " - "
                                                        + (last_child + 1));
                                              }
                                            });
                                    ((Button) explorer.findViewById(R.id.fv_project_next))
                                        .setOnClickListener(
                                            new Button.OnClickListener() {
                                              @Override
                                              public void onClick(View v) {
                                                if (project_infos.getDisplayedChild()
                                                    == last_child) {
                                                  project_infos.setDisplayedChild(0);
                                                } else {
                                                  project_infos.setDisplayedChild(
                                                      project_infos.getDisplayedChild() + 1);
                                                }
                                                count.setText(
                                                    (project_infos.getDisplayedChild() + 1)
                                                        + " - "
                                                        + (last_child + 1));
                                              }
                                            });
                                    context.runOnUiThread(
                                        new Runnable() {
                                          @Override
                                          public void run() {
                                            swith.setVisibility(View.VISIBLE);
                                          }
                                        });
                                  }
                                  context.runOnUiThread(
                                      new Runnable() {
                                        @Override
                                        public void run() {
                                          project_infos.setVisibility(View.VISIBLE);
                                          bar.setVisibility(View.GONE);
                                          if (swith.getVisibility() == View.VISIBLE) {
                                            infos.setVisibility(View.GONE);
                                            RelativeLayout.LayoutParams params =
                                                (RelativeLayout.LayoutParams)
                                                    infos.getLayoutParams();
                                            params.removeRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                                            params.addRule(RelativeLayout.ABOVE, swith.getId());
                                            infos.setLayoutParams(params);
                                          }
                                          infos.setVisibility(View.VISIBLE);
                                        }
                                      });
                                }
                              } else {
                                infos.setVisibility(View.GONE);
                              }
                            }
                          })
                      .start();
                } catch (IOException e) {
                }
              }
            } else {
              fileListToListView(context, current_file, list_files);
            }
          }
        });

    fv_prev.setOnClickListener(
        new Button.OnClickListener() {
          @Override
          public void onClick(View arg0) {
            infos.setVisibility(View.GONE);
            swith.setVisibility(View.GONE);
            if (current_file.isFile()) {
              current_file = current_file.getParentFile();
            }
            if (!current_file
                .getAbsolutePath()
                .equals(Environment.getExternalStorageDirectory().getAbsolutePath())) {
              current_file = current_file.getParentFile();
              fileListToListView(context, current_file, list_files);
            }
          }
        });
  }

  public String getValueFromMap(final String keySet, final String keyValue) {
    String value = project_in_zip.get(keySet).get(keyValue);
    if (value != null) {
      return value;
    } else {
      return "";
    }
  }

  public void readFilesByZipEntry(final ZipFile zipFile) {
    final Enumeration<? extends ZipEntry> fileEnum = zipFile.entries();
    int entries_count = 0;
    Map<String, String> map = new HashMap<String, String>();
    ZipEntry file_in_zip;
    File file;
    String file_name;
    String project_name = null;
    String parent_folder;
    while (fileEnum.hasMoreElements()) {
      file_in_zip = fileEnum.nextElement();
      file = new File(file_in_zip.getName());
      file_name = file.getName();
      try{
        parent_folder = file.getParentFile().getName();
      } catch (NullPointerException n){
        parent_folder = new File(zipFile.getName()).getName();
        parent_folder = parent_folder.substring(0, parent_folder.lastIndexOf("."));
      }
       XLog.v("XayUp", parent_folder);
      if (file_in_zip.isDirectory()) {
        if (file_name.equalsIgnoreCase("keyled") || file_name.equalsIgnoreCase("sounds")) {
          project_in_zip.get(project_name).put(file_name.toLowerCase(), null);
          zip_entries_list.add(file_in_zip.getName());
          entries_count++;
          project_in_zip.get(project_name).put(ENTRY, "" + entries_count);
          XLog.v("XayUp", "keyled");
        } else if (zipFile.getEntry(file_in_zip.getName() + "info") != null) {
          /*
           * Se esta arvore contém o arquivo 'info' então ele é um progeto
           * Pegue o nome da pasta pai e use-o para o nome do pasta na extração
           */
          project_name = parent_folder;
          if (project_in_zip.get(project_name) == null) project_in_zip.put(project_name, map);
          entries_count = 0;
          XLog.v("XayUp", "have info");
        }
      } else if (file_name.equalsIgnoreCase("info")) {
        try {
          /*
           * Isso irá ler o arquivo info para exibição
           */
          InputStream input = zipFile.getInputStream(file_in_zip);
          InputStreamReader reader = new InputStreamReader(input, "UTF-8");
          Scanner file_ready = new Scanner(reader);
          while (file_ready.hasNext()) {
            String line = file_ready.nextLine();
            if (line.toLowerCase().contains("producername=")) {
              project_in_zip.get(project_name).put(PRODUCER, line.substring(line.indexOf("=") + 1));
            } else if (line.toLowerCase().contains("title=")) {
              project_in_zip.get(project_name).put(TITLE, line.substring(line.indexOf("=") + 1));
            }
          }
          input.close();
          reader.close();
          zip_entries_list.add(file_in_zip.getName());
          entries_count++;
          project_in_zip.get(project_name).put(ENTRY, "" + entries_count);
        } catch (IOException u) {
        }
        /*
        * Verifique os arquivos do projeto e adicione-a ao mapa
        * junto com a posicao na lista para solicitar os arquivos 
        * especificos caso haja multiplos projetos
        */
      } else if (file_name.equalsIgnoreCase("keysound")) {
        zip_entries_list.add(file_in_zip.getName());
        entries_count++;
        project_in_zip.get(project_name).put(ENTRY, "" + entries_count);
      } else if (file_name.equalsIgnoreCase("autoplay")) {
        zip_entries_list.add(file_in_zip.getName());
        entries_count++;
        project_in_zip.get(project_name).put(ENTRY, "" + entries_count);
      } else if (file_name.equalsIgnoreCase("keyled")) {
        leds += 1;
        project_in_zip.get(project_name).put(KEYLED, "" + leds);
        zip_entries_list.add(file_in_zip.getName());
        entries_count++;
        project_in_zip.get(project_name).put(ENTRY, "" + entries_count);
      } else if (file_name.equalsIgnoreCase("sounds")) {
        sounds += 1;
        project_in_zip.get(project_name).put(SOUNDS, "" + sounds);
        zip_entries_list.add(file_in_zip.getName());
        entries_count++;
        project_in_zip.get(project_name).put(ENTRY, "" + entries_count);
      }
    }
  }

  public void fileListToListView(Context context, File dir, ListView lv) {
    if (dir.isDirectory()) {
      fv_title.setText("" + dir.getName());
      lv.setAdapter(new FilesAdapter(context, dir.listFiles()));
    }
  }

  public String putTo(File file, ZipFile zipFile) {
    try {
      return file.getParentFile().getParentFile().getName();
    } catch (NullPointerException n) {
      return new File(zipFile.getName()).getName();
    }
  }
}

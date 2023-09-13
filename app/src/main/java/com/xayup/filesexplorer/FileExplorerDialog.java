package com.xayup.filesexplorer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.*;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ViewFlipper;
import com.xayup.debug.XLog;
import com.xayup.multipad.ProjectListAdapter;
import com.xayup.multipad.R;
import com.xayup.multipad.Readers;
import com.xayup.multipad.VariaveisStaticas;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class FileExplorerDialog {
  // Para obter determinada propriedade no mapa
  protected final int KEYLED_COUNT = 0;
  protected final int SOUNDS_COUNT = 1;
  protected final int PROJECT_FILE_COUNT = 2;
  protected final int PROJECT_NAME = 3;
  protected final int PROJECT_AUTHOR = 4;
  protected final int PROJECT_ENTRY = 5;
  protected final int PROJECT_ROOT = 6;
  protected final int ZIP_DIR = 7;

  protected Activity context;
  protected View explorer;
  protected File current_file = Environment.getExternalStorageDirectory();

  /*
   * Variaveis referente às informações visuais da(s) Unipck(s)
   */
  protected File[] files_directory;
  protected ListView list_files;
  protected Button fv_prev;
  protected Button fv_close;
  protected ViewFlipper projects_infos;
  protected View infos;
  protected View swith;
  protected ProgressBar bar;
  protected TextView fv_title;
  AlertDialog extract_progress;

  // Variaveis temporarias
  protected Map<String, Map<Integer, Object>> projects;
  protected ArrayList<String> selected_zips;

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

  protected void prepare() {
    explorer = LayoutInflater.from(context).inflate(R.layout.file_explorer, null);
    list_files = explorer.findViewById(R.id.fv_list_files);
    fv_prev = explorer.findViewById(R.id.fv_prev);
    fv_close = explorer.findViewById(R.id.fv_close);
    fv_title = explorer.findViewById(R.id.fv_current_folder);
    infos = explorer.findViewById(R.id.fv_project_infos);
    swith = infos.findViewById(R.id.fv_project_swith);
    bar = explorer.findViewById(R.id.fv_progress_bar);
    projects_infos = explorer.findViewById(R.id.fv_flipper);
    selected_zips = new ArrayList<>();
    fileListToListView(context, current_file, list_files);
    fv_prev.setOnClickListener(
        new Button.OnClickListener() {
          @Override
          public void onClick(View arg0) {
            infos.setVisibility(View.GONE);
            swith.setVisibility(View.GONE);
            bar.setVisibility(View.VISIBLE);
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
    list_files.setOnItemClickListener(
        new AdapterView.OnItemClickListener() {
          @Override
          public void onItemClick(AdapterView<?> adapter, View view, int pos, long id) {
            current_file = (File) adapter.getItemAtPosition(pos);
            if (current_file.isDirectory()) {
              fileListToListView(context, current_file, list_files);
            } else if (current_file.getName().lastIndexOf(".zip") != -1) {
              /*Fins de teste*/
              if (selected_zips != null) selected_zips.clear();
              selected_zips.add(current_file.getAbsolutePath());
              // Processamento do ZIP
              projects_infos.removeAllViews();
              if (projects != null) {
                projects.clear();
              } else {
                projects = new HashMap<>();
              }
              Thread ready_zip =
                  new Thread(
                      () -> {
                        getProjectsFromZip(selected_zips);
                        if (projects.size() > 0) {
                          /*
                           * Obtenha as propriedades do projeto e adicione ao ViewFliper
                           */
                          String project_folder_name;
                          for (String key : projects.keySet()) {
                            project_folder_name = key;
                            final View project_infos =
                                context.getLayoutInflater().inflate(R.layout.project_infos, null);
                            ((TextView) project_infos.findViewById(R.id.fv_project_name))
                                .setText((String) projects.get(key).get(PROJECT_NAME));
                            ((TextView) project_infos.findViewById(R.id.fv_project_author))
                                .setText((String) projects.get(key).get(PROJECT_AUTHOR));
                            ((Button) project_infos.findViewById(R.id.fv_project_import))
                                .setOnClickListener(
                                    new View.OnClickListener() {
                                      @Override
                                      public void onClick(View button) {
                                        extract_progress = new AlertDialog.Builder(context).setCancelable(false).create();
                                        new Thread(
                                                () -> {
                                                  writeProject(
                                                      projects,
                                                      VariaveisStaticas.PROJECTS_PATH,
                                                      true,
                                                      0);
                                                  context.runOnUiThread(
                                                      () -> {
                                                        if (!VariaveisStaticas.use_unipad_folder)
                                                          ((ListView)
                                                                  context.findViewById(
                                                                      R.id.listViewProjects))
                                                              .setAdapter(
                                                                  new ProjectListAdapter(
                                                                      context,
                                                                      new Readers()
                                                                          .readInfo(
                                                                              context,
                                                                              new File(
                                                                                  VariaveisStaticas
                                                                                      .PROJECTS_PATH),
                                                                              Environment
                                                                                  .isExternalStorageManager())));
                                                      });
                                                })
                                            .start();
                                      }
                                    });
                            context.runOnUiThread(
                                () -> {
                                  projects_infos.addView(project_infos);
                                });
                          }
                          if (projects.size() > 1) {
                            // Alteranar entre os projetos
                            int project_counts = projects.size();
                            TextView current_child_view = swith.findViewById(R.id.fv_project_count);
                            current_child_view.setText("1 - " + project_counts);
                            ((Button) swith.findViewById(R.id.fv_project_next))
                                .setOnClickListener(
                                    new View.OnClickListener() {
                                      @Override
                                      public void onClick(View button) {
                                        swithInfos(1, project_counts, current_child_view);
                                      }
                                    });
                            ((Button) swith.findViewById(R.id.fv_project_prev))
                                .setOnClickListener(
                                    new View.OnClickListener() {
                                      @Override
                                      public void onClick(View button) {
                                        swithInfos(0, project_counts, current_child_view);
                                      }
                                    });
                            context.runOnUiThread(
                                () -> {
                                  swith.setVisibility(View.VISIBLE);
                                });
                          }
                          context.runOnUiThread(
                              () -> {
                                bar.setVisibility(View.GONE);
                              });
                        }
                      });
              ready_zip.start();
            }
          }
        });
  }

  protected void fileListToListView(Context context, File dir, ListView lv) {
    if (dir.isDirectory()) {
      fv_title.setText("" + dir.getName());
      lv.setAdapter(new FilesAdapter(context, dir.listFiles()));
    }
  }

  protected void swithInfos(int action, int index_limit, TextView show_index) {
    int current_child_index = projects_infos.getDisplayedChild();
    switch (action) {
      case 0: // Previous
        {
          current_child_index--;
          if (current_child_index < 0) {
            current_child_index = index_limit - 1;
          }
          break;
        }
      case 1: // Next
        {
          current_child_index++;
          if (current_child_index >= index_limit) {
            current_child_index = 0;
          }
          break;
        }
    }
    projects_infos.setDisplayedChild(current_child_index);
    show_index.setText((current_child_index + 1) + " - " + index_limit);
  }

  protected void getProjectsFromZip(ArrayList<String> zip_dirs /* Diretório(s) do(s) Zip(s) */) {
    ZipFile mZipFile;
    ZipEntry mZipEntry;
    Enumeration<? extends ZipEntry> enums;

    String zip_file_name;
    String project_name;
    String parent_folder_name;
    String file_name;
    String project;
    String parent_folder;
    String root_project;
    File entry_file;

    // Temporários
    int sound_count;
    int led_count;
    ZipEntry tmp_entry;

    for (String zip : zip_dirs) {
      try {
        mZipFile = new ZipFile(zip);
        enums = mZipFile.entries();
        zip_file_name = getZipName(mZipFile);

        nextElement:
        while (enums.hasMoreElements()) {
          mZipEntry = enums.nextElement();
          entry_file = new File(mZipEntry.getName());
          file_name = entry_file.getName();
          parent_folder = entry_file.getParent();
          /*
           * Definir as variáveis para facilmente detectar uma Unipack
           * Verifique se o arquivo tem pasta pai. Se não houver então estamos na pasta raíz.
           * Caso haja verifique se ele o pai contém o arquivo "info". Se sim, defina as variáveis.
           * Se não, verifique se o pai tem pai. Se sim, o pai do pai será o nome do projeto
           * (mesmo que não tenha o "info" pois essa verificação é feita depois).
           */
          if (parent_folder == null) {
            project_name = zip_file_name;
            parent_folder_name = "";
            root_project = "";
            parent_folder = "";
          } else {
            parent_folder_name = new File(parent_folder).getName();
            project = new File(parent_folder).getParent();
            parent_folder = parent_folder.replace(File.separator, "/");
            root_project = parent_folder + "/";
            if (mZipFile.getEntry(root_project + "Info") == null
                && mZipFile.getEntry(root_project + "info") == null) {
              if (project == null) {
                project_name = zip_file_name;
                root_project = "";
              } else {
                project_name = project.substring(project.lastIndexOf(File.separator) + 1);
                project = project.replace(File.separator, "/");
                root_project = project + "/";
              }
            } else {
              project_name = new File(root_project).getName();
            }
          }
          if (mZipEntry.isDirectory()) {
            /*
             * Não tenho interesse se for uma pasta. Caso seja um arquivo
             */
            continue nextElement;
          }
          /*
           * Verifique se isto é um projeto Unipad
           */
          tmp_entry =
              (mZipFile.getEntry(root_project + "Info") != null)
                  ? mZipFile.getEntry(root_project + "Info")
                  : mZipFile.getEntry(root_project + "info");
          if (tmp_entry != null) {
            if (!root_project.equals("")
                && !new File(root_project).getName().equalsIgnoreCase(project_name)) {
              project_name = new File(root_project).getName();
            }
            if (!projects.containsKey(project_name)) {
              /*
               * Arquivo é criada um mapa para o projeto contendo:
               * Número de leds, número de samples, nome do projeto (Info), nome do(s) autor(es)(Info)
               * localização do arquivo zip, o diretório root dentro do zip do projeto, quantidade de
               * arquivos no diretório do projeto e a lista dos diretórios a partir do diretório do projeto
               */
              context.runOnUiThread(
                  () -> {
                    if (infos.getVisibility() == View.GONE) infos.setVisibility(View.VISIBLE);
                  });

              ArrayList<ZipEntry> entry_list = new ArrayList<>();
              Map<Integer, Object> project_properties = new HashMap<>();
              InputStream mIS = mZipFile.getInputStream(tmp_entry);
              InputStreamReader mISR = new InputStreamReader(mIS, "UTF-8");
              Scanner mS = new Scanner(mISR);
              String title = "";
              String producerName = "";
              String line;
              while (mS.hasNextLine()) {
                /*
                 * Processo para ler o arquivo "info" deretamente do zip.
                 * Útil para apresentar o nome e autor do projeto
                 */
                line = mS.nextLine().trim();
                if (line.indexOf("title") == 0) {
                  title = line.substring(line.indexOf("=") + 1).trim();
                } else if (line.indexOf("producerName") == 0) {
                  producerName = line.substring(line.indexOf("=") + 1).trim();
                }
                if (!title.equals("") && !producerName.equals("")) break;
              }
              mS.close();
              mISR.close();
              mIS.close();
              project_properties.put(PROJECT_NAME, title);
              project_properties.put(PROJECT_AUTHOR, producerName);
              project_properties.put(PROJECT_ROOT, root_project);
              project_properties.put(ZIP_DIR, mZipFile.getName());
              project_properties.put(KEYLED_COUNT, 0);
              project_properties.put(SOUNDS_COUNT, 0);
              project_properties.put(PROJECT_FILE_COUNT, 0);
              project_properties.put(PROJECT_ENTRY, entry_list);
              projects.put(project_name, project_properties);
            }
            /*
             * Adicione os arquivos ao mapa pois este é uma Unipack
             * Verifique se é arquivo de led/som e adicione +1 se for verdade
             */
            ((ArrayList<ZipEntry>) projects.get(project_name).get(PROJECT_ENTRY)).add(mZipEntry);
            /*
             * Verifique se o arquivo atual é uma sample ou uma led.
             * Se sim, acrecente +1 na contagem
             * Verificação da led: Estrutura padrão é "0 0 0 0" e verifique
             * se a pasta pai começa com "keyled".
             * Verificação da sample: Extenção mais comum em Unipack são ".wav" e ".mp3"
             * e verifique se a pasta pais se chama "sounds".
             */
            if (file_name.matches("\\d\\s\\d\\s\\d\\s\\d.*")
                && parent_folder_name.toLowerCase().indexOf("keyled") == 0) {
              led_count = (int) projects.get(project_name).get(KEYLED_COUNT);
              led_count++;
              projects.get(project_name).put(KEYLED_COUNT, (int) led_count);
            } else if (file_name.matches("(.*\\.wav|.*\\.mp3)")
                && parent_folder_name.equalsIgnoreCase("sounds")) {
              sound_count = (int) projects.get(project_name).get(SOUNDS_COUNT);
              sound_count++;
              projects.get(project_name).put(SOUNDS_COUNT, (int) sound_count);
            }
          }
        }
        // Fim do processo
        mZipFile.close();
      } catch (IOException io) {
        // Arquivo zip não existe
        for (StackTraceElement s : io.getStackTrace()) {
          System.out.println(s.toString());
        }
      }
    }
  }

  protected String getZipName(ZipFile zipfile) {
    return zipfile
        .getName()
        .substring(
            zipfile.getName().lastIndexOf(File.separator) + 1, zipfile.getName().lastIndexOf("."));
  }

  protected void writeProject(
      Map<String, Map<Integer, Object>> projects, String output, boolean all, int index) {
    /*
     * A verificação de tamanho do mapa que contém os projetos é quase inútil mas optei por deixar.
     * Obtenha cada mapa e chame writeProject() para fazer o resto.
     * O nome da chave será o nome da pasta raiz do projeto.
     */
    if (projects.size() > 0) {
      context.runOnUiThread(
          () -> { // Progress Dialog
            extract_progress.show();
          });
      if (all) {
        for (String name : projects.keySet()) {
          writeProject(projects.get(name), name, output, extract_progress);
        }
      } else {
        String name = (String) projects.keySet().toArray()[index];
        writeProject(projects.get(name), name, output, extract_progress);
      }
      context.runOnUiThread(
          () -> { // Progress Dialog
            extract_progress.dismiss();
          });
    }
  }

  protected void writeProject(
      Map<Integer, Object> project, String name, String output, AlertDialog extract_dialog) {
    /*
     * Isso irá "extrair" os arquivos do zipFile, obtendo os diretório que está em project com get(PROJECT_ENTRY).
     * get(PROJECT_ENTRY) retorna uma lista dos diretório. Prefiro assim por ser mais fácil e também para facilitar
     * a extração de múltiplos projetos sem precisar fazer a verificação novamente, buscar os arquivos certo, trabalho
     * que getProjectsFromZip() já faz.
     * root_entry é o diretório raíz de onde está o arquivo "info", que indica que o projeto começa lá
     * útil para quando o projeto está muito mais longe da ráiz, que é "" no zip, como, por exemplo, "pasta1/pasta2/pasta3/projeto"
     * o nome da pasta "root_entry" é usado para criar a pasta no diretório de extração. Neste caso terá de ser "<EXTRAC_TO_DIR>/projeto/"
     * Após isso, esse detório é definido como a raíz para extração do demais arquivos contidos em root_entry.
     * O nome exato do arquivo será obtido pegando a entry e subtituindo a raíz no zip pela raíz da extração, ou seja:
     * entry = "pasta1/pasta2/pasta3/projeto/info"
     * root_entry = "pasta1/pasta2/pasta3/projeto" (Raíz do projeto no zip)
     * write_to = "<EXTRAC_TO_DIR>/" (Raiz de extração)
     * Substituindo:
     * file_out = em entry, substitua root_entry por write, resultando em "<EXTRAC_TO_DIR>/projeto/info"
     * Verifique se a pasta pai existe e se não existe, então, crie-o (como "keyled" e "sounds").
     */
    try {
      ZipFile zipFile = new ZipFile((String) project.get(ZIP_DIR));
      File write_to = new File(output + File.separator + name);
      String root_entry = (String) project.get(PROJECT_ROOT);
      ArrayList<ZipEntry> project_entrys = (ArrayList<ZipEntry>) project.get(PROJECT_ENTRY);
      if (!write_to.exists()) {
        write_to.mkdirs();
      }
      File file_out;
      File file_out_parent;
      byte[] bytes = new byte[1024];
      int len;
      for (ZipEntry entry : project_entrys) {
        file_out = new File(write_to, entry.getName().replace(root_entry, ""));
        file_out_parent = file_out.getParentFile();
        if (!file_out_parent.exists()) {
          file_out_parent.mkdirs();
        } else {
          if (file_out.exists()) {
            // Coloque aqui a operação de substituir, pular ou cancelar

          }
        }
        /*
         * Preparação para extração e extração
         */
        InputStream mIS = zipFile.getInputStream(entry);
        FileOutputStream mFOS = new FileOutputStream(file_out);
        while ((len = mIS.read(bytes)) >= 0) {
          mFOS.write(bytes, 0, len);
        }
        mFOS.close();
        mIS.close();
        System.out.println(entry.getName() + ": Arquivo criado");
      }
    } catch (FileNotFoundException fnfe) {
      XLog.v("Import Project: Extract", fnfe.toString());
    } catch (IOException io) {
      XLog.v("Import Project: Extract", io.toString());
    }
    // Extração concluida
  }

  public class ExtractDialog extends Dialog {
    Activity context;

    public ExtractDialog(Context context) {
      super(context);
      this.context = (Activity) context;
    }
  }
}

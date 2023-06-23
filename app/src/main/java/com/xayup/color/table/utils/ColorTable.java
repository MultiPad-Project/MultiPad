package com.xayup.color.table.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.ViewGroup;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.Toast;
import com.xayup.debug.XLog;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ColorTable extends TableAdapter {
    public final int CT_FORMAT = 0;
    public final int RETINA_FORMAT = 1;
    public final String CT_FILE_FORMAT = ".ct";

    protected Activity context;
    public int[ /*ID*/][ /*R,G,B*/] color_table;
    public File table_file;

    public ColorTable(Context context) {
        this.context = (Activity) context;
    }

    public int[] toColorWithAlpha(int[][] table) {
        if (color_table == null || table == null) return null;
        float[] hsv = new float[3];
        int[] colors = new int[table.length];
        for (int i = 0; i < color_table.length; i++) {
            if (table[i] != null) {
                Color.RGBToHSV(table[i][vR], table[i][vG], table[i][vB], hsv);
                colors[i] =
                        Color.argb((int) (hsv[2] * 255), table[i][vR], table[i][vG], table[i][vB]);
                XLog.v("HSV","Result: " + String.valueOf((int) hsv[2] * 255)+" "+ String.valueOf(hsv[0]) +" "+ String.valueOf(hsv[1]) +" "+ String.valueOf(hsv[2]));
            }
        }
        return colors;
    }

    public int[] toColor(int[][] table) {
        if (color_table == null || table == null) return null;
        int[] colors = new int[table.length];
        for (int i = 0; i < color_table.length; i++) {
            if (table[i] != null) {
                colors[i] = Color.rgb(table[i][vR], table[i][vG], table[i][vB]);
            }
        }
        return colors;
    }

    public int[][] parse(File table) {
        table_file = table;
        try {
            FileReader fr = new FileReader(table);
            BufferedReader br = new BufferedReader(fr);
            int[][] tmp;
            if (table_file.getName().substring(table_file.getName().length() - 3).equals(".ct")) {
                tmp = parseCT(br);
            } else {
                tmp = parseAnother(br);
            }
            br.close();
            fr.close();
            return tmp;
        } catch (IOException io) {
            Log.e("Parse file error", io.toString());
            return null;
        }
    }

    public int[][] parseAnother(BufferedReader bReader) {
        try {
            String line = bReader.readLine();
            String to_value = "";
            int value = 0;
            int id = 0;
            int[] id_rgb = null;
            color_table = new int[128][3];
            while (line != null) {
                id_rgb = new int[3];
                for (int i = 0; i < line.length(); i++) {
                    char tChar = line.charAt(i);
                    if (Character.isDigit(tChar)) {
                        to_value += Character.toString(tChar);
                    } else if (!to_value.equals("")) {
                        if (Character.toString(tChar).equals(";")) {
                            id_rgb[value] = Integer.parseInt(to_value) * 4;
                            color_table[id] = id_rgb;
                            value = 0;
                        } else if (Character.toString(tChar).equals(",")) {
                            id = Integer.parseInt(to_value);
                        } else {
                            id_rgb[value++] = Integer.parseInt(to_value) * 4;
                        }
                        to_value = "";
                    }
                }
                line = bReader.readLine();
            }
        } catch (IOException | NullPointerException | NumberFormatException | ArrayIndexOutOfBoundsException e) {
            Log.e("parseAnother()", e.getStackTrace()[0].toString());
            return null;
        }
        return color_table;
    }

    public int[][] parseCT(BufferedReader bReader) {
        try {
            String line = bReader.readLine();
            int id = 0;
            int[] id_rgb = null;
            color_table = new int[128][3];
            while (line != null) {
                if (line.contains("{")) {
                    id_rgb = new int[3];
                } else if (line.contains("id=")) {
                    id = Integer.parseInt(line.replaceAll("id=", ""));
                } else if (line.contains("r=")) {
                    id_rgb[vR] = Integer.parseInt(line.replaceAll("r=", ""));
                } else if (line.contains("g=")) {
                    id_rgb[vG] = Integer.parseInt(line.replaceAll("g=", ""));
                } else if (line.contains("b=")) {
                    id_rgb[vB] = Integer.parseInt(line.replaceAll("b=", ""));
                } else if (line.contains("}")) {
                    color_table[id] = id_rgb;
                }
                line = bReader.readLine();
            }
        } catch (IOException f) {
            System.out.println(f);
            return null;
        } catch (NullPointerException n) {
            System.out.println(n);
            return null;
        } catch (NumberFormatException n) {
            System.out.println(n);
            return null;
        } catch (ArrayIndexOutOfBoundsException a) {
            System.out.println(a);
            return null;
        }
        return color_table;
    }

    public void saveTableFile(String name, int format) {
        if (format == CT_FORMAT) {
            writeFile(builderCT(), name, CT_FORMAT);
        } else if (format == RETINA_FORMAT) {
            writeFile(builderRetina(), name, RETINA_FORMAT);
        }
    }

    protected StringBuilder builderRetina() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < color_table.length; i++) {
            if (color_table[i] != null) {
                builder.append(i + ",");
                for (int v : color_table[i]) {
                    builder.append(" " + v / 4);
                }
                builder.append(";\n");
            } else {
                builder.append(i + ", 0 0 0;");
            }
        }
        return builder;
    }

    protected StringBuilder builderCT() {
        StringBuilder builder = new StringBuilder();
        int r, g, b = 0;
        for (int i = 0; i < color_table.length; i++) {
            if (color_table[i] != null) {
                r = color_table[i][vR];
                g = color_table[i][vG];
                b = color_table[i][vB];
            } else {
                r = 0;
                g = 0;
                b = 0;
            }
            builder.append("{" + "\nid=" + i + "\nr=" + r + "\ng=" + g + "\nb=" + b + "\n}\n");
        }
        return builder;
    }

    protected void writeFile(StringBuilder builder, String name, int format) {
        try {
            File new_table_file = new File(table_file.getParent(), "." + table_file.getName());
            if (new_table_file.createNewFile()) new Throwable();
            FileWriter save_to = new FileWriter(new_table_file);
            save_to.write(builder.toString());
            save_to.close();
            if (table_file.delete()) {
                String new_name = name;
                if (format == CT_FORMAT) {
                    if (new_name.lastIndexOf(CT_FILE_FORMAT) == -1) {
                        new_name += CT_FILE_FORMAT;
                    }
                } else if (format == RETINA_FORMAT) {
                    int index = 0;
                    if ((index = new_name.lastIndexOf(".")) != -1) {
                        new_name = new_name.replace(new_name.substring(index), "");
                    }
                }
                new_table_file.renameTo(new File(new_table_file.getParent(), new_name));
            } else {
                new Throwable();
            }
            Toast.makeText(context, "Saved!", Toast.LENGTH_SHORT).show();
        } catch (IOException i) {
            Toast.makeText(context, "Save failed :(", Toast.LENGTH_SHORT).show();
            System.out.println(i);
        }
    }
}

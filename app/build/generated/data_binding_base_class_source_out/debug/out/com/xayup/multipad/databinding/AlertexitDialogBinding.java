// Generated by view binder compiler. Do not edit!
package com.xayup.multipad.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.xayup.multipad.R;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class AlertexitDialogBinding implements ViewBinding {
  @NonNull
  private final RelativeLayout rootView;

  @NonNull
  public final Button alertColorTableButtom;

  @NonNull
  public final Button alertConfigsButtom;

  @NonNull
  public final Button alertDefaultColorTableButtom;

  @NonNull
  public final LinearLayout alertExitBar;

  @NonNull
  public final RelativeLayout alertExitButtonExit;

  @NonNull
  public final ListView alertExitListColorTable;

  @NonNull
  public final ListView alertExitListSkins;

  @NonNull
  public final Button alertExitPrev;

  @NonNull
  public final TextView alertExitTitle;

  @NonNull
  public final ImageView exitButtonIcon;

  @NonNull
  public final ViewFlipper exitMenuSwitcher;

  private AlertexitDialogBinding(@NonNull RelativeLayout rootView,
      @NonNull Button alertColorTableButtom, @NonNull Button alertConfigsButtom,
      @NonNull Button alertDefaultColorTableButtom, @NonNull LinearLayout alertExitBar,
      @NonNull RelativeLayout alertExitButtonExit, @NonNull ListView alertExitListColorTable,
      @NonNull ListView alertExitListSkins, @NonNull Button alertExitPrev,
      @NonNull TextView alertExitTitle, @NonNull ImageView exitButtonIcon,
      @NonNull ViewFlipper exitMenuSwitcher) {
    this.rootView = rootView;
    this.alertColorTableButtom = alertColorTableButtom;
    this.alertConfigsButtom = alertConfigsButtom;
    this.alertDefaultColorTableButtom = alertDefaultColorTableButtom;
    this.alertExitBar = alertExitBar;
    this.alertExitButtonExit = alertExitButtonExit;
    this.alertExitListColorTable = alertExitListColorTable;
    this.alertExitListSkins = alertExitListSkins;
    this.alertExitPrev = alertExitPrev;
    this.alertExitTitle = alertExitTitle;
    this.exitButtonIcon = exitButtonIcon;
    this.exitMenuSwitcher = exitMenuSwitcher;
  }

  @Override
  @NonNull
  public RelativeLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static AlertexitDialogBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static AlertexitDialogBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.alertexit_dialog, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static AlertexitDialogBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.alert_color_table_buttom;
      Button alertColorTableButtom = ViewBindings.findChildViewById(rootView, id);
      if (alertColorTableButtom == null) {
        break missingId;
      }

      id = R.id.alert_configs_buttom;
      Button alertConfigsButtom = ViewBindings.findChildViewById(rootView, id);
      if (alertConfigsButtom == null) {
        break missingId;
      }

      id = R.id.alert_default_color_table_buttom;
      Button alertDefaultColorTableButtom = ViewBindings.findChildViewById(rootView, id);
      if (alertDefaultColorTableButtom == null) {
        break missingId;
      }

      id = R.id.alertExit_bar;
      LinearLayout alertExitBar = ViewBindings.findChildViewById(rootView, id);
      if (alertExitBar == null) {
        break missingId;
      }

      id = R.id.alertExitButtonExit;
      RelativeLayout alertExitButtonExit = ViewBindings.findChildViewById(rootView, id);
      if (alertExitButtonExit == null) {
        break missingId;
      }

      id = R.id.alertExit_list_color_table;
      ListView alertExitListColorTable = ViewBindings.findChildViewById(rootView, id);
      if (alertExitListColorTable == null) {
        break missingId;
      }

      id = R.id.alertExitListSkins;
      ListView alertExitListSkins = ViewBindings.findChildViewById(rootView, id);
      if (alertExitListSkins == null) {
        break missingId;
      }

      id = R.id.alertExit_prev;
      Button alertExitPrev = ViewBindings.findChildViewById(rootView, id);
      if (alertExitPrev == null) {
        break missingId;
      }

      id = R.id.alertExitTitle;
      TextView alertExitTitle = ViewBindings.findChildViewById(rootView, id);
      if (alertExitTitle == null) {
        break missingId;
      }

      id = R.id.exit_button_icon;
      ImageView exitButtonIcon = ViewBindings.findChildViewById(rootView, id);
      if (exitButtonIcon == null) {
        break missingId;
      }

      id = R.id.exitMenuSwitcher;
      ViewFlipper exitMenuSwitcher = ViewBindings.findChildViewById(rootView, id);
      if (exitMenuSwitcher == null) {
        break missingId;
      }

      return new AlertexitDialogBinding((RelativeLayout) rootView, alertColorTableButtom,
          alertConfigsButtom, alertDefaultColorTableButtom, alertExitBar, alertExitButtonExit,
          alertExitListColorTable, alertExitListSkins, alertExitPrev, alertExitTitle,
          exitButtonIcon, exitMenuSwitcher);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
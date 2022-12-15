// Generated by view binder compiler. Do not edit!
package com.xayup.multipad.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

public final class MainFloatMenuBinding implements ViewBinding {
  @NonNull
  private final RelativeLayout rootView;

  @NonNull
  public final ListView listSkins;

  @NonNull
  public final ListView listUsbMidiDevices;

  @NonNull
  public final RelativeLayout mainFloatingMenu;

  @NonNull
  public final ViewFlipper mainFloatingMenuBackground;

  @NonNull
  public final LinearLayout mainFloatingMenuBar;

  @NonNull
  public final Button mainFloatingMenuBarButtonPrev;

  @NonNull
  public final TextView mainFloatingMenuBarTitle;

  @NonNull
  public final Button mainFloatingMenuButtonExit;

  @NonNull
  public final Button mainFloatingMenuButtonMidiDevices;

  private MainFloatMenuBinding(@NonNull RelativeLayout rootView, @NonNull ListView listSkins,
      @NonNull ListView listUsbMidiDevices, @NonNull RelativeLayout mainFloatingMenu,
      @NonNull ViewFlipper mainFloatingMenuBackground, @NonNull LinearLayout mainFloatingMenuBar,
      @NonNull Button mainFloatingMenuBarButtonPrev, @NonNull TextView mainFloatingMenuBarTitle,
      @NonNull Button mainFloatingMenuButtonExit,
      @NonNull Button mainFloatingMenuButtonMidiDevices) {
    this.rootView = rootView;
    this.listSkins = listSkins;
    this.listUsbMidiDevices = listUsbMidiDevices;
    this.mainFloatingMenu = mainFloatingMenu;
    this.mainFloatingMenuBackground = mainFloatingMenuBackground;
    this.mainFloatingMenuBar = mainFloatingMenuBar;
    this.mainFloatingMenuBarButtonPrev = mainFloatingMenuBarButtonPrev;
    this.mainFloatingMenuBarTitle = mainFloatingMenuBarTitle;
    this.mainFloatingMenuButtonExit = mainFloatingMenuButtonExit;
    this.mainFloatingMenuButtonMidiDevices = mainFloatingMenuButtonMidiDevices;
  }

  @Override
  @NonNull
  public RelativeLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static MainFloatMenuBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static MainFloatMenuBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.main_float_menu, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static MainFloatMenuBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.list_skins;
      ListView listSkins = ViewBindings.findChildViewById(rootView, id);
      if (listSkins == null) {
        break missingId;
      }

      id = R.id.list_usb_midi_devices;
      ListView listUsbMidiDevices = ViewBindings.findChildViewById(rootView, id);
      if (listUsbMidiDevices == null) {
        break missingId;
      }

      RelativeLayout mainFloatingMenu = (RelativeLayout) rootView;

      id = R.id.main_floating_menu_background;
      ViewFlipper mainFloatingMenuBackground = ViewBindings.findChildViewById(rootView, id);
      if (mainFloatingMenuBackground == null) {
        break missingId;
      }

      id = R.id.main_floating_menu_bar;
      LinearLayout mainFloatingMenuBar = ViewBindings.findChildViewById(rootView, id);
      if (mainFloatingMenuBar == null) {
        break missingId;
      }

      id = R.id.main_floating_menu_bar_button_prev;
      Button mainFloatingMenuBarButtonPrev = ViewBindings.findChildViewById(rootView, id);
      if (mainFloatingMenuBarButtonPrev == null) {
        break missingId;
      }

      id = R.id.main_floating_menu_bar_title;
      TextView mainFloatingMenuBarTitle = ViewBindings.findChildViewById(rootView, id);
      if (mainFloatingMenuBarTitle == null) {
        break missingId;
      }

      id = R.id.main_floating_menu_button_exit;
      Button mainFloatingMenuButtonExit = ViewBindings.findChildViewById(rootView, id);
      if (mainFloatingMenuButtonExit == null) {
        break missingId;
      }

      id = R.id.main_floating_menu_button_midi_devices;
      Button mainFloatingMenuButtonMidiDevices = ViewBindings.findChildViewById(rootView, id);
      if (mainFloatingMenuButtonMidiDevices == null) {
        break missingId;
      }

      return new MainFloatMenuBinding((RelativeLayout) rootView, listSkins, listUsbMidiDevices,
          mainFloatingMenu, mainFloatingMenuBackground, mainFloatingMenuBar,
          mainFloatingMenuBarButtonPrev, mainFloatingMenuBarTitle, mainFloatingMenuButtonExit,
          mainFloatingMenuButtonMidiDevices);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}

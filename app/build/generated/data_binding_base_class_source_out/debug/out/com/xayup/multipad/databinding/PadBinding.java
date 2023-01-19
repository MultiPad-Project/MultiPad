// Generated by view binder compiler. Do not edit!
package com.xayup.multipad.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.xayup.multipad.R;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class PadBinding implements ViewBinding {
  @NonNull
  private final RelativeLayout rootView;

  @NonNull
  public final ImageView led;

  @NonNull
  public final TextView map;

  @NonNull
  public final ImageView pad;

  @NonNull
  public final ImageView phantom;

  @NonNull
  public final ImageView press;

  private PadBinding(@NonNull RelativeLayout rootView, @NonNull ImageView led,
      @NonNull TextView map, @NonNull ImageView pad, @NonNull ImageView phantom,
      @NonNull ImageView press) {
    this.rootView = rootView;
    this.led = led;
    this.map = map;
    this.pad = pad;
    this.phantom = phantom;
    this.press = press;
  }

  @Override
  @NonNull
  public RelativeLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static PadBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static PadBinding inflate(@NonNull LayoutInflater inflater, @Nullable ViewGroup parent,
      boolean attachToParent) {
    View root = inflater.inflate(R.layout.pad, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static PadBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.led;
      ImageView led = ViewBindings.findChildViewById(rootView, id);
      if (led == null) {
        break missingId;
      }

      id = R.id.map;
      TextView map = ViewBindings.findChildViewById(rootView, id);
      if (map == null) {
        break missingId;
      }

      id = R.id.pad;
      ImageView pad = ViewBindings.findChildViewById(rootView, id);
      if (pad == null) {
        break missingId;
      }

      id = R.id.phantom;
      ImageView phantom = ViewBindings.findChildViewById(rootView, id);
      if (phantom == null) {
        break missingId;
      }

      id = R.id.press;
      ImageView press = ViewBindings.findChildViewById(rootView, id);
      if (press == null) {
        break missingId;
      }

      return new PadBinding((RelativeLayout) rootView, led, map, pad, phantom, press);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
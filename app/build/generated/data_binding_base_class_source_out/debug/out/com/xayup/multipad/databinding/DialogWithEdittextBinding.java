// Generated by view binder compiler. Do not edit!
package com.xayup.multipad.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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

public final class DialogWithEdittextBinding implements ViewBinding {
  @NonNull
  private final RelativeLayout rootView;

  @NonNull
  public final Button dweBtn1;

  @NonNull
  public final Button dweBtn2;

  @NonNull
  public final Button dweBtn3;

  @NonNull
  public final EditText dweEditText;

  @NonNull
  public final TextView dweTitle;

  private DialogWithEdittextBinding(@NonNull RelativeLayout rootView, @NonNull Button dweBtn1,
      @NonNull Button dweBtn2, @NonNull Button dweBtn3, @NonNull EditText dweEditText,
      @NonNull TextView dweTitle) {
    this.rootView = rootView;
    this.dweBtn1 = dweBtn1;
    this.dweBtn2 = dweBtn2;
    this.dweBtn3 = dweBtn3;
    this.dweEditText = dweEditText;
    this.dweTitle = dweTitle;
  }

  @Override
  @NonNull
  public RelativeLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static DialogWithEdittextBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static DialogWithEdittextBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.dialog_with_edittext, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static DialogWithEdittextBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.dwe_btn1;
      Button dweBtn1 = ViewBindings.findChildViewById(rootView, id);
      if (dweBtn1 == null) {
        break missingId;
      }

      id = R.id.dwe_btn2;
      Button dweBtn2 = ViewBindings.findChildViewById(rootView, id);
      if (dweBtn2 == null) {
        break missingId;
      }

      id = R.id.dwe_btn3;
      Button dweBtn3 = ViewBindings.findChildViewById(rootView, id);
      if (dweBtn3 == null) {
        break missingId;
      }

      id = R.id.dwe_editText;
      EditText dweEditText = ViewBindings.findChildViewById(rootView, id);
      if (dweEditText == null) {
        break missingId;
      }

      id = R.id.dwe_title;
      TextView dweTitle = ViewBindings.findChildViewById(rootView, id);
      if (dweTitle == null) {
        break missingId;
      }

      return new DialogWithEdittextBinding((RelativeLayout) rootView, dweBtn1, dweBtn2, dweBtn3,
          dweEditText, dweTitle);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
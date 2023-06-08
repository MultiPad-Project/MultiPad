package com.xayup.multipad.layouts.loadscreen;

import android.app.Activity;
import android.graphics.drawable.AnimationDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ViewFlipper;
import com.xayup.multipad.R;
import java.util.List;

public class LoadScreen {
    protected Activity context;
    protected ViewFlipper flipper;
    protected Animation anim;
    protected ViewGroup root;
    protected TextView text_state;

    public LoadScreen(Activity context, ViewGroup root) {
        this.context = context;
        this.root = root;
        this.flipper =
                context.getLayoutInflater()
                        .inflate(R.layout.loading_screen, root, true)
                        .findViewById(R.id.loading_screen_flipper);
        this.text_state = flipper.findViewById(R.id.loading_text_state);
    }

    public void show(long duration) {
        anim = AnimationUtils.loadAnimation(context, R.anim.fade_in_splash);
        anim.setDuration(duration);
        anim.setStartTime(0);
        anim.setStartOffset(0);
        flipper.startAnimation(anim);
        ((AnimationDrawable)
                        ((ImageView) flipper.findViewById(R.id.loading_logo_image)).getBackground())
                .start();
    }

    public void hide(long duration) {
        anim = AnimationUtils.loadAnimation(context, R.anim.fade_out_splash);
        anim.setDuration(duration);
        anim.setStartTime(0);
        anim.setStartOffset(0);
        flipper.startAnimation(anim);
        ((AnimationDrawable)
                        ((ImageView) flipper.findViewById(R.id.loading_logo_image)).getBackground())
                .stop();
        flipper.setVisibility(View.GONE);
    }
    
    public void updatedText(String text){
        text_state.setText(text);
    }
    
    public void remove(){
        root.removeView(flipper);
    }
    
    public void showErrorsList(List<String> errors, View.OnClickListener ok_click) {
        flipper.setOutAnimation(context, R.anim.fade_out_splash);
        flipper.getOutAnimation().setDuration(200);
        flipper.setInAnimation(context, R.anim.fade_in_splash);
        flipper.getInAnimation().setDuration(200);
        flipper.showNext();
        ((ListView) flipper.findViewById(R.id.loading_screen_error_list)).setAdapter(new ArrayAdapter(context, R.layout.simple_list_item, errors));
        ((Button) flipper.findViewById(R.id.loading_screen_error_button_ok)).setOnClickListener(ok_click);
    }
    
    public Animation getCurrentAnimation() {
        return this.anim;
    }
}

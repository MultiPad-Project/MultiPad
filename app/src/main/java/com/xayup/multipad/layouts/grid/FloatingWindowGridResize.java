package com.xayup.multipad.layouts.grid;

import android.content.Context;
import android.graphics.PixelFormat;
import android.view.*;
import android.widget.*;
import com.xayup.multipad.configs.GlobalConfigs;
import com.xayup.multipad.pads.GridPadsReceptor;

import com.xayup.multipad.R;

public abstract class FloatingWindowGridResize {
    public abstract GridPadsReceptor getGridPadsReceptor();
    protected TextView title;
    protected Spinner spinner;
    protected ViewGroup window;
    protected ViewGroup windowManager;
    protected WindowManager.LayoutParams windowParams;
    //Controls
    protected Switch part_switch;
    protected SeekBar grid_height;
    protected SeekBar grid_width;
    protected SeekBar grid_rotation;
    protected SeekBar grid_x_position;
    protected SeekBar grid_y_position;
    //Controls values
    protected TextView grid_height_text;
    protected TextView grid_width_text;
    protected TextView grid_rotation_text;
    protected TextView grid_x_position_text;
    protected TextView grid_y_position_text;
    // Only manage visibility
    protected View part_grid;
    protected View part_pads;
    // Only change var
    protected ViewGroup part;

    public float init_window_x, init_window_y;

    public FloatingWindowGridResize(Context context, ViewGroup toPlace){
        this.windowManager = toPlace;
        this.window = (ViewGroup) LayoutInflater.from(context).inflate(R.layout.floating_window_resize_grid, windowManager, false);
        this.title = window.findViewById(R.id.floating_window_resize_grid_header_title);
        this.spinner = window.findViewById(R.id.floating_window_resize_grid_header_spinner);
        this.part_switch = window.findViewById(R.id.floating_window_grid_resize_part_switch);
        this.grid_height = window.findViewById(R.id.floating_window_resize_grid_height);
        this.grid_width = window.findViewById(R.id.floating_window_resize_grid_width);
        this.grid_x_position = window.findViewById(R.id.floating_window_resize_grid_x_position);
        this.grid_y_position = window.findViewById(R.id.floating_window_resize_grid_y_position);
        this.grid_rotation = window.findViewById(R.id.floating_window_resize_grid_rotation);
        this.grid_height_text = window.findViewById(R.id.floating_window_resize_grid_height_text);
        this.grid_width_text = window.findViewById(R.id.floating_window_resize_grid_width_text);
        this.grid_x_position_text = window.findViewById(R.id.floating_window_resize_grid_x_position_text);
        this.grid_y_position_text = window.findViewById(R.id.floating_window_resize_grid_y_position_text);
        this.grid_rotation_text = window.findViewById(R.id.floating_window_resize_grid_rotation_text);
        this.part_grid = window.findViewById(R.id.floating_window_resize_grid_part_grid_text);
        this.part_pads = window.findViewById(R.id.floating_window_resize_grid_part_pads_text);

        //Set window functions
        //// HEADER ////
        window.findViewById(R.id.floating_window_resize_grid_header).setOnTouchListener(onTouch());
        window.findViewById(R.id.floating_window_resize_grid_close).setOnClickListener(v -> hide());
        window.findViewById(R.id.floating_window_resize_grid_minimize).setOnClickListener(
                v -> Toast.makeText(context, "Coming soon..", Toast.LENGTH_SHORT).show());
        spinner.setAdapter(new ArrayAdapter<>(context, android.R.layout.simple_list_item_1,
                android.R.id.text1, getGridPadsReceptor().getAllPadsNameList()));
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                title.setText(String.valueOf(adapterView.getItemAtPosition(i))); }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        //// SEEKBARS ////
        grid_width.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(b) part.getLayoutParams().width = i;
                grid_width_text.setText((context.getString(R.string.floating_window_resize_grid_width).concat(" " + i + "px"))); }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        grid_height.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(b) part.getLayoutParams().height = i;
                grid_height_text.setText((context.getString(R.string.floating_window_resize_grid_height).concat(" " + i + "px"))); }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        grid_rotation.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(b) part.setRotation((float) i-180);
                grid_rotation_text.setText((context.getString(R.string.floating_window_resize_grid_rotation).concat(" " + (i-180) + "º"))); }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        grid_x_position.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(b) getGridPadsReceptor().getGridByName((String) spinner.getSelectedItem()).getContainer().setX((float) i);
                grid_x_position_text.setText((context.getString(R.string.floating_window_resize_grid_x_position).concat(" " + i + "px"))); }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        grid_y_position.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(b) getGridPadsReceptor().getGridByName((String) spinner.getSelectedItem()).getContainer().setY((float) i);
                grid_y_position_text.setText((context.getString(R.string.floating_window_resize_grid_y_position).concat(" " + i + "px"))); }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        part_switch.setOnCheckedChangeListener((compoundButton, b) -> {
            if(part_switch.isChecked()) {
                part_grid.setAlpha(0.4f);
                part_pads.setAlpha(1f);
                part = getGridPadsReceptor().getGridByName((String) spinner.getSelectedItem()).getGridPads();
            } else {
                part_grid.setAlpha(1f);
                part_pads.setAlpha(0.4f);
                part = getGridPadsReceptor().getGridByName((String) spinner.getSelectedItem()).getRootPads();
            }
            updateShowValues();
            });
    }


     //// CALL THIS FIRST UPDATE!! ////
    private void updatePart(){
        part = (part_switch.isChecked()) ?
                getGridPadsReceptor().getGridByName((String) spinner.getSelectedItem()).getRootPads():
                getGridPadsReceptor().getGridByName((String) spinner.getSelectedItem()).getGridPads();
    }
    private void updateMaxMinValues(){
        grid_rotation.setMax(360); //Compatibility
        grid_height.setMax(GlobalConfigs.display_height); grid_width.setMax(GlobalConfigs.display_width);
        grid_x_position.setMax(GlobalConfigs.display_width); grid_y_position.setMax(GlobalConfigs.display_height);
    }
    private void updateShowValues(){
        grid_rotation.setProgress((int) part.getRotation()+180);
        grid_height.setProgress(part.getMeasuredHeight());
        grid_width.setProgress(part.getMeasuredWidth());
        grid_x_position.setProgress((int) part.getX());
        grid_y_position.setProgress((int) part.getY());
    }

    public View.OnTouchListener onTouch(){
        return (view, event) -> {
            if(event.getAction() == MotionEvent.ACTION_DOWN){
                init_window_x = event.getRawX() - window.getX();
                init_window_y = event.getRawY() - window.getY();
                return true;
            } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                window.setX(event.getRawX() - init_window_x);
                window.setY(event.getRawY() - init_window_y);
                return true;
            } else if (event.getAction() == MotionEvent.ACTION_UP){
                if(window.getX() < -(float) window.getWidth()/2)
                    window.setX(-(float) window.getWidth()/2);
                else if (window.getX() > (float) (windowManager.getWidth()-(window.getWidth()/2)))
                    window.setX((float) (windowManager.getWidth()-(window.getWidth()/2)));
                if(window.getY() < 0)
                    window.setY(0);
                else if (window.getY() > (float) (windowManager.getHeight()-(window.getHeight()/2)))
                    window.setY((float) (windowManager.getHeight()-(window.getHeight()/2)));
                return false;
            }
            view.performClick();
            return false;
        };
    }

    public void show(){
        //// UPDATES ////
        updatePart();
        updateMaxMinValues();
        // Add window to WindowManager
        windowManager.addView(window);
    }
    public void hide(){
        windowManager.removeView(window);
    }
}

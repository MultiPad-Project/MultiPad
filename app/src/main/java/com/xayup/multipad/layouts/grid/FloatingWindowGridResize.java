package com.xayup.multipad.layouts.grid;

import android.content.Context;
import android.view.*;
import android.widget.*;
import com.xayup.multipad.configs.GlobalConfigs;
import com.xayup.multipad.pads.GridPadsReceptor;

import com.xayup.multipad.R;

public abstract class FloatingWindowGridResize {
    public abstract GridPadsReceptor getGridPadsReceptor();
    protected Context context;
    protected TextView title;
    protected Spinner spinner;
    protected ViewGroup window;
    protected ViewGroup windowManager;
    protected View windowBackground;
    //Controls
    protected Switch part_switch;
    protected SeekBar grid_height;
    protected SeekBar grid_width;
    protected SeekBar grid_rotation;
    protected SeekBar grid_x_position;
    protected SeekBar grid_y_position;
    protected SeekBar grid_padding;
    //Controls values
    protected TextView grid_height_text;
    protected TextView grid_width_text;
    protected TextView grid_rotation_text;
    protected TextView grid_x_position_text;
    protected TextView grid_y_position_text;
    protected TextView grid_padding_text;
    //Get values
    protected boolean copied = false;
    protected Button copy_data;
    protected Button paste_data;
    protected Button save_data;
    //Data
    protected int grid_width_value = 0;
    protected int grid_height_value = 0;
    protected int grid_rotation_value = 0;
    protected int grid_padding_value = 0;
    protected float grid_x_value;
    protected float grid_y_value;
    // Only change var
    protected ViewGroup part;
    protected ViewGroup.MarginLayoutParams marginLayoutParams;
    protected ViewGroup viewGroup;

    public float init_window_x, init_window_y;

    public FloatingWindowGridResize(Context context, ViewGroup toPlace){
        this.context = context;
        this.windowManager = toPlace;
        this.window = (ViewGroup) LayoutInflater.from(context).inflate(R.layout.floating_window_resize_grid, windowManager, false);
        this.windowBackground = window.findViewById(R.id.floating_window_resize_grid_background);
        this.title = window.findViewById(R.id.floating_window_resize_grid_header_title);
        this.spinner = window.findViewById(R.id.floating_window_resize_grid_header_spinner);
        //this.part_switch = window.findViewById(R.id.floating_window_grid_resize_part_switch);
        this.grid_height = window.findViewById(R.id.floating_window_resize_grid_height);
        this.grid_width = window.findViewById(R.id.floating_window_resize_grid_width);
        this.grid_x_position = window.findViewById(R.id.floating_window_resize_grid_x_position);
        this.grid_y_position = window.findViewById(R.id.floating_window_resize_grid_y_position);
        this.grid_rotation = window.findViewById(R.id.floating_window_resize_grid_rotation);
        this.grid_padding = window.findViewById(R.id.floating_window_resize_grid_padding);
        this.grid_height_text = window.findViewById(R.id.floating_window_resize_grid_height_text);
        this.grid_width_text = window.findViewById(R.id.floating_window_resize_grid_width_text);
        this.grid_x_position_text = window.findViewById(R.id.floating_window_resize_grid_x_position_text);
        this.grid_y_position_text = window.findViewById(R.id.floating_window_resize_grid_y_position_text);
        this.grid_rotation_text = window.findViewById(R.id.floating_window_resize_grid_rotation_text);
        this.grid_padding_text = window.findViewById(R.id.floating_window_resize_grid_padding_text);
        this.copy_data = window.findViewById(R.id.floating_window_resize_grid_get_data_copy);
        this.paste_data = window.findViewById(R.id.floating_window_resize_grid_get_data_paste);
        this.save_data = window.findViewById(R.id.floating_window_resize_grid_get_data_save);

        //Set window functions
        //// HEADER ////
        window.findViewById(R.id.floating_window_resize_grid_header).setOnTouchListener(onTouch());
        window.findViewById(R.id.floating_window_resize_grid_close).setOnClickListener(v -> hide());
        window.findViewById(R.id.floating_window_resize_grid_minimize).setOnClickListener(
                v -> windowBackground.setVisibility(
                        (windowBackground.getVisibility() == View.VISIBLE) ? View.GONE : View.VISIBLE));
        spinner.setAlpha(1f);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                title.setText(String.valueOf(adapterView.getItemAtPosition(i)));
                spinner.setPrompt("");
                updatePart();
                updateMaxMinValues();
                updateShowValues();
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });
        spinner.setPrompt("");

        //// SEEKBARS ////
        grid_width.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(b) setGridWidth(i);
                grid_width_text.setText((context.getString(R.string.floating_window_resize_grid_width).concat(" " + i + "px"))); }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        grid_height.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(b) setGridHeight(i);
                grid_height_text.setText((context.getString(R.string.floating_window_resize_grid_height).concat(" " + i + "px"))); }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        grid_rotation.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(b) setGridRotation(i);
                grid_rotation_text.setText((context.getString(R.string.floating_window_resize_grid_rotation).concat(" " + (i-180) + "º"))); }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        grid_x_position.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(b) setGridPositionX((float) i);
                grid_x_position_text.setText((context.getString(R.string.floating_window_resize_grid_x_position).concat(" " + i + "px"))); }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        grid_y_position.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(b) setGridPositionY(i);
                grid_y_position_text.setText((context.getString(R.string.floating_window_resize_grid_y_position).concat(" " + i + "px"))); }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        grid_padding.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(b) setGridPadding(i);
                grid_padding_text.setText((context.getString(R.string.floating_window_resize_grid_padding).concat(" " + i + "px"))); }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                marginLayoutParams = null;
                viewGroup = null;
            }
        });
        copy_data.setOnClickListener(v -> {
            grid_rotation_value = grid_rotation.getProgress() - 180;
            grid_height_value = grid_height.getProgress();
            grid_width_value = grid_width.getProgress();
            grid_x_value = grid_x_position.getProgress();
            grid_y_value = grid_y_position.getProgress();
            grid_padding_value = grid_padding.getProgress();
            copied = true;
        });
        paste_data.setOnClickListener(v -> {
            if(copied){
                setGridRotation(grid_rotation_value + 180);
                setGridHeight(grid_height_value);
                setGridWidth(grid_width_value);
                setGridPositionX(grid_x_value);
                setGridPositionY(grid_y_value);
                setGridPadding(grid_padding_value);
            } else{
                Toast.makeText(context, context.getString(R.string.floating_window_resize_grid_get_data_past_error_message),
                        Toast.LENGTH_SHORT).show();
            }
        });
        save_data.setOnClickListener(v -> {
            Toast.makeText(context, "Coming soon...", Toast.LENGTH_SHORT).show();
        });
    }

    public void setGridHeight(int value){
        ViewGroup.LayoutParams param = part.getLayoutParams();
        param.height = value;
        part.setLayoutParams(param);
        grid_height.setProgress(value);
    }
    public void setGridWidth(int value){
        ViewGroup.LayoutParams param = part.getLayoutParams();
        param.width = value;
        part.setLayoutParams(param);
        grid_width.setProgress(value);
    }
    public void setGridRotation(int value){
        part.setRotation((float) value-180);
    }
    public void setGridPositionX(float value){
        part.setX(value);
    }
    public void setGridPositionY(float value){
        part.setY(value);
    }
    public void setGridPadding(int value){
        marginLayoutParams.setMargins(value, value, value, value);
        viewGroup.requestLayout();
    }

    //// CALL THIS FIRST UPDATE!! ////
    public void updateSpinner(){
        spinner.setAdapter(new ArrayAdapter<>(context, android.R.layout.simple_list_item_1,
                android.R.id.text1, getGridPadsReceptor().getAllPadsNameList()));
    }
    private void updatePart(){
        part = getGridPadsReceptor().getGridByName((String) spinner.getSelectedItem()).getContainer();
        viewGroup = getGridPadsReceptor().getGridByName((String) spinner.getSelectedItem()).getGridPads();
        marginLayoutParams = (ViewGroup.MarginLayoutParams) viewGroup.getLayoutParams();
    }

    private void updateMaxMinValues(){
        grid_rotation.setMax(360); //Compatibility
        grid_height.setMax(GlobalConfigs.display_height); grid_width.setMax(GlobalConfigs.display_width);
        grid_x_position.setMax(GlobalConfigs.display_width); grid_y_position.setMax(GlobalConfigs.display_height);
        grid_padding.setMax(GlobalConfigs.display_height/2);
    }
    private void updateShowValues(){
        grid_rotation.setProgress((int) part.getRotation()+180);
        grid_height.setProgress(part.getMeasuredHeight());
        grid_width.setProgress(part.getMeasuredWidth());
        grid_x_position.setProgress((int) part.getX());
        grid_y_position.setProgress((int) part.getY());
        grid_padding.setProgress(marginLayoutParams.bottomMargin);
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
        updateSpinner();
        updatePart();
        updateMaxMinValues();
        updateShowValues();
        // Add window to WindowManager
        windowManager.addView(window);
        GlobalConfigs.floating_window_grid_resize_visible = true;
    }
    public void hide(){
        windowManager.removeView(window);
        GlobalConfigs.floating_window_grid_resize_visible = false;
    }
}

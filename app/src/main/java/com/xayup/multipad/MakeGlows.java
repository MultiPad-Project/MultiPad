package com.xayup.multipad;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.xayup.multipad.configs.GlobalConfigs;
import com.xayup.multipad.pads.GridPadsReceptor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MakeGlows {
	private final List<String> ignore = Arrays.asList(new String[] { "00", "09", "90", "99" });
	private final String glowInitId = "100";
	private int posicao_inicial, glowSize, glowPadSize, screenH, screenW, glowChainSize, glowPivotXY, glowChainPivotXY,
			padWH, centerPad, inicial_pos_y, inicial_pos_x;
    public float glowIntensity, glowChainIntensity;
	private Map<String, ImageView> glows;
	private Activity context;
	private ViewGroup.LayoutParams chainParams;
	private ViewGroup.LayoutParams padParams;

	public MakeGlows(Activity context, int padWH, int glowPadSize, int glowChainSize, int screenW, int screenH) {
		this.context = context;
        this.glowIntensity = 180;
        this.glowChainIntensity = 180;
		this.glowPadSize = glowPadSize;
		this.glowChainSize = glowChainSize;
		this.padWH = padWH;
		this.centerPad = padWH / 2;
		this.screenH = screenH;
		this.screenW = screenW;
		this.chainParams = new RelativeLayout.LayoutParams(glowChainSize, glowChainSize);
		this.padParams = new RelativeLayout.LayoutParams(glowPadSize, glowPadSize);
		posicao_inicial = ((screenW - screenH) / 2) + ((screenH - GlobalConfigs.display_height) / 2);

	}

	//calculos
	private void calc(int grid_size, int layout_mode) {
		padWH = grid_size;
		centerPad = padWH/2;
		posicao_inicial = ((screenW - screenH) / 2) + ((screenH - GlobalConfigs.display_height) / 2);
		int pos_init = posicao_inicial;
		if (layout_mode == GridPadsReceptor.PadLayoutMode.LAYOUT_MK2_MODE) {
			pos_init -= padWH;
		}
		inicial_pos_x = pos_init + centerPad;
		inicial_pos_y = centerPad + ((screenH - GlobalConfigs.display_height) / 2);
	}

	public void resize(int grid_size) {
		// calc(grid_size);
		int to_y = inicial_pos_y;
		int to_x = inicial_pos_x;
		for (int l = 0; l <= 9; l++) {
			for (int c = 0; c <= 9; c++) {
				if (!ignore.contains(l + "" + c)) {
					String key = l + "" + c;
					if (l == 0)
						key = "" + c;
					glows.get(key).setVisibility(View.GONE);
					glows.get(key).setLayoutParams(size(l, c));
					glows.get(key).setX(to_x - (glowSize / 2));
					glows.get(key).setY(to_y - (glowSize / 2));
					glows.get(key).setVisibility(View.VISIBLE);
				}

				to_x += padWH; //adicinar o glow a cada coluna;
			}
			to_x = inicial_pos_x; //voltar o ponteiro x para o primeiro botaona coluna 1
			to_y += padWH; //por o ponteiro para a linha seguinte,
		}
	}

	public void setOnGlows(int grid_size, int mode) {
		calc(grid_size, mode);

		if (glows != null) {
			resize(grid_size);
		} else {
			int to_y = inicial_pos_y;
			int to_x = inicial_pos_x;
			glows = new HashMap<String, ImageView>();
			RelativeLayout src = context.findViewById(R.id.layoutbackground);
			for (int l = 0; l <= 9; l++) {
				for (int c = 0; c <= 9; c++) {
					if (!ignore.contains(l + "" + c)) {
						ImageView glowImg = new ImageView(context);
						glowImg.setImageDrawable(context.getDrawable(R.drawable.glow));
						src.addView(glowImg); //ja add logo ae
						glowImg.setLayoutParams(size(l, c));
						glowImg.setX(to_x - (glowSize / 2));
						glowImg.setY(to_y - (glowSize / 2));
						glowImg.setAlpha(0.0f);

						if (l == 0) {
							glowImg.setId(Integer.parseInt(glowInitId + c));
							glows.put("" + c, glowImg);
						} else {
							glowImg.setId(Integer.parseInt(glowInitId + l + c));
							glows.put(l + "" + c, glowImg);
						}
						if (true/*MK2 MODE*/ && (l == 9 || c == 0)) {
							glowImg.setVisibility(View.GONE);
							glowImg.setTag("hide");
						}

						//	System.out.println("" + to_x + to_y + glowSize);
					}
					to_x += padWH; //adicinar o glow a cada coluna;
				}
				to_x = inicial_pos_x; //voltar o ponteiro x para o primeiro botaona coluna 1
				to_y += padWH; //por o ponteiro para a linha seguinte, 
			}
		}
	}

	public void setOffGlows() {
		if (glows != null) {
			for (String key : glows.keySet()) {
				glows.get(key).setVisibility(View.GONE);
			}
		}
	}

	public void offGlows() {
		if (glows != null) {
			for (String key : glows.keySet()) {
				glows.get(key).setAlpha(0.0f);
			}
		}
	}

	public void mk2Glows(int newSize, int padSize, boolean mk2) {
		centerPad = padSize / 2;
		padWH = padSize;
		//calc(padSize);
		int to_x = inicial_pos_x;
		int to_y = inicial_pos_y;
		for (int l = 0; l <= 9; l++) {
			for (int c = 0; c <= 9; c++) {
				if (!ignore.contains(l + "" + c)) {
					String key = l + "" + c;
					if (l == 0)
						key = "" + c;
					glows.get(key).setVisibility(View.GONE);
					glows.get(key).setLayoutParams(size(l, c));
					glows.get(key).setX(to_x - (glowSize / 2));
					glows.get(key).setY(to_y - (glowSize / 2));
					glows.get(key).setTag("hide");
					if (!(mk2 && (l == 9 || c == 0))) {
						glows.get(key).setVisibility(View.VISIBLE);
						glows.get(key).setTag("visible");
					}
				}
				to_x += padSize;
			}
			to_x = inicial_pos_x;
			to_y += padSize;
		}
	}

	public void changeCfg(int radius, float alpha, boolean chain, int layout_mode) {
		if (chain) {
			glowChainIntensity = alpha;
			glowChainSize = radius;
		} else {
			glowIntensity = alpha;
			glowPadSize = radius;
		}
		glowSize = radius;
		calc(padWH, layout_mode);

		int to_x = inicial_pos_x;
		int to_y = inicial_pos_y;

		for (int l = 0; l <= 9; l++) {
			for (int c = 0; c <= 9; c++) {
				if (!ignore.contains(l + "" + c)) {
					String key = l + "" + c;
					if (l == 0) {
						key = "" + c;
					}
					boolean skip = false;
					if (chain) {
						if (VariaveisStaticas.btnsIDList.contains(key))
							skip = true;
					} else {
						if (!VariaveisStaticas.btnsIDList.contains(key))
							skip = true;
					}
					System.out.println(skip);
					if (!skip) {
						if (layout_mode != GridPadsReceptor.PadLayoutMode.LAYOUT_MK2_MODE && (key == "1" || key == "91"))
							to_x += padWH;
						glows.get(key).setVisibility(View.GONE);
						glows.get(key).getLayoutParams().height = radius;
						glows.get(key).getLayoutParams().width = radius;
						glows.get(key).setX(to_x - (radius / 2));
						glows.get(key).setY(to_y - (radius / 2));
						if (glows.get(key).getTag() != "hide") {
							glows.get(key).setVisibility(View.VISIBLE);
						}
					}
				}
				to_x += padWH;
			}
			to_y += padWH;
			to_x = inicial_pos_x;
		}
	}

	public int radius(boolean chain) {
		if (chain) {
			return glowChainSize;
		}
		return glowPadSize;
	}

	private ViewGroup.LayoutParams size(int l, int c) {
		if (l == 0 || l == 9 || c == 0 || c == 9) {
			glowSize = glowChainSize;
			return chainParams;
		}
		glowSize = glowPadSize;
		return padParams;

	}
}
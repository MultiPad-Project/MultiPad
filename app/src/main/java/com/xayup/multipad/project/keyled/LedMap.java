/**
* Codigo padrao para gerenciar o mapa de leds
* led_count e uma matrix de 6 arrays.
* O index do array 1 (Comentado como "chain") devera ser de 0 a 23. (chain sequecionada).
* O index do array 2 (Comentado como "pad_x") devera ser de 0 a 7. (cordenada X do botao).
* O index do array 3 (Comentado como "pad_y") devera ser de 0 a 7. (cordenada Y do botao).
* O index do array 4 (Comentado como "sequence") devera ser um numero menor que o tamanho dessa array.
* (as leds mapeadas para este botao, ou seja, caso haja mais de um arquivo de led para este bota, este array sera maior que 1)
* O index do array 5 (Comentado como "frame") contera as frames obtidos na leitura do arquvio da led, ou seja, cada frame e uma linha no arquivo lido.
* O array retornado sera um array com 4 valores. o valor 1 (index 0) sera o tipo da led (on, off). O valor 2 (index 1) sera a velocidade (ou a cor da led, neste caso) de 0 a 126.
+ O valor 3 (index 2) sera a cordenada X da pad e o valor 4 (index 3) sera a cordenada Y da pad.
*/

package com.xayup.multipad.project.keyled;
import com.xayup.multipad.project.keyled.KeyLEDReader;
import java.util.Arrays;
import java.util.Map;

public class LedMap implements KeyLEDReader.KeyLEDMap {
    protected int led_count = 0;
    protected int[/*chain*/][/*pad_x*/][/*pad_y*/][/*sequence*/][/*frames*/][/*TYPE, VALUE, PAD_X, PAD_Y*/] led_map;
    public LedMap(){
        led_map = new int[24][10][10]/*Dynamic add with put()*/[0][0][0];
    }
    @Override
    public void putFrame(int chain, int x, int y, int sequence, int[/*TYPE, VALUE, PAD_X, PAD_Y*/] led_frame){
        int[][] tmp = led_map[chain][x][y][sequence];
        tmp = Arrays.copyOf(tmp, tmp.length+1);
        tmp[tmp.length-1] = led_frame;
        led_map[chain][x][y][sequence] = tmp;
    }
    @Override
    public void putSequence(int chain, int x, int y, int[/*FRAME*/][/*TYPE, VALUE, PAD_X, PAD_Y*/] led_frames_sequence){
        int[][][] tmp = led_map[chain][x][y];
        tmp = Arrays.copyOf(tmp, tmp.length+1);
        tmp[tmp.length-1] = led_frames_sequence;
        led_map[chain][x][y] = tmp;
        led_count++;
    }
    @Override
    public int[/*FRAMES*/][/*TYPE, VALUE, PAD_X, PAD_Y*/] getLedData(int chain, int x, int y, int sequence){
        return led_map[chain][x][y][sequence];
    }
    @Override
    public int framesCount(int chain, int x, int y, int sequence){
        return led_map[chain][x][y][sequence].length;
    }
    @Override
    public int sequenceCount(int chain, int x, int y){
        return led_map[chain][x][y].length;
    }
    @Override
    public int ledsCount(){
        return led_count;
    }
}

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
* O valor 3 (index 2) sera a cordenada X da pad e o valor 4 (index 3) sera a cordenada Y da pad. O valor 5 (index 4) sera a etiqueta para identificar em qual Pads_layout sera reproduzida a led.
*/

package com.xayup.multipad.projects.project.keyled;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class LedMap implements KeyLEDReader.KeyLEDMap {
    protected int led_count = 0;
    protected Map<String, KeyLEDData[]> led_map;
    protected byte[][] sequence_index;
    public LedMap(){
        led_map = new HashMap<>();
        sequence_index = new byte[10][10];
    }
    @Override
    public void clear(){
        led_count = 0;
        led_map.clear();
        led_map = null;
        resetSequencesIndex();
        sequence_index = null;
    }
    public void resetSequencesIndex(){
        for(final byte[] s : sequence_index){
            Arrays.fill(s, (byte) 0);
        }
    }
    @Override
    public void putSequence(int c, int x, int y, KeyLEDData led_data){
        String key = makeKey(c, x, y);
        if(led_map.get(key) == null){
            led_map.put(key, new KeyLEDData[]{led_data});
        } else {
            KeyLEDData[] tmp = led_map.get(key);
            tmp = Arrays.copyOf(Objects.requireNonNull(tmp), tmp.length+1);
            tmp[tmp.length-1] = led_data;
            led_map.put(key, tmp);
        }
        led_count++;
    }
    public String makeKey(int c, int x, int y){
        return c + String.valueOf(x) + y;
    }
    @Override
    public KeyLEDData getLedData(int c, int x, int y){
        int length;
        String key = makeKey(c, x, y);
        KeyLEDData[] tmp = led_map.get(key);
        if(tmp == null) return null;
        if((length = tmp.length) > 0){
            if(sequence_index[x][y] >= length){
                sequence_index[x][y] = 0;
            }
            return tmp[sequence_index[x][y]++];
        }
        return null;
    }
    @Override
    public KeyLEDData getLedData(int chain, int x, int y, int sequence){
        return null;
    }

    @Override
    public KeyLEDData newFrameData(){
        return new KeyLEDData();
    }
    @Override
    public int framesCount(int chain, int x, int y, int sequence){
        return Objects.requireNonNull(led_map.get(makeKey(chain, x, y)))[sequence].length();
    }
    @Override
    public int sequenceCount(int chain, int x, int y){
        return Objects.requireNonNull(led_map.get(makeKey(chain, x, y))).length;
    }
    @Override
    public int ledsCount(){
        return led_count;
    }
}

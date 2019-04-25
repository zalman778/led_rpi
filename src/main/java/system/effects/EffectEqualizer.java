package system.effects;

import system.config.Config;
import system.model.Led;
import system.model.LedStrip;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/*
    equalizer effect - all the whole led strip is divided into Intervals, where each is
    responsible for an interval of frequency. the color of each bar varies from min and max
    value in HSB system.
 */
public class EffectEqualizer extends Effect {

    private ArrayList<ArrayList<Double>> old_hzAmplArr;
    //private Double maxHz;
    //private Double minHz;

    private double intMaxAmplsArr[];

    private float[] hsbArr;
    private float[] silenceArr;
    private float currVal;

    private float diffMinMax;
    private float additionalDiff;

    int red = 1;
    int green = 1;
    int blue = 1;

    public EffectEqualizer(
            LedStrip ledStrip, HashMap<String, Object> initParams) {
        super(ledStrip, initParams);
        //old_hzAmplArr = (ArrayList<ArrayList<Double>>) initParams.get("hzAmplArr");
        //maxHz = (Double) initParams.get("maxHz");
        //minHz = (Double) initParams.get("minHz");

        //System.out.println("initialized effeq with: arr"+hzAmplArr.hashCode());
        //System.out.println("init of max="+maxHz.hashCode()+" : "+maxHz);
    }

    @Override
    public void tick(ArrayList<ArrayList<Double>> hzAmplArr, Double maxHz, Double minHz) {
        ///System.out.println("inside tick:"+maxHz);
        //cutting min-max to INT_CNT intervals
        int deltaInt = (int) Math.round((maxHz - minHz) / Config.INT_CNT);

        //calculation HSB-color limits from Cfg
        float min_hsb_val_in_1 = Config.MIN_HSB_VAL / 360.0f;
        float max_hsb_val_in_1 = Config.MAX_HSB_VAL / 360.0f;

        //array of max Ampl for each Interval
        intMaxAmplsArr = new double[Config.INT_CNT];
        Arrays.fill(intMaxAmplsArr, 0);

        for (int i = 0; i < Config.INT_CNT - 1; i++) {
            double maxCurrAmpl = 0;
            for (int j = 0; j < hzAmplArr.size(); j++) {
                if (hzAmplArr.get(j).get(0) > i * deltaInt && hzAmplArr.get(j).get(0) < (i + 1) * deltaInt) {
                    if (hzAmplArr.get(j).get(1) > maxCurrAmpl) {
                        maxCurrAmpl = hzAmplArr.get(j).get(1);
                    }
                }
            }
            intMaxAmplsArr[i] = maxCurrAmpl;

            //update all-time peaks;
            if (Config.maxAmplArrAllTime[i] < intMaxAmplsArr[i])
                Config.maxAmplArrAllTime[i] = intMaxAmplsArr[i];
        }

        hsbArr = new float[Config.INT_CNT - 1];
        silenceArr = new float[Config.INT_CNT - 1]; //массив тишины- - выключаем свет, если нет


        for (int i = 0; i < Config.INT_CNT - 1; i++) {



            currVal = (float) ((intMaxAmplsArr[i]) / Config.maxAmplArrAllTime[i]);

            //Список последних аплитуд и сравнение со средним
            //если  не полный, то замолняем
            if (Config.lastXAmplList.size() < Config.MAX_LAST_AMPL_LIST_SIZE) {
                Config.lastXAmplList.addFirst(intMaxAmplsArr[i]);
            } else {
                //если полный, то удаляем конец, добавляем голову
                if (Config.lastXAmplList.size() > 0) {
                    Config.lastXAmplList.removeLast();
                }
                Config.lastXAmplList.addFirst(intMaxAmplsArr[i]);

                //проверка всего списка на макс, если меньше, то обновляем макс
                boolean wasNoMaxInList = true;
                //System.out.println("compare:");
                for (Double x : Config.getLastXAmplList()) {
                    //System.out.print(x+" ");
                    if (x > Config.MIN_SOUND_AMPL_FILTER_PRC * Config.currentMaxAmplVal / 100) {
                        wasNoMaxInList = false;
                        break;
                    }
                }
                //System.out.println();
                //System.out.println("was comparing with "+(MIN_SOUND_AMPL_FILTER_PRC * currentMaxAmplVal));
                if (wasNoMaxInList) {
                    Config.reset();
                    //System.out.println("reset");
                }
            }


            //Обновление макс аплитуды
            if (intMaxAmplsArr[i] > Config.currentMaxAmplVal) {
                Config.currentMaxAmplVal = intMaxAmplsArr[i];
            }

            if (intMaxAmplsArr[i] > (Config.currentMaxAmplVal * Config.MIN_SOUND_AMPL_FILTER_PRC / 100))
                silenceArr[i] = 1;
            else
                silenceArr[i] = 0;


            diffMinMax = max_hsb_val_in_1 - min_hsb_val_in_1;
            additionalDiff = diffMinMax * currVal;
            currVal = min_hsb_val_in_1 + additionalDiff;
            hsbArr[i] = currVal;
        }

        //writing all HSB int to Led arrays
        int ledIntDelta = Config.LED_CNT / Config.INT_CNT;

        for (int i = 0; i < Config.INT_CNT - 1; i++) {
            //default light, if current sound peak didnt reach its max val
            red = 1;
            green = 1;
            blue = 1;

            if (silenceArr[i] == 1) {
                int rgb = Color.HSBtoRGB(hsbArr[i], 1.0f, 1.0f);
                red = (rgb >> 16) & 0xFF;
                green = (rgb >> 8) & 0xFF;
                blue = rgb & 0xFF;
            }

            for (int j = i * ledIntDelta; j < (i + 1) * ledIntDelta; j++) {
                ledStrip.set(j, new Led(red, green, blue, Config.LED_BRIGHT));
            }


        }
    }

    @Override
    public boolean isEnded() {
        return false;
    }
}

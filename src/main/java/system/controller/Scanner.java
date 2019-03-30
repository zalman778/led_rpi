package system.controller;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.io.TarsosDSPAudioInputStream;
import be.tarsos.dsp.io.jvm.JVMAudioInputStream;
import be.tarsos.dsp.util.fft.FFT;
import org.apache.log4j.Logger;
import system.config.Config;
import system.model.Led;
import system.model.LedStrip;

import javax.sound.sampled.*;
import java.awt.*;
import java.util.Arrays;


/**
 * Created by Hiwoo on 06.02.2018.
 */
public class Scanner {
    final static Logger logger = Logger.getLogger(Scanner.class);
    final int bufferSize = 4096;
    final int fftSize = bufferSize / 2;
    final int sampleRate = 48000;//44100;


    private AudioDispatcher audioDispatcher;
    private AudioProcessor audioProcessor;
    private LedStrip ledStrip;




    public LedStrip getLedStrip() {
        return ledStrip;
    }

    public void setLedStrip(LedStrip ledStrip) {
        this.ledStrip = ledStrip;
    }

    public void run() {

        audioDispatcher.run();
    }



    public void initialize()  {

        //audioDispatcher = AudioDispatcherFactory.fromDefaultMicrophone(sampleRate, 4096, 0);

        //creating from default microphone:
        int audioBufferSize = 4096;

        try {


            AudioFormat format = new AudioFormat(48000, 16, 1, true, true);
//        System.out.println("got  format:");
//        System.out.println(format.toString());
            TargetDataLine line = AudioSystem.getTargetDataLine(format);
//        System.out.println("got line:");
//        System.out.println(line.getLineInfo().toString());

            line.open(format, audioBufferSize);
            line.start();
            AudioInputStream stream = new AudioInputStream(line);
            TarsosDSPAudioInputStream audioStream = new JVMAudioInputStream(stream);
            audioDispatcher = new AudioDispatcher(audioStream, audioBufferSize, 0);

            audioProcessor = new AudioProcessor() {

                FFT fft = new FFT(bufferSize);
                final float[] amplitudes = new float[fftSize];

                double[][] hzAmplArr;
                double minHz;
                double maxHz;
                double intMaxAmplsArr[];

                float[] hsbArr;
                float[] silenceArr;
                float currVal;

                float diffMinMax;
                float additionalDiff;

                int red = 1;
                int green = 1;
                int blue = 1;

                public boolean process(AudioEvent audioEvent) {
                    //System.out.println("new loop of process:");

                    float[] audioBuffer = audioEvent.getFloatBuffer();
                    fft.forwardTransform(audioBuffer);
                    fft.modulus(audioBuffer, amplitudes);


                    //creating array of Hz - Ampl value
                    hzAmplArr = new double[amplitudes.length][2];
                    minHz = Config.MAX_HZ;
                    maxHz = 0;

                    for (int i = 0; i < amplitudes.length; i++) {

                        //System.out.printf("Amplitude at %3d Hz: %8.3f     ", (int) fft.binToHz(i, sampleRate) , amplitudes[i]);
                        hzAmplArr[i][0] = fft.binToHz(i, sampleRate);

                        //специальное занижение ампитуды частоты (сабик громкий)
                        if (hzAmplArr[i][0] < Config.LOW_HZ_MLT_HIGH_BOUND)
                            hzAmplArr[i][1] = amplitudes[i] * Config.LOW_HZ_MLT;
                        else if (hzAmplArr[i][1] >= Config.MID_HZ_MLT_LOW_BOUND && hzAmplArr[i][1] < Config.MID_HZ_MLT_HIGH_BOUND)
                            hzAmplArr[i][1] = amplitudes[i] * Config.MID_HZ_MLT;
                        else
                            hzAmplArr[i][1] = amplitudes[i] * Config.HIGH_HZ_MLT;


                        //min and max
                        if (minHz > hzAmplArr[i][0])
                            minHz = hzAmplArr[i][0];
                        if (maxHz < hzAmplArr[i][0])
                            maxHz = hzAmplArr[i][0];
                    }
                    //System.out.println(minHz+" "+maxHz);
                    //limiting maxHz
                    if (maxHz > Config.MAX_HZ_LIMIT)
                        maxHz = Config.MAX_HZ_LIMIT;
                    //cutting min-max to INT_CNT intervals
                    int deltaInt = (int) Math.round((maxHz - minHz) / Config.INT_CNT);
                    //array of max Ampl for each Interval
                    intMaxAmplsArr = new double[Config.INT_CNT];
                    Arrays.fill(intMaxAmplsArr, 0);

                    for (int i = 0; i < Config.INT_CNT - 1; i++) {
                        double maxCurrAmpl = 0;
                        for (int j = 0; j < hzAmplArr.length; j++) {
                            if (hzAmplArr[j][0] > i * deltaInt && hzAmplArr[j][0] < (i + 1) * deltaInt) {
                                if (hzAmplArr[j][1] > maxCurrAmpl) {
                                    maxCurrAmpl = hzAmplArr[j][1];
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
                        //calculation HSB-color limits from Cfg
                        float min_hsb_val_in_1 = Config.MIN_HSB_VAL / 360.0f;
                        float max_hsb_val_in_1 = Config.MAX_HSB_VAL / 360.0f;


                        currVal = (float) ((intMaxAmplsArr[i]) / Config.maxAmplArrAllTime[i]);

                        //Список последних аплитуд и сравнение со средним
                        //если  не полный, то замолняем
                        if (Config.currAmplListSize < Config.MAX_LAST_AMPL_LIST_SIZE) {
                            Config.lastXAmplList.addFirst(intMaxAmplsArr[i]);
                            Config.currAmplListSize++;
                        } else {
                            //если полный, то удаляем конец, добавляем голову
                            Config.lastXAmplList.removeLast();
                            Config.lastXAmplList.addFirst(intMaxAmplsArr[i]);

                            //проверка всего списка на макс, если меньше, то обновляем макс
                            boolean wasNoMaxInList = true;
                            //System.out.println("compare:");
                            for (Double x : Config.getLastXAmplList()) {
                                //System.out.print(x+" ");
                                if (x > Config.MIN_SOUAND_AMPL_FILTER_PRC * Config.currentMaxAmplVal / 100) {
                                    wasNoMaxInList = false;
                                    break;
                                }
                            }
                            //System.out.println();
                            //System.out.println("was comparing with "+(MIN_SOUAND_AMPL_FILTER_PRC * currentMaxAmplVal));
                            if (wasNoMaxInList) {
                                Config.reset();
                                //System.out.println("reset");
                            }
                        }


                        //Обновление макс аплитуды
                        if (intMaxAmplsArr[i] > Config.currentMaxAmplVal) {
                            Config.currentMaxAmplVal = intMaxAmplsArr[i];
                        }

                        if (intMaxAmplsArr[i] > (Config.currentMaxAmplVal * Config.MIN_SOUAND_AMPL_FILTER_PRC / 100))
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
                            ledStrip.set(j, new Led(red, green, blue, 0.61f));
                        }


                    }
                    //logger.info("fft done: curr peak = " + Config.currentMaxAmplVal);
                    return true;
                }

                @Override
                public void processingFinished() {

                }
            };
            audioDispatcher.addAudioProcessor(audioProcessor);
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
    }
}
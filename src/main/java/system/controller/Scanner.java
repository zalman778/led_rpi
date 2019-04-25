package system.controller;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.io.TarsosDSPAudioInputStream;
import be.tarsos.dsp.io.jvm.JVMAudioInputStream;
import be.tarsos.dsp.util.fft.FFT;
import org.apache.log4j.Logger;
import system.config.Config;
import system.effects.Effect;
import system.effects.EffectEqualizer;
import system.effects.EffectsHandler;
import system.model.Led;
import system.model.LedStrip;

import javax.sound.sampled.*;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;


/**
 * Created by Hiwoo on 06.02.2018.
 */
public class Scanner {
    private static Logger logger = Logger.getLogger(Scanner.class);
    private int bufferSize = 4096;
    private int fftSize = bufferSize / 2;
    private int sampleRate = 48000;

    //массив: частота - амплитуда

    //private Double[][] hzAmplArr = new Double[fftSize][2];
    private ArrayList<ArrayList<Double>> hzAmplArr = new ArrayList<>();
    private Double maxHz = 0d;
    private Double minHz = 0d;


    private AudioDispatcher audioDispatcher;
    private AudioProcessor audioProcessor;
    private LedStrip ledStrip;

    private AudioFormat format;
    private TargetDataLine line;
    private AudioInputStream stream;
    private TarsosDSPAudioInputStream audioStream;

    private EffectsHandler effectsHandler;






    public LedStrip getLedStrip() {
        return ledStrip;
    }

    public void setLedStrip(LedStrip ledStrip) {
        this.ledStrip = ledStrip;
    }

    public void run() {

        audioDispatcher.run();
    }

    private void clear() {
        //cleaning previous
        try {

            if (audioDispatcher != null) {
                audioDispatcher.stop();
                audioDispatcher = null;
            }

            if (audioStream != null) {
                audioStream.close();
            }

            if (stream != null) {
                stream.close();
            }

            if (line != null) {
                line.close();
            }

            if (format != null) {
                format = null;
            }

            System.gc();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void initialize()  {

        clear();

        try {

            format = new AudioFormat(sampleRate, 16, 1, true, true);
            line = AudioSystem.getTargetDataLine(format);
            line.open(format, bufferSize);
            line.start();

            stream = new AudioInputStream(line);
            audioStream = new JVMAudioInputStream(stream);
            audioDispatcher = new AudioDispatcher(audioStream, bufferSize, 0);




            final Effect effect;
            //choosing the effect, depending on config:
            if (Config.ASYM_MODE == 1) {
                HashMap<String, Object> paramsMap = new HashMap<>();
                paramsMap.put("hzAmplArr", hzAmplArr);
                paramsMap.put("maxHz", maxHz);
                paramsMap.put("minHz", minHz);
//                System.out.println("writted init params: arr="+hzAmplArr.hashCode()
//                +"; max="+maxHz.hashCode()
//                );
                effect = new EffectEqualizer(ledStrip, paramsMap);
            } else
                effect = null;



            audioProcessor = new AudioProcessor() {

                FFT fft = new FFT(bufferSize);

                float[] amplitudes = new float[fftSize];


                public boolean process(AudioEvent audioEvent) {
                    //System.out.println("new loop of process:");

                    float[] audioBuffer = audioEvent.getFloatBuffer();
                    fft.forwardTransform(audioBuffer);
                    fft.modulus(audioBuffer, amplitudes);


                    //creating array of Hz - Ampl value
                    //hzAmplArr = new Double[amplitudes.length][2];
                    //hzAmplArr = new ArrayList<>(Collections.nCopies(amplitudes.length, new ArrayList<>()));
                    hzAmplArr = new ArrayList<>();
                    for (int i = 0; i < amplitudes.length; i++) {
                        hzAmplArr.add(new ArrayList<>());
                    }

                    minHz = (double) Config.MAX_HZ;
                    maxHz = 0d;

                    for (int i = 0; i < amplitudes.length; i++) {

                        //System.out.printf("Amplitude at %3d Hz: %8.3f     ", (int) fft.binToHz(i, sampleRate) , amplitudes[i]);
                        //получаем частоту
                        //hzAmplArr[i][0] = fft.binToHz(i, sampleRate);
                        hzAmplArr.get(i).add(fft.binToHz(i, sampleRate));

                        //специальное занижение ампитуды частоты (сабик громкий)
                        if (hzAmplArr.get(i).get(0) < Config.MID_HZ_MLT_LOW_BOUND)
                            //hzAmplArr[i][1] = (double) (amplitudes[i] * Config.LOW_HZ_MLT);
                            hzAmplArr.get(i).add((double) (amplitudes[i] * Config.LOW_HZ_MLT));
                        else
                            if (       hzAmplArr.get(i).get(0) >= Config.MID_HZ_MLT_LOW_BOUND
                                    && hzAmplArr.get(i).get(0) < Config.MID_HZ_MLT_HIGH_BOUND
                            )
                                //hzAmplArr[i][1] = (double) amplitudes[i] * Config.MID_HZ_MLT;
                                hzAmplArr.get(i).add((double) amplitudes[i] * Config.MID_HZ_MLT);
                            else
                                //hzAmplArr[i][1] = (double) amplitudes[i] * Config.HIGH_HZ_MLT;
                                hzAmplArr.get(i).add((double) amplitudes[i] * Config.HIGH_HZ_MLT);


                        //min and max
                        if (minHz > hzAmplArr.get(i).get(0))
                            minHz = hzAmplArr.get(i).get(0);
                        if (maxHz < hzAmplArr.get(i).get(0))
                            maxHz = hzAmplArr.get(i).get(0);
                    }
                    //System.out.println(minHz+" "+maxHz);
                    //limiting maxHz
                    if (maxHz > Config.MAX_HZ)
                        maxHz = (double) Config.MAX_HZ;

                    //System.out.println("before tick: maxHz="+maxHz);
                    effect.tick(hzAmplArr, maxHz, minHz);

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
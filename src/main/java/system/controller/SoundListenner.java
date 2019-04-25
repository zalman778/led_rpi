package system.controller;

import system.effects.EffectsHandler;
import system.model.LedStrip;

import javax.sound.sampled.LineUnavailableException;

/**
 * Created by Hiwoo on 09.02.2018.
 */
public class SoundListenner implements SoundRunnable {

    private LedStrip ledStrip;
    private Scanner scanner;
    private Thread currentThread;
    private EffectsHandler effectsHandler;

    @Override
    public void init() {

        scanner = new Scanner();
        scanner.setLedStrip(ledStrip);
        scanner.initialize();

        currentThread = new Thread(this);
        currentThread.start();

    }

    @Override
    public void setLedStrip(LedStrip ledStrip) {
        this.ledStrip = ledStrip;
    }

    @Override
    public void setEffectsHandler(EffectsHandler effectsHandler) {
        this.effectsHandler = effectsHandler;
    }

    @Override
    public void stop() {
        //currentThread.interrupt();
        currentThread.stop();
        currentThread.destroy();
        currentThread = null;

        scanner = null;
        System.gc();
    }

    @Override
    public void run() {
        scanner.run();
    }
}

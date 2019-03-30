package system.controller;

import system.model.LedStrip;

import javax.sound.sampled.LineUnavailableException;

/**
 * Created by Hiwoo on 09.02.2018.
 */
public class SoundListenner implements SoundRunnable {

    private LedStrip ledStrip;
    private Scanner scanner;

    @Override
    public void init() {

        scanner = new Scanner();
        scanner.setLedStrip(ledStrip);
        scanner.initialize();

    }

    @Override
    public void setLedStrip(LedStrip ledStrip) {
        this.ledStrip = ledStrip;
    }

    @Override
    public void run() {
        scanner.run();
    }
}

package system.effects;

import system.model.LedStrip;

import java.util.ArrayList;
import java.util.HashMap;

/*
    Базовый класс эффектов.
    Описывает тик для кажлого временного интервала.
    предусмотрена возможность создавать продолжительные эфеекты: создаются дополнительные потоки.
 */

public abstract class Effect extends Thread{

    protected LedStrip ledStrip;

    private HashMap<String, Object> initParams;

    public Effect(LedStrip ledStrip, HashMap<String, Object> initParams) {
        this.ledStrip = ledStrip;
        this.initParams = initParams;
    }

    public LedStrip getLedStrip() {
        return ledStrip;
    }

    public void setLedStrip(LedStrip ledStrip) {
        this.ledStrip = ledStrip;
    }


    /*
        base tick method - calculates the changes on every step
     */
    public abstract void tick(ArrayList<ArrayList<Double>> pArr, Double max, Double min);
    /*
        base method - is ended, checks if effect was ended by time or other rules
     */
    public abstract boolean isEnded();

    @Override
    public void run() {
        super.run();
        while (true) {
            //tick();
            if (isEnded())
                break;
        }
    }

    protected void delay(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


}

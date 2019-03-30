package system.model;

import system.config.Config;

import java.util.ArrayList;

/**
 * Created by Hiwoo on 05.02.2018.
 */
public class LedStrip {
    private ArrayList<Led> mlist;

    public  void init() {
        mlist = new ArrayList<>();
        for (int i = 0; i < Config.LED_CNT; i++) {
            mlist.add(new Led(10, 10, 10, 0.61f));
        }
    }

    public ArrayList<Led> getMlist() {
        return mlist;
    }

    public synchronized void set(int i, Led led) {
        mlist.set(i,  led);
    }
}

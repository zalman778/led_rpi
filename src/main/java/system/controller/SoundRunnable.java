package system.controller;

import system.model.LedStrip;

/**
 * Created by Hiwoo on 09.02.2018.
 */
public interface SoundRunnable extends Runnable  {
    public void init();

    public void setLedStrip(LedStrip ledStrip);
}

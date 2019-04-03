package system.view;


import system.model.LedStrip;



/**
 * Created by Hiwoo on 08.02.2018.
 */
public interface SignalRunnable extends Runnable {



    public void init();

    public void setLedStrip(LedStrip ledStrip);

    public void stop();

    public void sendSignal();
}

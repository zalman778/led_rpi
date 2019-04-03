package system.view;


import com.github.mbelling.ws281x.LedStripType;
import com.github.mbelling.ws281x.Ws281xLedStrip;
import system.config.Config;
import system.config.Fader;
import system.model.LedStrip;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Created by Hiwoo on 08.02.2018.
 */

public class SignalSender implements SignalRunnable{

    private Socket pingSocket = null;
    private PrintWriter out = null;
    private BufferedReader in = null;

    private LedStrip ledStrip = null;

    private Ws281xLedStrip ws281xLedStrip;

    private Thread currentThread;

    public SignalSender(LedStrip ledStrip) {
        this.ledStrip = ledStrip;

    }



    @Override
    public void run() {
        while (true) {
            if (Config.SEND.equals("1") && !Thread.interrupted()) {
                //fade step

                for (int i = 0; i < ledStrip.getMlist().size(); i++) {

                    ws281xLedStrip.setPixel(i
                            , ledStrip.getMlist().get(i).getRed()
                            , ledStrip.getMlist().get(i).getGreen()
                            , ledStrip.getMlist().get(i).getBlue()
                    );
                }
                ws281xLedStrip.render();

            } else {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void sendSignal() {
        for (int i = 0; i < ledStrip.getMlist().size(); i++) {

            ws281xLedStrip.setPixel(i
                    ,ledStrip.getMlist().get(i).getRed()
                    ,ledStrip.getMlist().get(i).getGreen()
                    ,ledStrip.getMlist().get(i).getBlue()
            );
        }
        ws281xLedStrip.setBrightness(Math.round(Config.LED_BRIGHT * 255));
        ws281xLedStrip.render();
    }

    @Override
    public void init() {
        ws281xLedStrip = new Ws281xLedStrip(
                Config.LED_CNT,       // leds
                10,          // Using pin 10 to do SPI, which should allow non-sudo access
                800000,  // freq hz
                10,            // dma
                255,      // brightness
                0,      // pwm channel
                false,        // invert
                LedStripType.WS2811_STRIP_RGB,    // Strip type
                false    // clear on exit
        );

        currentThread = new Thread(this);
        currentThread.start();
    }

    public void setLedStrip(LedStrip ledStrip) {
        this.ledStrip = ledStrip;
    }

    @Override
    public void stop() {
        currentThread.stop();
        currentThread.destroy();
        currentThread = null;

        ws281xLedStrip = null;
        System.gc();
    }
}

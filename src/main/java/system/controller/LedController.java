package system.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import system.config.Config;
import system.model.Led;
import system.model.LedStrip;
import system.view.SignalRunnable;

import java.awt.*;
import java.lang.reflect.Field;

/**
 * Created by Hiwoo on 08.02.2018.
 */
@Controller
@RequestMapping("/")
public class LedController {

    @Autowired
    LedStrip ledStrip;

    @Autowired
    SignalRunnable signalSender;

    @Autowired
    SoundRunnable soundListenner;

    @GetMapping("/set_blue")
    @ResponseBody
    public void set_blue() {

    }

    @GetMapping("/set_red")
    @ResponseBody
    public void set_red() {

    }

    @GetMapping("/set_hsb/{paramValue}")
    @ResponseBody
    public String set_color(@PathVariable String paramValue) {
        try {
            float saturation = 100;
            float brightness = 90;
            int red= 1;
            int green = 1;
            int blue = 1;

            Float value = Float.parseFloat(paramValue);

            int rgb = Color.HSBtoRGB(value/360, saturation/100, brightness/100);
            red = (rgb>>16)&0xFF;
            green = (rgb>>8)&0xFF;
            blue = rgb&0xFF;

            for (int i = 0; i < Config.LED_CNT - 1; i++) {
                ledStrip.set(i, new Led(red, green, blue, Config.LED_BRIGHT));
            }

            System.out.println(value+"; got color= "+red+": "+green+": "+blue);
            signalSender.sendSignal();
        } catch (Exception e) {
            return "param should be 0-360: "+e.getLocalizedMessage();
        }
        soundListenner.init();
        return "OK";
    }


    @GetMapping("/reset")
    @ResponseBody
    public String reset() {
        Config.reset();
        return String.valueOf(Config.MIN_HSB_VAL)+" -> "+String.valueOf(Config.MAX_HSB_VAL);
    }

    @GetMapping("/set/{paramName}/{paramValue}")
    @ResponseBody
    public String setParamByName(@PathVariable String paramName, @PathVariable String paramValue) {
        try {
            Field field = Config.class.getDeclaredField(paramName);
            field.setAccessible(true);
            switch(String.valueOf(field.getType())) {
                case "class java.lang.Float":
                    Float value = Float.parseFloat(paramValue);
                    field.set(null, value);
                    break;
                case "class java.lang.Integer":
                    Integer intVal = Integer.parseInt(paramValue);
                    field.set(null, intVal);
                    break;
                case "class java.lang.String":
                    field.set(null, paramValue);
                    break;
            }
            soundListenner.stop();
            signalSender.stop();
            Config.reset();
            soundListenner.init();
            signalSender.init();

        } catch (NoSuchFieldException e) {
            return "Field not found";
        } catch (IllegalAccessException e) {
            return "No such ";
        }
        return "OK";
    }

    @GetMapping("/info")
    @ResponseBody
    public String full_info() {
        StringBuilder sb = new StringBuilder();
        //sb.append(Config.class.getDeclaredFields().length);
        String nameVar = null;
        try {
            for (Field field : Config.class.getDeclaredFields()) {
                sb.append(field.getName()+" = "+field.get(Config.class)+ "<br>");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "No such ";
        }
        return sb.toString();
    }

    @GetMapping("/stop")
    @ResponseBody
    public String stopAll() {
        signalSender.stop();
        soundListenner.stop();
        return "OK";
    }

}

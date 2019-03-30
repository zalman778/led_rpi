package system;

import system.config.Config;

import java.awt.*;

public class Test {
    public static void main(String[] args) {

        String val = "200";
        Integer value = Integer.parseInt(val);

        float saturation = 1.0f;
        float brightness = 1.0f;

        int rgb = Color.HSBtoRGB(value, saturation, brightness);
        int red = (rgb >> 16) & 0xFF;
        int green = (rgb >> 8) & 0xFF;
        int blue = rgb & 0xFF;

        System.out.println(val+"; got color= "+red+": "+green+": "+blue);
    }
}

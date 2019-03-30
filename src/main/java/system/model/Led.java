package system.model;

/**
 * Created by Hiwoo on 05.02.2018.
 */
public class Led {
    private int red;
    private int green;
    private int blue;

    private float brightness;

    public Led(int red, int green, int blue, float brightness) {
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.brightness = brightness;
    }

    public int getRed() {
        return red;
    }

    public int getGreen() {
        return green;
    }

    public int getBlue() {
        return blue;
    }

    public float getBrightness() {
        return brightness;
    }
}

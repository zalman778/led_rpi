package system.config;

import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;

import java.io.*;
import java.util.LinkedList;
import java.util.Properties;

/**
 * Основной конфигурацинный класс.
 */
public class Config {

    public static Integer INT_CNT = 60;
    public static Integer LED_CNT = 780;

    //максимальная частота
    public static Integer MAX_HZ = 15000;

    //процент громкости, чтобы отобразить бар звука
    public static Float MIN_SOUND_AMPL_FILTER_PRC = 3.2f;

    //множители аплтуд для частот в диапазонах
    public static Float LOW_HZ_MLT = 0.02f;
    public static Float MID_HZ_MLT_LOW_BOUND = 1500f;
    public static Float MID_HZ_MLT = 1.0f;
    public static Float MID_HZ_MLT_HIGH_BOUND = 6000f;
    public static Float HIGH_HZ_MLT = 1.0f;

    //сколько последних хранить для сравнения
    public static Integer MAX_LAST_AMPL_LIST_SIZE = 500;

    public static Integer MIN_HSB_VAL = 220;
    public static Integer MAX_HSB_VAL = 360;
    public static Float LED_BRIGHT = 0.71f;
    public static Integer ASYM_MODE = 1;
    public static String SEND = "1";

    public static Integer EFFECTS_THREAS_COUNT = 20;



    public static double[] maxAmplArrAllTime = new double[INT_CNT];
    //список последних аплитуд, для поиск затишья
    public static LinkedList<Double> lastXAmplList = new LinkedList<>();

    public static synchronized LinkedList<Double> getLastXAmplList(){
        return lastXAmplList;
    }
    public static Integer currAmplListSize = 0;
    public static Double currentMaxAmplVal = -Double.MIN_VALUE; //текущее значение макс громкости, для подсчета процена (выше)





    public static void reset() {
        currentMaxAmplVal = 0d;
        maxAmplArrAllTime = new double[INT_CNT];
        lastXAmplList = new LinkedList<>();
        currentMaxAmplVal = -Double.MIN_VALUE;
    }


    public static void readAll(ApplicationContext ac) {
        Properties prop = new Properties();

        InputStream inp = null;

        try {
            Resource resource = ac.getResource("classpath:Config.properties");
            inp = resource.getInputStream();

            //inp = new FileInputStream("Config.properties");
            prop.load(inp);
            INT_CNT = Integer.parseInt(prop.getProperty("INT_CNT"));
            LED_CNT = Integer.parseInt(prop.getProperty("LED_CNT"));
            MAX_HZ = Integer.parseInt(prop.getProperty("MAX_HZ"));
            //MAX_HZ_LIMIT = Integer.parseInt(prop.getProperty("MAX_HZ_LIMIT"));
            MIN_SOUND_AMPL_FILTER_PRC = Float.parseFloat(prop.getProperty("MIN_SOUAND_AMPL_FILTER_PRC"));
            LOW_HZ_MLT = Float.parseFloat(prop.getProperty("LOW_HZ_MLT"));
            //LOW_HZ_MLT_HIGH_BOUND = Float.parseFloat(prop.getProperty("LOW_HZ_MLT_HIGH_BOUND"));
            MID_HZ_MLT = Float.parseFloat(prop.getProperty("MID_HZ_MLT"));
            MID_HZ_MLT_LOW_BOUND = Float.parseFloat(prop.getProperty("MID_HZ_MLT_LOW_BOUND"));
            MID_HZ_MLT_HIGH_BOUND = Float.parseFloat(prop.getProperty("MID_HZ_MLT_HIGH_BOUND"));
            HIGH_HZ_MLT = Float.parseFloat(prop.getProperty("HIGH_HZ_MLT"));
            //HIGH_HZ_MLT_LOW_BOUND = Float.parseFloat(prop.getProperty("HIGH_HZ_MLT_LOW_BOUND"));
            MAX_LAST_AMPL_LIST_SIZE =  Integer.parseInt(prop.getProperty("MAX_LAST_AMPL_LIST_SIZE"));
            MIN_HSB_VAL =  Integer.parseInt(prop.getProperty("MIN_HSB_VAL"));
            MAX_HSB_VAL =  Integer.parseInt(prop.getProperty("MAX_HSB_VAL"));
            LED_BRIGHT = Float.parseFloat(prop.getProperty("LED_BRIGHT"));
            ASYM_MODE = Integer.parseInt(prop.getProperty("ASYM_MODE"));
            SEND = prop.getProperty("SEND");
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveAll() {
        Properties prop = new Properties();
        OutputStream output = null;
        try {

            output = new FileOutputStream("Config.properties");

            // set the properties value
            prop.setProperty("INT_CNT", String.valueOf(INT_CNT));
            prop.setProperty("LED_CNT", String.valueOf(LED_CNT));
            prop.setProperty("MAX_HZ", String.valueOf(MAX_HZ));
            //prop.setProperty("MAX_HZ_LIMIT", String.valueOf(MAX_HZ_LIMIT));
            prop.setProperty("MIN_SOUAND_AMPL_FILTER_PRC", String.valueOf(MIN_SOUND_AMPL_FILTER_PRC));
            prop.setProperty("LOW_HZ_MLT", String.valueOf(LOW_HZ_MLT));
            //prop.setProperty("LOW_HZ_MLT_HIGH_BOUND", String.valueOf(LOW_HZ_MLT_HIGH_BOUND));
            prop.setProperty("MID_HZ_MLT", String.valueOf(MID_HZ_MLT));
            prop.setProperty("MID_HZ_MLT_LOW_BOUND", String.valueOf(MID_HZ_MLT_LOW_BOUND));
            prop.setProperty("MID_HZ_MLT_HIGH_BOUND", String.valueOf(MID_HZ_MLT_HIGH_BOUND));
            prop.setProperty("HIGH_HZ_MLT", String.valueOf(HIGH_HZ_MLT));
            //prop.setProperty("HIGH_HZ_MLT_LOW_BOUND", String.valueOf(HIGH_HZ_MLT_LOW_BOUND));
            prop.setProperty("MAX_LAST_AMPL_LIST_SIZE", String.valueOf(MAX_LAST_AMPL_LIST_SIZE));
            prop.setProperty("MIN_HSB_VAL", String.valueOf(MIN_HSB_VAL));
            prop.setProperty("MAX_HSB_VAL", String.valueOf(MAX_HSB_VAL));
            prop.setProperty("LED_BRIGHT", String.valueOf(LED_BRIGHT));
            prop.setProperty("ASYM_MODE", String.valueOf(ASYM_MODE));
            prop.setProperty("SEND", Config.SEND);

            // save properties to project root folder
            prop.store(output, null);

        } catch (IOException io) {
            io.printStackTrace();
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }

}

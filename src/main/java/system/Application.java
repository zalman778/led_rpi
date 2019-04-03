package system;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.*;
import system.config.Config;
import system.controller.SoundListenner;
import system.controller.SoundRunnable;
import system.model.LedStrip;
import system.view.SignalRunnable;
import system.view.SignalSender;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.sound.sampled.LineUnavailableException;
import java.util.Arrays;

/**
 * Created by sanek on 05.02.2018.
 */
@Configuration
@EnableAutoConfiguration
@ComponentScan(basePackages = {"system"})
@PropertySource("classpath:application.properties")
public class Application extends SpringBootServletInitializer implements ServletContextListener {
    final static Logger logger = Logger.getLogger(Application.class);

    public static void main(String[] args) throws LineUnavailableException {
        ApplicationContext ctx = SpringApplication.run(Application.class, args);
    }

    @Autowired
    private ApplicationContext context;

    @Autowired
    LedStrip ledStrip;

    @Autowired
    SignalRunnable signalRunnable;

    @Autowired
    SoundRunnable soundRunnable;


    @Bean
    @Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
    public LedStrip ledStrip() {
        LedStrip ledStripNew = new LedStrip();
        ledStripNew.init();
        return ledStripNew;
    }

    @Bean
    @Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
    public SoundListenner soundListenner() {
        SoundListenner soundListenner = new SoundListenner();
        soundListenner.setLedStrip(ledStrip);
        soundListenner.init();
        return soundListenner;
    }


    @Bean
    @Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
    public SignalRunnable signalSender() {
        SignalRunnable signalSender = new SignalSender(ledStrip);
        signalSender.init();
        return signalSender;
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        Config.readAll(context);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        signalRunnable.stop();
        soundRunnable.stop();
    }



}

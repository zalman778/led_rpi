package system;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
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

        System.out.println("Let's inspect the beans provided by Spring Boot:");

        String[] beanNames = ctx.getBeanDefinitionNames();
        Arrays.sort(beanNames);
        for (String beanName : beanNames) {
            System.out.println(beanName);
        }
    }

    @Autowired
    LedStrip ledStrip;

    @Bean
    @Scope("singleton")
    public LedStrip ledStrip() {
        LedStrip ledStripNew = new LedStrip();
        ledStripNew.init();
        return ledStripNew;
    }

    @Bean
    @Scope("singleton")
    public SoundListenner soundListenner() {
        SoundListenner soundListenner = new SoundListenner();
        soundListenner.setLedStrip(ledStrip);
        soundListenner.init();
        Thread tSound = new Thread(soundListenner);
        tSound.start();
        return soundListenner;
    }


    @Bean
    @Scope("singleton")
    public SignalSender signalSender() {
        SignalSender signalSender = new SignalSender(ledStrip);
        signalSender.init();
        Thread tSignal = new Thread(signalSender);
        tSignal.start();
        return signalSender;
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ApplicationContext ac = new AnnotationConfigApplicationContext(Application.class);
        sce.getServletContext().setAttribute("applicationContext", ac);
        ServletContext sc = sce.getServletContext();
        logger.info("app started:");
        Config.readAll(ac);





    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
    }


}

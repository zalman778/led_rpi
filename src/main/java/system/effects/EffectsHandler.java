package system.effects;

import system.config.Config;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EffectsHandler {

    private ExecutorService threadPool;
    private volatile boolean running = false;

    public EffectsHandler() {
        this.threadPool = Executors.newFixedThreadPool(Config.EFFECTS_THREAS_COUNT);
    }

    public void addEffect(Effect effect) {
        threadPool.submit(effect);
    }

}

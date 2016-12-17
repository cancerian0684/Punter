package org.shunya;

import org.shunya.punter.gui.Main;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Arrays;

@EnableAsync
@SpringBootApplication
public class PunterApp implements CommandLineRunner{
    private static final Logger logger = LoggerFactory.getLogger(PunterApp.class);

    @Autowired
    private Main main;

    public static void main(String[] args) {
        ConfigurableApplicationContext ctx = new SpringApplicationBuilder(PunterApp.class).headless(false).run(args);
        String[] beanNames = ctx.getBeanDefinitionNames();
        Arrays.sort(beanNames);
        for (String beanName : beanNames) {
            logger.debug(beanName);
        }
        logger.info("PunterApp Started Successfully.");
    }

    @Override
    public void run(String... args) throws Exception {
        main.init();
    }

    @Bean
    public AsyncTaskExecutor getAsync(){
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(3);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(10);
        executor.setThreadNamePrefix("AsyncExec-");
        executor.initialize();
        return executor;
    }
}

package com.didekin;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.web.embedded.jetty.JettyServletWebServerFactory;
import org.springframework.context.annotation.Bean;

import static com.didekin.ThreadPoolConstants.IDLE_TIMEOUT_FRONT;
import static org.springframework.boot.Banner.Mode.OFF;

/**
 * User: pedro
 * Date: 10/03/15
 * Time: 17:18
 */
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class Application {

    private static final Logger logger = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args)
    {
        logger.debug("Before calling run()");
        SpringApplication app = new SpringApplication(Application.class);
        app.setBannerMode(OFF);
        app.run(args);
    }

    @Bean
    public JettyServletWebServerFactory jettyEmbeddedServletContainerFactory()
    {
        final JettyServletWebServerFactory factory = new JettyServletWebServerFactory();
        factory.addServerCustomizers((Server server) -> {
            final QueuedThreadPool threadPool = server.getBean(QueuedThreadPool.class);
            threadPool.setIdleTimeout(IDLE_TIMEOUT_FRONT);
            logger.debug(String.format("Max threads = %d min threads = %d idleTimeout = %d %n",
                    threadPool.getMaxThreads(), threadPool.getMinThreads(), threadPool.getIdleTimeout()));
        });
        return factory;
    }
}
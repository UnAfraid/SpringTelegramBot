package com.github.unafraid.spring;

import com.github.unafraid.spring.bot.handlers.general.CommandHandler;
import com.github.unafraid.spring.bot.handlers.general.ICommandHandler;
import com.github.unafraid.spring.config.TelegramBotConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.Map;


/**
 * @author UnAfraid
 */
@SpringBootApplication
@EnableConfigurationProperties({TelegramBotConfig.class})
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = {"com.github.unafraid.spring.repositories"})
public class Application extends SpringBootServletInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(Application.class);
    }

    public static void main(String[] args) {
        final ConfigurableApplicationContext context = SpringApplication.run(Application.class);

        // Handlers setup
        final Map<String, ICommandHandler> handlers = context.getBeansOfType(ICommandHandler.class);
        handlers.values().forEach(handler -> {
            CommandHandler.getInstance().addHandler(handler);
            LOGGER.info("Loaded handler: {}", handler.getClass().getSimpleName());
        });
        LOGGER.info("Loaded {} handlers", handlers.size());
    }
}

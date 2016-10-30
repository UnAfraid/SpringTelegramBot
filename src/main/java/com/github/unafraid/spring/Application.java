package com.github.unafraid.spring;

import com.github.unafraid.spring.bot.handlers.CommandHandler;
import com.github.unafraid.spring.bot.handlers.impl.ICommandHandler;
import com.github.unafraid.spring.config.TelegramBotConfig;
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
 * Created by UnAfraid on 21.10.2016 г..
 */
@SpringBootApplication
@EnableConfigurationProperties({TelegramBotConfig.class})
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = {"com.github.unafraid.spring.bot.db.repositories"})
public class Application extends SpringBootServletInitializer {
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(Application.class);
    }

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(Application.class);
        final Map<String, ICommandHandler> handlers = context.getBeansOfType(ICommandHandler.class);
        handlers.values().forEach(CommandHandler.getInstance()::addHandler);
        handlers.values().forEach(handler -> LoggerFactory.getLogger(Application.class).info("Loaded handler: {}", handler.getClass().getSimpleName()));
        LoggerFactory.getLogger(Application.class).info("Loaded {} handlers", handlers.size());
    }
}

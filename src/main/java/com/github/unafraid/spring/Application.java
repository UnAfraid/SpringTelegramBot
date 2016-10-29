package com.github.unafraid.spring;

import com.github.unafraid.spring.config.TelegramBotConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;


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
        SpringApplication.run(Application.class);
    }
}

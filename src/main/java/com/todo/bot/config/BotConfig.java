package com.todo.bot.config;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Data
@Configuration
@NoArgsConstructor(force = true)
@PropertySource("application.properties")
public class BotConfig {

    @Value("${bot.token}")
    private final String token;

    @Value("${bot.username}")
    private final String username;

}

package QuentinXHugo.demo.royaume.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.time.Clock;
import java.util.concurrent.Executor;

@Configuration
public class AsyncConfiguration {

    @Bean
    public Executor royaumeQuestExecutor() {
        var executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setThreadNamePrefix("royaume-quest-");
        executor.initialize();
        return executor;
    }

    @Bean
    public Clock systemClock() {
        return Clock.systemUTC();
    }
}

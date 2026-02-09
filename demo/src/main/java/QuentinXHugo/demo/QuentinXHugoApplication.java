package QuentinXHugo.demo;

import QuentinXHugo.demo.royaume.config.RoyaumeApiProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableConfigurationProperties(RoyaumeApiProperties.class)
@EnableAsync
@EnableScheduling
public class QuentinXHugoApplication {

	public static void main(String[] args) {
		SpringApplication.run(QuentinXHugoApplication.class, args);
	}

}

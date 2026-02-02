package QuentinXHugo.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import QuentinXHugo.demo.config.ProfessorProperties;

@SpringBootApplication
@EnableScheduling
@EnableAsync
@EnableConfigurationProperties(ProfessorProperties.class)
public class QuentinXHugoApplication {

	public static void main(String[] args) {
		SpringApplication.run(QuentinXHugoApplication.class, args);
	}

}

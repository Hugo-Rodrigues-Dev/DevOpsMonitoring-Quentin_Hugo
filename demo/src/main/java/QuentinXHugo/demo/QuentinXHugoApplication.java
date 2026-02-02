package QuentinXHugo.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

import QuentinXHugo.demo.config.ProfessorProperties;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(ProfessorProperties.class)
public class QuentinXHugoApplication {

	public static void main(String[] args) {
		SpringApplication.run(QuentinXHugoApplication.class, args);
	}

}

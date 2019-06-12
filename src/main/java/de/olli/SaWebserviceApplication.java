package de.olli;

import de.olli.repository.PriceMongoDBReactiveRepo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableReactiveMongoRepositories(basePackageClasses = PriceMongoDBReactiveRepo.class)
@SpringBootApplication
@EnableScheduling
@EnableAsync
public class SaWebserviceApplication {

	public static void main(String[] args) {
		SpringApplication.run(SaWebserviceApplication.class, args);
	}
}

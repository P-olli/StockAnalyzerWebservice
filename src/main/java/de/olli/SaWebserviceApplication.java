package de.olli;

import de.olli.repository.StocksMongoDBReactiveRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;

@EnableReactiveMongoRepositories(basePackageClasses = StocksMongoDBReactiveRepository.class)
@SpringBootApplication
public class SaWebserviceApplication {

	public static void main(String[] args) {
		SpringApplication.run(SaWebserviceApplication.class, args);
	}
}

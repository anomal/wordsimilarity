package shortlister.service.word.similarity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import shortlister.service.word.similarity.rest.controller.WordSimilarityRestController;

@SpringBootApplication
public class WordSimilarityApplication {

	private final static Logger log = LoggerFactory.getLogger(WordSimilarityApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(WordSimilarityApplication.class, args);
		log.info("DEV_TOKEN: {}", WordSimilarityRestController.DEV_TOKEN);
	}

}

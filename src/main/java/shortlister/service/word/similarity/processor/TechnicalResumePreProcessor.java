package shortlister.service.word.similarity.processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class TechnicalResumePreProcessor extends ResumePreProcessor {

    private static Logger log = LoggerFactory.getLogger(TechnicalResumePreProcessor.class);

    @Override
    protected String sanitize(String resume) {
        return resume
                .replace("pl/sql", "plsql")
                .replaceAll("[^a-z0-9+#]", " ");
    }
}

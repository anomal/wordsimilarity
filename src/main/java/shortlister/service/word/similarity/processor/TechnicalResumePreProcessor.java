package shortlister.service.word.similarity.processor;

import org.deeplearning4j.text.sentenceiterator.SentencePreProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class TechnicalResumePreProcessor implements SentencePreProcessor {

    private static Logger log = LoggerFactory.getLogger(TechnicalResumePreProcessor.class);

    @Override
    public String preProcess(String resume) {
        return resume.toLowerCase()
                .replace("pl/sql", "plsql")
                .replaceAll("[^a-z0-9+#]", " ");
    }
}

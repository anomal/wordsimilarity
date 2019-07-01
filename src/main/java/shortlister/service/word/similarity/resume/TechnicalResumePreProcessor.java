package shortlister.service.word.similarity.resume;

import org.deeplearning4j.text.sentenceiterator.SentencePreProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TechnicalResumePreProcessor implements SentencePreProcessor {

    private final static Logger log = LoggerFactory.getLogger(TechnicalResumePreProcessor.class);

    private List<String> ignoredWords;

    public TechnicalResumePreProcessor(List<String> wordsToIgnore) {
        ignoredWords = wordsToIgnore;
    }

    @Override
    public String preProcess(String resume) {
        if (ignoredWords != null) {
            for (String ignoredWord : ignoredWords) {
                resume = resume.replace(ignoredWord, "");
            }
        }
        return resume.toLowerCase()
                .replace("pl/sql", "plsql")
                .replaceAll("[^a-z0-9+#]", " ");
    }
}

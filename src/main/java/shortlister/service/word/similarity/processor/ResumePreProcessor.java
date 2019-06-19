package shortlister.service.word.similarity.processor;

import org.deeplearning4j.text.sentenceiterator.SentencePreProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class ResumePreProcessor implements SentencePreProcessor {
    private static Logger log = LoggerFactory.getLogger(ResumePreProcessor.class);

    private volatile List<String> uniqueWordsPerResume = new ArrayList<>();
    protected Map<String,Long> wordCounts;

    @Override
    public String preProcess(String resume) {
        //log.info("sentence: {}", resume);
        log.info("sentence: {}", resume.substring(0,10));
        String preprocessed = sanitize(resume.toLowerCase());
        String[] tokenized = preprocessed.split(" +");
        Set<String> uniqueWords = new HashSet<>();
        for (String token : tokenized) {
            uniqueWords.add(token);
        }

        for (String uniqueWord : uniqueWords) {
            if (uniqueWord.equals("hadoop") || uniqueWord.equals("hackathon")) {
                log.info("adding {}", uniqueWord);
            }
            uniqueWordsPerResume.add(uniqueWord);
        }
        log.debug("preprocessed: {}", preprocessed);
        return preprocessed;
    }

    protected abstract String sanitize(String resume);

    public synchronized Map<String,Long> getWordCounts() {
        /*
        wordCounts = new HashMap<>();

        for (String uniqueWordPerResume : uniqueWordsPerResume) {
            //log.info("uniqueWordPerResume: {}", uniqueWordPerResume);
            Long count = wordCounts.get(uniqueWordPerResume);
            if (count == null) {
                wordCounts.put(uniqueWordPerResume, 1L);
            } else {
                wordCounts.put(uniqueWordPerResume, count + 1);
            }
        }

       return wordCounts;

        */

        return uniqueWordsPerResume.stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
    }
}

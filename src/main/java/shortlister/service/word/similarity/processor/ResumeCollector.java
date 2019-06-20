package shortlister.service.word.similarity.processor;

import io.swagger.client.model.Resume;
import org.deeplearning4j.text.sentenceiterator.SentencePreProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.MathContext;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

public class ResumeCollector {

    private static Logger log = LoggerFactory.getLogger(ResumeCollector.class);

    private final List<String> resumeTexts;
    private final Map<String,Long> wordUniqueWordFrequencies = new HashMap<>();
    private long maxUniqueWordFrequency = 0L;

    public ResumeCollector(List<Resume> resumes, SentencePreProcessor resumePreProcessor) {
        resumeTexts = resumes.stream()
                .map(resume -> {
                    String resumeText = resume.getText();
                    String[] tokenized = resumePreProcessor.preProcess(resumeText)
                            .split(" +");

                    Set<String> uniqueWords = new HashSet<>();
                    for (String token : tokenized) {
                        uniqueWords.add(token);
                    }
                    for (String uniqueWord : uniqueWords){
                        Long count = wordUniqueWordFrequencies.get(uniqueWord);
                        if (count == null) {
                            count = 1L;
                        } else {
                            count++;
                        }
                        wordUniqueWordFrequencies.put(uniqueWord, count);
                        if (count > maxUniqueWordFrequency) {
                            maxUniqueWordFrequency = count;
                        }
                    }

                    return resumeText;
                })
                .collect(Collectors.toList());
    }

    public List<String> getResumeTexts() {
        return resumeTexts;
    }

    public Map<String,Long> getWordUniqueWordFrequencies() {
        return wordUniqueWordFrequencies;
    }

    public long getMaxUniqueWordFrequency() {
        return maxUniqueWordFrequency;
    }
}

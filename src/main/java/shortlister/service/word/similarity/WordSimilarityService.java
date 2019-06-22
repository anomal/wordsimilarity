package shortlister.service.word.similarity;

import io.swagger.client.model.Resume;
import io.swagger.client.model.Word;
import io.swagger.client.model.WordSimilarityResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import shortlister.service.word.similarity.neuralnet.ResumeWordModel;
import shortlister.service.word.similarity.resume.*;
import shortlister.service.word.similarity.visualization.ResumeWordCloud;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.*;
import java.util.List;

//@Slf4j
@Service
public class WordSimilarityService {

    private final static Logger log = LoggerFactory.getLogger(WordSimilarityService.class);

    private final static MathContext MATH_CONTEXT = new MathContext(3, RoundingMode.HALF_UP);

    @Autowired
    private TechnicalResumePreProcessor technicalResumePreProcessor;

    @Autowired
    private ResumeWordComparator resumeWordComparator;

    public WordSimilarityResponse analyze (List<Resume> resumes, BigDecimal wordAttraction) throws IOException {

        ResumeDataRepository resumeDataRepository = new ResumeDataRepository(resumes, technicalResumePreProcessor);

        ResumeWordModel resumeWordModel = new ResumeWordModel(resumeDataRepository);

        ResumeWordCloud resumeWordCloud = new ResumeWordCloud(resumeWordModel, wordAttraction);

        Map<String,Long> wordCounts = resumeDataRepository.getWordUniqueWordFrequencies();

        List<Word> words = new ArrayList<>();
        for (int i = 0; i < resumeWordCloud.getSize(); i++) {
            String wordName = resumeWordCloud.getWordName(i);
            Long freq = wordCounts.get(wordName);
            if (freq != null && freq > 1) {
                Word word = new Word();
                word.setName(wordName);
                word.setX(resumeWordCloud.getX(i));
                word.setY(resumeWordCloud.getY(i));
                word.setFrequency(freq);
                BigDecimal size = new BigDecimal(freq).divide(
                        new BigDecimal(resumeDataRepository.getMaxUniqueWordFrequency()),
                        MATH_CONTEXT);
                word.setSize(size);
                log.info("{} {} {} {} {}", word.getName(), word.getFrequency(),
                        word.getSize(),
                        word.getX(), word.getY());
                words.add(word);
            }
        }

        words.sort(resumeWordComparator);

        WordSimilarityResponse response = new WordSimilarityResponse();
        response.setWords(words);
        log.info("words length is {}", words.size());
        response.setApplicants(resumeDataRepository.getApplicantNicknames());

        return response;
    }
}

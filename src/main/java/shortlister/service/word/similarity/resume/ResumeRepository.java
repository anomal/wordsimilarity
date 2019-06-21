package shortlister.service.word.similarity.resume;

import io.swagger.client.model.Applicant;
import io.swagger.client.model.Resume;
import org.deeplearning4j.text.sentenceiterator.SentencePreProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class ResumeRepository {

    private final static Logger log = LoggerFactory.getLogger(ResumeRepository.class);

    private final List<String> resumeTexts;
    private final Map<String,Long> wordUniqueWordFrequencies = new HashMap<>();
    private long maxUniqueWordFrequency = 0L;
    private final List<Applicant> applicantNicknames;

    public ResumeRepository(List<Resume> resumes, SentencePreProcessor resumePreProcessor) {
        Map<String,String> uniqueWordToIdMap = new HashMap<>();

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
                            uniqueWordToIdMap.put(uniqueWord, resume.getId());
                        } else {
                            count++;
                            uniqueWordToIdMap.remove(uniqueWord);
                        }
                        wordUniqueWordFrequencies.put(uniqueWord, count);
                        if (count > maxUniqueWordFrequency) {
                            maxUniqueWordFrequency = count;
                        }
                    }

                    return resumeText;
                })
                .collect(Collectors.toList());

        applicantNicknames = createApplicantNickames(resumes, uniqueWordToIdMap);
    }

    protected static List<Applicant> createApplicantNickames(List<Resume> resumes,
                                                      Map<String,String> uniqueWordToIdMap) {
        final List<Applicant> applicants = new ArrayList<>();
        for (Resume resume : resumes) {
            String nickname = null;
            Set<String> keys = uniqueWordToIdMap.keySet();
            for (String uniqueWord : keys){
                if (resume.getId().equals(uniqueWordToIdMap.get(uniqueWord))) {
                    if (nickname == null || uniqueWord.length() > nickname.length()) {
                        nickname = uniqueWord;
                    }
                }
            }
            if (nickname != null) {
                nickname = nickname.substring(0,1).toUpperCase() + nickname.substring(1);
            }
            Applicant applicant = new Applicant();
            applicant.setId(resume.getId());
            applicant.setNickname(nickname);
            applicants.add(applicant);
        }
        return applicants;
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

    public List<Applicant> getApplicantNicknames() {
        return applicantNicknames;
    }
}

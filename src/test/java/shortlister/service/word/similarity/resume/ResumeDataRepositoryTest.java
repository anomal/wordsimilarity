package shortlister.service.word.similarity.resume;

import io.swagger.client.model.Applicant;
import io.swagger.client.model.Resume;
import org.junit.Test;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ResumeDataRepositoryTest {

    @Test
    public void testCreateApplicantNicknames() {
        List<Resume> resumes = Arrays.stream(new String[]{"0", "1", "2"})
                .map( id -> {
                    Resume resume = new Resume();
                    resume.setId(id);
                    return resume;
                }).collect(Collectors.toList());
        Map<String,String> wordToIdMap = new HashMap<>();
        wordToIdMap.put("x", "0");
        wordToIdMap.put("xyz", "1");
        List<Applicant> applicantNicknames = ResumeDataRepository.createApplicantNickames(resumes, wordToIdMap);
        assertTrue(applicantNicknames.size() == resumes.size());
        for (Applicant applicant : applicantNicknames) {
            if (applicant.getId().equals("0")) {
                assertEquals("X", applicant.getNickname());
            } else if (applicant.getId().equals("1")) {
                assertEquals("Xyz", applicant.getNickname());
            } else if (applicant.getId().equals("2")) {
                assertNull(applicant.getNickname());
            }
        }
    }
}

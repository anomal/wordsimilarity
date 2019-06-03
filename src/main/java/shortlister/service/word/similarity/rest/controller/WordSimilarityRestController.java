package shortlister.service.word.similarity.rest.controller;

import io.swagger.client.model.WordSimilarityRequest;
import io.swagger.client.model.WordSimilarityResponse;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WordSimilarityRestController {

    @RequestMapping("/v1/resumes")
    public WordSimilarityResponse postResumes(@RequestBody WordSimilarityRequest request) {
        WordSimilarityResponse response = new WordSimilarityResponse();
        return response;
    }

}

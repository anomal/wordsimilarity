package shortlister.service.word.similarity.rest.controller;

import io.swagger.client.model.WordSimilarityRequest;
import io.swagger.client.model.WordSimilarityResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import shortlister.service.word.similarity.WordSimilarityService;

import java.io.IOException;

@RestController
public class WordSimilarityRestController {

    private static Logger log = LoggerFactory.getLogger(WordSimilarityRestController.class);

    @Autowired
    private WordSimilarityService service;

    @RequestMapping("/v1/resumes")
    public WordSimilarityResponse postResumes(@RequestBody WordSimilarityRequest request) {
        WordSimilarityResponse response = null;
        try {
            response = service.analyze(request.getResumes(), request.getWordAttraction());
            return  response;
        } catch (RuntimeException e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

}

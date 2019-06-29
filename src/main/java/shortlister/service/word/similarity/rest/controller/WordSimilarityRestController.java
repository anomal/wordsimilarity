package shortlister.service.word.similarity.rest.controller;

import io.swagger.client.model.WordSimilarityRequest;
import io.swagger.client.model.WordSimilarityResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import shortlister.service.word.similarity.WordSimilarityService;
import shortlister.service.word.similarity.exception.UnauthorizedException;

import java.io.IOException;
import java.util.UUID;

@RestController
public class WordSimilarityRestController {

    public final static String DEV_TOKEN = UUID.randomUUID().toString().replace("-","");
    private final static int BEARER_LENGTH = "Bearer ".length();
    private final static Logger log = LoggerFactory.getLogger(WordSimilarityRestController.class);

    @Autowired
    private WordSimilarityService service;

    @RequestMapping("/v1/resumes")
    public WordSimilarityResponse postResumes(@RequestHeader("authorization") String authorization,
                                              @RequestBody WordSimilarityRequest request) throws UnauthorizedException {
        WordSimilarityResponse response = null;
        try {
            if (authorization.length() < BEARER_LENGTH
                    || !authorization.substring(BEARER_LENGTH ).trim().equals(DEV_TOKEN)) {
                throw new UnauthorizedException("request token is " + authorization.substring(BEARER_LENGTH ).trim());
            }
            response = service.analyze(request.getResumes(), request.getWordAttraction());
            return response;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

}

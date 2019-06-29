package shortlister.service.word.similarity.rest.controller;

import io.swagger.client.model.WordSimilarityRequest;
import io.swagger.client.model.WordSimilarityResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import shortlister.service.word.similarity.WordSimilarityService;

import java.util.UUID;

@RestController
public class WordSimilarityRestController {

    private final static Logger log = LoggerFactory.getLogger(WordSimilarityRestController.class);
    public final static String DEV_TOKEN;
    static {
        String devToken = System.getProperty("dev.token");
        if (devToken == null) {
            DEV_TOKEN = UUID.randomUUID().toString().replace("-","");
        } else {
            DEV_TOKEN = devToken;
        }
    }
    private final static int BEARER_LENGTH = "Bearer ".length();

    @Autowired
    private WordSimilarityService service;

    @RequestMapping("/v1/resumes")
    public WordSimilarityResponse postResumes(@RequestHeader("authorization") String authorization,
                                              @RequestBody WordSimilarityRequest request) {
        WordSimilarityResponse response = null;
        try {
            if (authorization.length() < BEARER_LENGTH) {
                throw new AccessDeniedException("Unauthorized");
            } else {
                String token = authorization.substring(BEARER_LENGTH).trim();
                if (!token.equals(DEV_TOKEN)) {
                    throw new AccessDeniedException("Unauthorized");
                }
            }

            response = service.analyze(request.getResumes(), request.getWordAttraction());
            return response;
        } catch (AccessDeniedException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage(), e);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

}

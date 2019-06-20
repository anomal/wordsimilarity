package shortlister.service.word.similarity;

import io.swagger.client.model.Resume;
import io.swagger.client.model.Word;
import io.swagger.client.model.WordSimilarityResponse;
import org.deeplearning4j.models.embeddings.inmemory.InMemoryLookupTable;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.deeplearning4j.models.word2vec.wordstore.VocabCache;
import org.deeplearning4j.plot.BarnesHutTsne;
import org.deeplearning4j.text.sentenceiterator.CollectionSentenceIterator;
import org.deeplearning4j.text.sentenceiterator.SentenceIterator;
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CommonPreprocessor;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.primitives.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import shortlister.service.word.similarity.processor.ResumeCollector;
import shortlister.service.word.similarity.processor.ResumeWordComparator;
import shortlister.service.word.similarity.processor.TechnicalResumePreProcessor;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

//@Slf4j
@Service
public class WordSimilarityService {
    private static Logger log = LoggerFactory.getLogger(WordSimilarityService.class);

    private static MathContext MATH_CONTEXT = new MathContext(3, RoundingMode.HALF_UP);


    @Autowired
    private TechnicalResumePreProcessor technicalResumePreProcessor;

    public WordSimilarityResponse analyze (List<Resume> resumes) throws IOException {

        ResumeCollector resumeCollector = new ResumeCollector(resumes, technicalResumePreProcessor);
        SentenceIterator iter = new CollectionSentenceIterator(resumeCollector.getResumeTexts());
        iter.setPreProcessor(technicalResumePreProcessor);

        // Split on white spaces in the line to get words
        TokenizerFactory t = new DefaultTokenizerFactory();
        t.setTokenPreProcessor(new CommonPreprocessor());

        log.debug("Building model....");
        Word2Vec vec = new Word2Vec.Builder()
                .minWordFrequency(2)
                .layerSize(100)//100)
                .seed(42)
                .windowSize(200)//200)
                .iterate(iter)
                .tokenizerFactory(t)
                .build();

        log.debug("Fitting Word2Vec model....");
        vec.fit();

        // Write word vectors
        WordVectorSerializer.writeWordVectors(vec, "words.txt");

        Map<String,Long> wordCounts = resumeCollector.getWordUniqueWordFrequencies();

        //STEP 2: Turn text input into a list of words
        log.info("Load & Vectorize data....");
        File wordFile = new File("words.txt");   //Open the file
        //Get the data of all unique word vectors
        Pair<InMemoryLookupTable,VocabCache> vectors = WordVectorSerializer.loadTxt(wordFile);
        VocabCache vocabCache = vectors.getSecond();
        INDArray weights = vectors.getFirst().getSyn0();    //seperate weights of unique words into their own list
        log.info("weights: {}", weights.shapeInfoToString());

        //STEP 3: build a dual-tree tsne to use later
        log.info("Build model....");
        BarnesHutTsne tsne = new BarnesHutTsne.Builder()
                .setMaxIter(100).theta(0.0D)//.theta(0.5)
                .normalize(false)
                .learningRate(500)
                .useAdaGrad(false)
//                .usePca(false)
                .build();

        tsne.fit(weights);

        INDArray coords = tsne.getData();
        log.info("coords: {}", coords.shapeInfoToString());

        List<Word> words2 = new ArrayList<>();
        for (int i = 0; i < coords.size(0); i++) {
            Word word = new Word();
            word.setName(vocabCache.wordAtIndex(i));
            double x = coords.getDouble(i,0);
            double y = coords.getDouble(i, 1);
            word.setX(new BigDecimal(x));
            word.setY(new BigDecimal(y));
            Long freq = wordCounts.get(word.getName());
            if (freq == null) {
                freq = 0L;
            }
            word.setFrequency(freq);
            BigDecimal size = new BigDecimal(freq).divide(
                    new BigDecimal(resumeCollector.getMaxUniqueWordFrequency()),
                    MATH_CONTEXT);
            word.setSize(size);
            log.info("{} {} {} {} {}", word.getName(), word.getFrequency(),
                    word.getSize(),
                    word.getX(), word.getY());
            words2.add(word);
        }

        List<Word> words3 = words2.stream()
                .filter(word -> word.getFrequency() > 1)
                .collect(Collectors.toList());

        WordSimilarityResponse response = new WordSimilarityResponse();
        words3.sort(new ResumeWordComparator());
        response.setWords(words3);
        log.info("words length is {}", words3.size());

        for (int i = 0; i < 10 && i < words3.size(); i++) {
            Word w = words3.get(i);
            Collection<String> wordsNearest = vec.wordsNearest(w.getName(), 10);
            for (String s : wordsNearest) {
                log.info("near {}: {}", w.getName(), s);
            }
        }



        return response;
    }
}

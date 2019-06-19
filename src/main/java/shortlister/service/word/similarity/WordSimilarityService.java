package shortlister.service.word.similarity;

import io.swagger.client.model.Resume;
import io.swagger.client.model.Word;
import io.swagger.client.model.WordSimilarityResponse;
import lombok.extern.slf4j.Slf4j;
import org.deeplearning4j.models.embeddings.inmemory.InMemoryLookupTable;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.deeplearning4j.models.word2vec.wordstore.VocabCache;
import org.deeplearning4j.plot.BarnesHutTsne;
import org.deeplearning4j.text.sentenceiterator.CollectionSentenceIterator;
import org.deeplearning4j.text.sentenceiterator.SentenceIterator;
import org.deeplearning4j.text.sentenceiterator.SentencePreProcessor;
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CommonPreprocessor;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;
import org.nd4j.linalg.api.buffer.DataBuffer;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.cpu.nativecpu.NDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.primitives.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import shortlister.service.word.similarity.processor.TechnicalResumePreProcessor;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

//@Slf4j
@Service
public class WordSimilarityService {
    private static Logger log = LoggerFactory.getLogger(WordSimilarityService.class);

    @Autowired
    private TechnicalResumePreProcessor technicalResumePreProcessor;

    public WordSimilarityResponse analyze (List<Resume> resumes) throws IOException {

        SentenceIterator iter = new CollectionSentenceIterator(
                resumes.stream().map(resume -> resume.getText()).collect(Collectors.toList()));
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



        //STEP 2: Turn text input into a list of words
        log.info("Load & Vectorize data....");
        File wordFile = new File("words.txt");   //Open the file
        //Get the data of all unique word vectors
        Pair<InMemoryLookupTable,VocabCache> vectors = WordVectorSerializer.loadTxt(wordFile);
        VocabCache vocabCache = vectors.getSecond();
        INDArray weights = vectors.getFirst().getSyn0();    //seperate weights of unique words into their own list

        Map<String,Long> wordCounts = technicalResumePreProcessor.getWordCounts();

        //Nd4j.setDataType(DataBuffer.Type.DOUBLE);
        List<String> cacheList = new ArrayList<>(); //cacheList is a dynamic array of strings used to hold all words
        List<Word> words = new ArrayList<>();
        for(int i = 0; i < vocabCache.numWords(); i++) {  //seperate strings of words into their own list
            cacheList.add(vocabCache.wordAtIndex(i));
            Word word = new Word();
            word.setName(vocabCache.wordAtIndex(i));
            String wordName = vocabCache.wordAtIndex(i);
            Long freq = wordCounts.get(wordName);
            if (freq == null) {
                freq = 0L;
            }
            word.setFrequency(freq);
            words.add(word);
            log.info("{} {} {}", i, word.getName(), word.getFrequency());
        }

        //STEP 3: build a dual-tree tsne to use later
        log.info("Build model....");
        BarnesHutTsne tsne = new BarnesHutTsne.Builder()
                .setMaxIter(100).theta(0.5)
                .normalize(false)
                .learningRate(500)
                .useAdaGrad(false)
//                .usePca(false)
                .build();

        //STEP 4: establish the tsne values and save them to a file
        log.info("Store TSNE Coordinates for Plotting....");
        //String outputFile = "target/archive-tmp/tsne-standard-coords.csv";
        //(new File(outputFile)).getParentFile().mkdirs();

        tsne.fit(weights);
        //tsne.saveAsFile(cacheList, outputFile);

        INDArray coords = tsne.getData();
        log.info(coords.shapeInfoToString());

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
            log.info("{} {} {} {} {}", word.getName(), word.getFrequency(),
                    vocabCache.wordFrequency(word.getName()),
                    word.getX(), word.getY());
            words2.add(word);
        }

        WordSimilarityResponse response = new WordSimilarityResponse();
        response.setWords(words2);

        return response;
    }
}

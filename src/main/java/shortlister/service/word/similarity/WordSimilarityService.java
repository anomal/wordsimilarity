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
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

//@Slf4j
@Service
public class WordSimilarityService {
    private static Logger log = LoggerFactory.getLogger(WordSimilarityService.class);

    public WordSimilarityResponse analyze (List<Resume> resumes) throws IOException {
        Map<String,Integer> wordCount = new HashMap<>();

        SentenceIterator iter = new CollectionSentenceIterator(
                resumes.stream().map(resume -> resume.getText()).collect(Collectors.toList()));
        iter.setPreProcessor((SentencePreProcessor) resume -> {
            //log.debug("sentence: {}", sentence);
            String preprocessed = resume
                    .toLowerCase()
                    .replace("pl/sql", "plsql")
                    .replaceAll("[^a-z0-9+#]", " ")
                    ;
            String[] tokenized = preprocessed.split(" ");
            Set<String> uniqueWords = new HashSet<>();
            for (String token : tokenized) {
                uniqueWords.add(token);
            }
            for (String uniqueWord : uniqueWords) {
                Integer count = wordCount.get(uniqueWord);
                if (count == null) {
                    wordCount.put(uniqueWord, 1);
                } else {
                    wordCount.put(uniqueWord, count + 1);
                }
            }
            log.debug("preprocessed: {}", preprocessed);
            return preprocessed;
        });

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
        VocabCache cache = vectors.getSecond();
        INDArray weights = vectors.getFirst().getSyn0();    //seperate weights of unique words into their own list

        //Nd4j.setDataType(DataBuffer.Type.DOUBLE);
        List<String> cacheList = new ArrayList<>(); //cacheList is a dynamic array of strings used to hold all words
        List<Word> words = new ArrayList<>();
        for(int i = 0; i < cache.numWords(); i++) {  //seperate strings of words into their own list
            cacheList.add(cache.wordAtIndex(i));
            Word word = new Word();
            word.setName(cache.wordAtIndex(i));
            String wordName = cache.wordAtIndex(i);
            Integer freq = wordCount.get(wordName);
            if (freq == null) {
                freq = 0;
            }
            word.setFrequency((long)freq);
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
            word.setName("word" + i);
            double x = coords.getDouble(i,0);
            double y = coords.getDouble(i, 1);
            word.setX(new BigDecimal(x));
            word.setY(new BigDecimal(y));
            log.info("{} {} {}", word.getName(), word.getX(), word.getY());
        }

        WordSimilarityResponse response = new WordSimilarityResponse();

        return response;
    }
}

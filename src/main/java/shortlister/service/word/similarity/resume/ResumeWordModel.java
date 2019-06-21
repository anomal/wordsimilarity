package shortlister.service.word.similarity.resume;

import org.deeplearning4j.models.word2vec.VocabWord;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.deeplearning4j.models.word2vec.wordstore.VocabCache;
import org.deeplearning4j.text.sentenceiterator.CollectionSentenceIterator;
import org.deeplearning4j.text.sentenceiterator.SentenceIterator;
import org.deeplearning4j.text.sentenceiterator.SentencePreProcessor;
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CommonPreprocessor;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResumeWordModel {

    private final static Logger log = LoggerFactory.getLogger(ResumeWordModel.class);
    private final Word2Vec word2Vec;

    public ResumeWordModel(ResumeDataRepository resumeDataRepository, SentencePreProcessor resumePreProcessor) {
        SentenceIterator iter = new CollectionSentenceIterator(resumeDataRepository.getResumeTexts());
        iter.setPreProcessor(resumePreProcessor);

        // Split on white spaces in the line to get words
        TokenizerFactory t = new DefaultTokenizerFactory();
        t.setTokenPreProcessor(new CommonPreprocessor());

        log.debug("Building model....");
        word2Vec = new Word2Vec.Builder()
                .minWordFrequency(2)
                .layerSize(32)//100)
                .seed(0)
                .windowSize(200)//200)
                .iterate(iter)
                .tokenizerFactory(t)
                .build();

        log.debug("Fitting Word2Vec model....");
        word2Vec.fit();
    }

    public INDArray getWeights() {
        return word2Vec.lookupTable().getWeights();
    }

    public VocabCache<VocabWord> getVocab() {
        return word2Vec.getVocab();
    }
}

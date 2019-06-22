package shortlister.service.word.similarity.visualization;

import org.deeplearning4j.models.word2vec.VocabWord;
import org.deeplearning4j.models.word2vec.wordstore.VocabCache;
import org.deeplearning4j.plot.BarnesHutTsne;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.io.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import shortlister.service.word.similarity.neuralnet.ResumeWordModel;

import java.math.BigDecimal;

public class ResumeWordCloud {

    private final static Logger log = LoggerFactory.getLogger(ResumeWordCloud.class);
    private final static long MIN_PERPLEXITY = 5;
    private final static BigDecimal DEFAULT_ATTRACTION = new BigDecimal("0.8");

    private final INDArray twoDCoords;
    private final VocabCache<VocabWord> vocabCache;
    private final long size;

    public ResumeWordCloud(ResumeWordModel resumeWordModel, BigDecimal wordAttraction) {

        Assert.notNull(resumeWordModel, "resumeWordModel cannot be null");

        INDArray nDCoordinates = resumeWordModel.getWeights();

        BigDecimal attraction = DEFAULT_ATTRACTION;
        if (wordAttraction != null) {
            attraction = BigDecimal.ONE.max(wordAttraction);
        }
        long perplexity = (long)Math.min(MIN_PERPLEXITY, nDCoordinates.size(0) * attraction.doubleValue());
        log.info("Using a perplexity of {}", perplexity);

        BarnesHutTsne tsne = new BarnesHutTsne.Builder()
                .setMaxIter(100).theta(0.0D)//.theta(0.5)
                .normalize(false)
                .learningRate(500)
                .useAdaGrad(false)
//                .usePca(false)
                .perplexity(perplexity)
                .build();

        tsne.fit(nDCoordinates);
        twoDCoords = tsne.getData();

        vocabCache = resumeWordModel.getVocab();

        size = twoDCoords.size(0);
    }

    public BigDecimal getX(int i) {
        return new BigDecimal(twoDCoords.getDouble(i, 0));
    }

    public BigDecimal getY(int i) {
        return new BigDecimal(twoDCoords.getDouble(i, 1));
    }

    public String getWordName(int i) {
        return vocabCache.wordAtIndex(i);
    }

    public long getSize() {
        return size;
    }
}

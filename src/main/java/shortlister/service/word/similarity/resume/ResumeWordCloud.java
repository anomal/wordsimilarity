package shortlister.service.word.similarity.resume;

import org.deeplearning4j.models.word2vec.VocabWord;
import org.deeplearning4j.models.word2vec.wordstore.VocabCache;
import org.deeplearning4j.plot.BarnesHutTsne;
import org.nd4j.linalg.api.ndarray.INDArray;

import java.math.BigDecimal;

public class ResumeWordCloud {

    private final INDArray twoDCoords;
    private final VocabCache<VocabWord> vocabCache;
    private final long size;

    public ResumeWordCloud(ResumeWordModel resumeWordModel) {
        INDArray nDCoordinates = resumeWordModel.getWeights();

        BarnesHutTsne tsne = new BarnesHutTsne.Builder()
                .setMaxIter(100).theta(0.0D)//.theta(0.5)
                .normalize(false)
                .learningRate(500)
                .useAdaGrad(false)
//                .usePca(false)
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

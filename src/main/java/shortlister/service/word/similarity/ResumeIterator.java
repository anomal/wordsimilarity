package shortlister.service.word.similarity;

import io.swagger.client.model.Resume;
import org.deeplearning4j.text.sentenceiterator.SentenceIterator;
import org.deeplearning4j.text.sentenceiterator.SentencePreProcessor;

import java.util.List;

public class ResumeIterator implements SentenceIterator{

    private List<Resume> resumes;
    private SentencePreProcessor preProcessor;

    public ResumeIterator(SentencePreProcessor preProcessor, List<Resume> resumes) {
        this.resumes = resumes;
        this.preProcessor = preProcessor;
    }

    @Override
    public String nextSentence() {
        return null;
    }

    @Override
    public boolean hasNext() {
        return false;
    }

    @Override
    public void reset() {

    }

    @Override
    public void finish() {

    }

    @Override
    public SentencePreProcessor getPreProcessor() {
        return null;
    }

    @Override
    public void setPreProcessor(SentencePreProcessor sentencePreProcessor) {

    }
}

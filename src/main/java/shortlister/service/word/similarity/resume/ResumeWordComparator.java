package shortlister.service.word.similarity.resume;

import io.swagger.client.model.Word;

import java.util.Comparator;

public class ResumeWordComparator implements Comparator<Word> {

    @Override
    public int compare(Word w1, Word w2) {
        int frequencyComparison = w2.getFrequency().compareTo(w1.getFrequency());
        if (frequencyComparison == 0) {
            Integer w1Len = w1.getName().length();
            Integer w2Len = w2.getName().length();
            int lengthComparison = w2Len.compareTo(w1Len);
            if (lengthComparison == 0) {
                return w1.getName().compareTo(w2.getName());
            } else {
                return lengthComparison;
            }
        } else {
            return frequencyComparison;
        }
    }
}

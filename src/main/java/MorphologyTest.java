import morphology.Morphology;

import java.util.Map;

public class MorphologyTest {
    public static void main(String[] args) {
        Morphology morphology = new Morphology();
        Map<String, Integer> words = morphology.countWords("На дворе — трава, на траве — дрова. Не руби дрова на траве двора!");
        Map<String, Integer> words2 = morphology.countWords("Повторное появление леопарда в Осетии позволяет предположить, что леопард постоянно обитает в некоторых районах Северного Кавказа.");

        System.out.println(words);
        System.out.println(words2);
    }
}

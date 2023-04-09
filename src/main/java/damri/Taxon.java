package damri;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Taxon {
    private Integer id;
    private Map<Character, List<String>> letterToSpecies;
    private Integer countOfSequences;
    public Taxon(Integer id) {
        this.id = id;
        this.letterToSpecies = new HashMap<>();
        countOfSequences = 0;
    }
    public void addData(Character letter, String organism, String gene) {
        List<String> species = letterToSpecies.get(letter);
        if (species == null) {
            letterToSpecies.put(letter, new ArrayList<>());
            species = letterToSpecies.get(letter);
        }
        species.add(
                String.format("%s (%s)",
                        organism, gene))
        ;
        countOfSequences++;
    }

    public Map<Character, List<String>> getLetterToSpecies() {
        return letterToSpecies;
    }

    public Integer getCountOfSequences() {
        return countOfSequences;
    }

    public Integer getId() {
        return id;
    }
}

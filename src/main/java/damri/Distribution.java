package damri;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Distribution {
    private static final int MAX_TO_COMMENT = 10;
    private Map<Integer, Taxon> data;

    public Distribution(Map<Integer, Taxon> data) {
        this.data = data;
    }

    public String summarize(Map<Integer, String> taxonomy) {
        StringBuffer result = new StringBuffer();
        List<Taxon> biggestToSmallest = data.values().stream().sorted(
                Comparator.comparing(Taxon::getCountOfSequences).reversed()
        ).collect(Collectors.toList());
        for(Taxon taxon : biggestToSmallest){
            String taxonName = taxon.getId().toString();
            if (taxonomy.get(taxon.getId()) != null){
                taxonName = taxonomy.get(taxon.getId());
            }
            result.append(taxonName + ":\n");
            for(Character letter : taxon.getLetterToSpecies().keySet()){
                String comment = "";
                if(taxon.getLetterToSpecies().get(letter).size() <= MAX_TO_COMMENT){
                    comment = ": " + String.join(", ", taxon.getLetterToSpecies().get(letter));
                }
                result.append(String.format("\t%s - %d%s\n", letter, taxon.getLetterToSpecies().get(letter).size(), comment));
            }
            result.append("\n");
        }
        return result.toString();
    }
}

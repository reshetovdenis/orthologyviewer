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
            if(taxon.getId().equals(131567) || taxon.getId().equals(1)){
                continue;
            }
            String taxonName = taxon.getId().toString();
            if (taxonomy.get(taxon.getId()) != null){
                taxonName = taxonomy.get(taxon.getId());
            }

            result.append(String.format("%s (%d):\n", taxonName, taxon.getCountOfSequences()));
            List<Character> characters = taxon.getLetterToSpecies().keySet().stream().collect(Collectors.toList());
            for(Character letter : characters){
                String comment = "";
                Double percentValue = 100*(double)taxon.getLetterToSpecies().get(letter).size() / taxon.getCountOfSequences();
                String percentSttring = String.format("%.0f", percentValue);
                if(percentSttring.length() == 1){
                    percentSttring = " "+percentSttring;
                }

                if(taxon.getLetterToSpecies().get(letter).size() <= MAX_TO_COMMENT){
                    comment = ": " + String.join(", ", taxon.getLetterToSpecies().get(letter));
                }
                result.append(String.format("\t%s\t%s (%s)%s\n", letter,
                        percentSttring + " %",
                        taxon.getLetterToSpecies().get(letter).size(),
                        comment));
                if(letter != characters.get(characters.size()-1)){
                    result.append("\t-----\n");
                }
            }
            result.append("---------------------------------------\n");
        }
        return result.toString();
    }
}

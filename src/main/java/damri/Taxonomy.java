package damri;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Taxonomy {
    private static final Taxonomy instance = new Taxonomy();
    private Map<Integer, Integer> taxHierarchy = new HashMap<>();
    private Map<Integer, String> taxNames = new HashMap<>();
    private Map<String, List<Integer>> speciesTaxonsMap = new HashMap<>();
    private Taxonomy(){
        String nodesFile = "/Users/adr/tmp/nodes.dmp";
        String namesFile = "/Users/adr/tmp/names.dmp";
        try {
            taxHierarchy = loadTaxHierarchy(nodesFile);
            taxNames = loadTaxNames(namesFile);
            speciesTaxonsMap = generateSpeciesTaxonsMap(taxHierarchy, taxNames);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static Taxonomy getInstance() {
        return instance;
    }

    public static void main(String[] args) throws IOException {

    }

    private static Map<String, List<Integer>> generateSpeciesTaxonsMap(Map<Integer, Integer> taxHierarchy, Map<Integer, String> taxNames) throws IOException {
        Map<String, List<Integer>> speciesTaxonsMap = new HashMap<>();

        for (Integer speciesId : taxNames.keySet()) {
            if (taxHierarchy.containsKey(speciesId)) {
                List<Integer> lineage = getLineage(speciesId, taxHierarchy);
                speciesTaxonsMap.put(speciesId+"_0", lineage);
            }
        }

        return speciesTaxonsMap;
    }

    private static Map<Integer, Integer> loadTaxHierarchy(String nodesFile) throws IOException {
        Map<Integer, Integer> taxHierarchy = new HashMap<>();
        BufferedReader reader = new BufferedReader(new FileReader(nodesFile));
        String line;

        while ((line = reader.readLine()) != null) {
            String[] fields = line.split("\t\\|\t");
            int taxId = Integer.parseInt(fields[0]);
            int parentTaxId = Integer.parseInt(fields[1]);
            taxHierarchy.put(taxId, parentTaxId);
        }

        reader.close();
        return taxHierarchy;
    }

    public static Map<Integer, String> loadTaxNames(String namesFile) throws IOException {
        Map<Integer, String> taxNames = new HashMap<>();
        BufferedReader reader = new BufferedReader(new FileReader(namesFile));
        String line;

        while ((line = reader.readLine()) != null) {
            String[] fields = line.split("\t\\|\t");
            if (fields[3].equals("scientific name\t|")) {
                int taxId = Integer.parseInt(fields[0]);
                taxNames.put(taxId, fields[1]);
            }
        }

        reader.close();
        return taxNames;
    }

    private static List<Integer> getLineage(int speciesId, Map<Integer, Integer> taxHierarchy) {
        List<Integer> lineage = new ArrayList<>();
        Integer taxId = speciesId;

        while (taxId != null && taxId != 1) {
            lineage.add(taxId);
            taxId = taxHierarchy.get(taxId);
        }

        if (taxId != null) {
            lineage.add(1); // Add root taxon (cellular organisms)
        }

        return lineage;
    }

    public Map<Integer, Integer> getTaxHierarchy() {
        return taxHierarchy;
    }

    public Map<Integer, String> getTaxNames() {
        return taxNames;
    }

    public Map<String, List<Integer>> getSpeciesTaxonsMap() {
        return speciesTaxonsMap;
    }
}


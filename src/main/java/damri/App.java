package damri;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class App {
    public static void main(String[] args) throws IOException {
        int letterNumber = 320;
        String sequenceId = "Homo|sp|Q96F07-2|CYFP2_HUMAN: Isoform 2 of Cytoplasmic FMR1-interacting protein 2 OS=Homo sapiens OX=9606 GN=CYFIP2";
        String alignmentFile = "/Users/adr/programming/orthologyviewer/src/test/resources/alignment.fasta";
        String speciesToTaxonomyFile = "/Users/adr/programming/orthologyviewer/src/test/resources/odb11v0_level2species.tab";
        //wget https://ftp.ncbi.nlm.nih.gov/pub/taxonomy/taxdump.tar.gz

        String outputAlignmentFile = "/Users/adr/programming/orthologyviewer/src/test/resources/readable_alignment.fasta";
        String statisticsFile = "/Users/adr/programming/orthologyviewer/src/test/resources/outStatistics.txt";


        Map<String, String> alignment = readFastaFile(alignmentFile);

        Map<Integer, String> taxonIdToName = Taxonomy.getInstance().getTaxNames();
        Map<Integer, String> taxonIdToNameAdd = getTaxonNamesFromAlignment(alignment);
        taxonIdToName.putAll(taxonIdToNameAdd);

        Map<String, List<Integer>> speciesToTaxonomy = Taxonomy.getInstance().getSpeciesTaxonsMap();
        Map<String, List<Integer>> speciesToTaxonomyAdd = parseLevel2Species(speciesToTaxonomyFile);
        for(String key : speciesToTaxonomyAdd.keySet()){
            if(!speciesToTaxonomy.containsKey(key)){
                speciesToTaxonomy.put(key, speciesToTaxonomyAdd.get(key));
            }
        }

        String withDashes = alignment.get(sequenceId);
        String noDashes = withDashes.replaceAll("-", "");
        int alignmentPosition = getSymbolIndexWithDashes(noDashes, letterNumber, withDashes);
        Map<String, Character> column = getColumnData(alignment, alignmentPosition);
        Distribution distribution = getDistribution(column, speciesToTaxonomy);
        createStatisticsFile(statisticsFile,distribution.summarize(taxonIdToName));
        Map<String, String> readAbleAlignment = createReadableNames(alignment);
        createFastaFile(outputAlignmentFile, readAbleAlignment);
    }

    private static Map<String, String> createReadableNames(Map<String, String> alignment) {
        Map<String, String> readAbleAlignment = new HashMap<>();

        for (String name : alignment.keySet()) {
            try {
                SequenceData data = SequenceData.fromString(name);
                String newKey = String.format("%s|%s", data.getOrganism().replaceAll(" ", "_"), data.getGeneId());
                if(!readAbleAlignment.containsKey(newKey)){
                    readAbleAlignment.put(newKey, alignment.get(name));
                } else {
                    String seqInAlignment = readAbleAlignment.get(newKey);
                    if(!seqInAlignment.equals(alignment.get(name))){
                        newKey = newKey + "_see_if_its_error";
                        readAbleAlignment.put(newKey, alignment.get(name));
                    }
                }
            } catch (Exception e) {
                readAbleAlignment.put(name, alignment.get(name)
                );
            }
        }
        return readAbleAlignment;
    }


    public static Distribution getDistribution(Map<String, Character> column, Map<String, List<Integer>> speciesToTaxonomy){
        Map<Integer, Taxon> distribution = new HashMap<Integer, Taxon>();

        for (String name : column.keySet()) {
            Character letter = column.get(name);
            try {
                SequenceData data = SequenceData.fromString(name);
                List<Integer> taxons = speciesToTaxonomy.get(data.getTaxonId());
                for (Integer taxonId : taxons) {
                    Taxon taxon = distribution.get(taxonId);
                    if (taxon == null) {
                        distribution.put(taxonId, new Taxon(taxonId));
                        taxon = distribution.get(taxonId);
                    }
                    taxon.addData(letter, data.getOrganism(), data.getGeneId());
                }
            } catch (Exception e) {
            }
        }
        return new Distribution(distribution);
    }

    public static Map<Integer, String> getTaxonNamesFromAlignment(Map<String, String> alignment){
        Map<Integer, String> idToName = new HashMap<Integer, String>();
        for (String name : alignment.keySet()) {
            try {
                SequenceData data = SequenceData.fromString(name);
                idToName.put(Integer.parseInt(data.getTaxonId().replaceAll("_0", "")),
                        data.getOrganism());
            } catch (Exception e) {
            }
        }
        return idToName;
    }

    public static Map<String, String> readFastaFile(String fastaFilePath) {
        Map<String, String> sequences = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(fastaFilePath))) {
            String line;
            String description = "";
            StringBuilder sequence = new StringBuilder();

            while ((line = br.readLine()) != null) {
                if (line.startsWith(">")) {
                    if (!description.isEmpty()) {
                        sequences.put(description, sequence.toString());
                        sequence.setLength(0); // Reset the sequence
                    }
                    description = line.substring(1).trim();
                } else {
                    sequence.append(line.trim());
                }
            }

            // Add the last sequence to the map
            if (!description.isEmpty()) {
                sequences.put(description, sequence.toString());
            }
        } catch (IOException e) {
            System.err.println("Error reading the FASTA file: " + e.getMessage());
        }

        return sequences;
    }

    public static Map<String, Character> getColumnData(Map<String, String> sequences, int alignmentColumn) {
        Map<String, Character> columnData = new HashMap<>();
        for (String key : sequences.keySet()) {
            String sequence = sequences.get(key);
            if (alignmentColumn < sequence.length()) {
                columnData.put(key, sequence.charAt(alignmentColumn));
            } else {
                System.err.println("Warning: Sequence shorter than specified alignment column.");
            }
        }
        return columnData;
    }

    public static int getSymbolIndexWithDashes(String noDashes, int symbolIndexWithoutDashesOneBased, String withDashes) {
        int symbolIndexWithoutDashes = symbolIndexWithoutDashesOneBased - 1;
        if (symbolIndexWithoutDashes >= noDashes.length()) {
            throw new IllegalArgumentException("Symbol index is out of range.");
        }

        char targetSymbol = noDashes.charAt(symbolIndexWithoutDashes);
        int symbolCount = 0;
        int originalIndex = 0;

        for (int i = 0; i < withDashes.length(); i++) {
            if (withDashes.charAt(i) != '-') {
                if (symbolCount == symbolIndexWithoutDashes) {
                    originalIndex = i;
                    break;
                }
                symbolCount++;
            }
        }

        // If the target symbol is not found in the original string, return -1
        if (originalIndex == 0 && withDashes.charAt(0) != targetSymbol) {
            return -1;
        }

        return originalIndex;
    }


    public static Map<String, List<Integer>> parseLevel2Species(String filePath) throws IOException {
        Map<String, List<Integer>> resultMap = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] columns = line.split("\t");
                if (columns.length >= 4) {
                    String key = columns[1];
                    List<Integer> values = parseValuesFromLevel2Species(columns[3]);
                    resultMap.put(key, values);
                }
            }
        }

        return resultMap;
    }

    private static List<Integer> parseValuesFromLevel2Species(String valuesString) {
        String[] valueStrings = valuesString.replaceAll("[\\{\\}]", "").split(",");
        List<Integer> values = new ArrayList<>(valueStrings.length);
        for (String valueString : valueStrings) {
            values.add(Integer.parseInt(valueString));
        }
        return values.stream().distinct().collect(Collectors.toList());
    }

    public static Map<Integer, String> parseLevel(String filePath) throws IOException {
        Map<Integer, String> resultMap = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] columns = line.split("\t");
                if (columns.length >= 5) {
                    Integer key = Integer.parseInt(columns[0]);
                    String value = columns[1];
                    resultMap.put(key, value);
                }
            }
        }

        return resultMap;
    }
    public static void createFastaFile(String fileName, Map<String, String> sequences) throws IOException {
        File file = new File(fileName);

        if (!file.exists()) {
            file.createNewFile();
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            for (Map.Entry<String, String> entry : sequences.entrySet()) {
                String sequenceName = entry.getKey();
                String sequence = entry.getValue();

                bw.write(">" + sequenceName + "\n");

                int lineLength = 60;
                for (int i = 0; i < sequence.length(); i += lineLength) {
                    int endIndex = Math.min(i + lineLength, sequence.length());
                    String subSequence = sequence.substring(i, endIndex);
                    bw.write(subSequence + "\n");
                }
            }
        }
    }
    public static void createStatisticsFile(String fileName, String output) throws IOException {
        File file = new File(fileName);

        if (!file.exists()) {
            file.createNewFile();
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            bw.write("ATTENTION: taxonomy was read from different sources thus it may look inconsistent between taxonomy groups.\n");
            bw.write("---------------------------------------\n");
            bw.write(output);
        }
    }
}

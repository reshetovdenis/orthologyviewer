package damri;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class SequenceData {
    //>9447_0:001c71 {"pub_og_id":"446059at2759","og_name":"cytoplasmic FMR1 interacting protein 1 ","level_taxid":2759,"organism_taxid":"9447_0","organism_name":"Lemur catta","pub_gene_id":"123638021","description":"cytoplasmic FMR1-interacting protein 2"}
    private String taxonId;
    private String organism;
    private String geneId;
    private String geneDescription;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public SequenceData(String taxonId, String organism, String geneId, String geneDescription) {
        this.taxonId = taxonId;
        this.organism = organism;
        this.geneId = geneId;
        this.geneDescription = geneDescription;
    }

    public static SequenceData fromString(String data) throws IOException {
        String[] input = data.split(" ", 2);
        JsonNode jsonNode = objectMapper.readTree(input[1]);
        String taxonId = jsonNode.get("organism_taxid").asText();
        String organism = jsonNode.get("organism_name").asText();
        String geneId = jsonNode.get("pub_gene_id").asText();
        String geneDescription = jsonNode.get("description").asText();
        SequenceData result = new SequenceData(taxonId, organism, geneId, geneDescription);
        return result;
    }

    public String getTaxonId() {
        return taxonId;
    }

    public String getOrganism() {
        return organism;
    }

    public String getGeneId() {
        return geneId;
    }

    public String getGeneDescription() {
        return geneDescription;
    }
}

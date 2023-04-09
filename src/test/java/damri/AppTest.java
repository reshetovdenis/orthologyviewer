package damri;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertEquals;

/**
 * Unit test for simple App.
 */
public class AppTest {

    @Test
    public void testReadFastaFile() throws IOException {
        Map result = App.readFastaFile(getAbsolutePathNameOfTestResource("two_sequences.fasta"));
        assertEquals(result.keySet().size(), 2);
        assertEquals(result.get("one some info"), "M-THVTLEDAL");
    }

    @Test
    public void testGetColumnData() throws IOException {
        Map result = App.readFastaFile(getAbsolutePathNameOfTestResource("two_sequences.fasta"));
        Map<String, Character> column = App.getColumnData(result, 1);
        assertEquals(column.size(), 2);
        assertEquals(column.get("one some info"), '-');
    }

    @Test
    public void testGetSymbolIndexWithDashes() throws IOException {
        int zeroBasedStart = App.getSymbolIndexWithDashes("MTHVTLEDAL", 1, "M-THVTLEDAL");
        assertEquals(zeroBasedStart, 0);
        int dontCountGaps = App.getSymbolIndexWithDashes("MTHVTLEDAL", 2, "M-THVTLEDAL");
        assertEquals(dontCountGaps, 2);
    }

    @Test
    public void testParseLevel2Species() throws IOException {
        // Test method
        Map<String, List<Integer>> result = App.parseLevel2Species(getAbsolutePathNameOfTestResource("example_odb11v0_level2species.tab"));

        // Prepare expected data
        Map<String, List<Integer>> expectedData = new HashMap<>();
        expectedData.put("9606_0", Arrays.asList(2759, 33208, 7742, 32523, 40674, 9347, 314146, 9443, 314295, 9604, 9606));
        expectedData.put("1000373_1", Arrays.asList(10239, 1000373));
        expectedData.put("1016879_1", Arrays.asList(10239, 1016879));

        // Validate result
        Assert.assertEquals(result, expectedData);
    }

    @Test
    public void testParseLevel() throws IOException {
        // Test method
        Map<Integer, String> result = App.parseLevel(getAbsolutePathNameOfTestResource("example_odb11v0_levels.tab"));

        // Prepare expected data
        Map<Integer, String> expectedData = new HashMap<>();
        expectedData.put(2, "Bacteria");
        expectedData.put(18, "Pelobacter");
        expectedData.put(22, "Shewanella");

        // Validate result
        Assert.assertEquals(result, expectedData);
    }

    @Test
    public void testNameToData() throws IOException {
        SequenceData expected = new SequenceData(9447, "Lemur catta", "123638021", "cytoplasmic FMR1-interacting protein 2");
        SequenceData actual = SequenceData.fromString(">9447_0:001c71 {\"pub_og_id\":\"446059at2759\",\"og_name\":\"cytoplasmic FMR1 interacting protein 1 \",\"level_taxid\":2759,\"organism_taxid\":\"9447_0\",\"organism_name\":\"Lemur catta\",\"pub_gene_id\":\"123638021\",\"description\":\"cytoplasmic FMR1-interacting protein 2\"}");
        Assert.assertEquals(actual.getTaxonId(), expected.getTaxonId());
        Assert.assertEquals(actual.getOrganism(), expected.getOrganism());
        Assert.assertEquals(actual.getGeneId(), expected.getGeneId());
        Assert.assertEquals(actual.getGeneDescription(), expected.getGeneDescription());
    }

    public static String getAbsolutePathNameOfTestResource(String name) {
        Path resourceDirectory = Paths.get("src", "test", "resources");
        return String.format("%s/%s", resourceDirectory.toAbsolutePath().toString(), name);
    }
}

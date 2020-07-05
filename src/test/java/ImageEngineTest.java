import java.io.IOException;

import io.vavr.collection.List;
import io.vavr.collection.Seq;
import org.junit.Test;

public class ImageEngineTest {

    @Test
    public void testFlatMap() {
        final Seq<Integer> list = List.of(1, 2, 3, 4, 5).flatMap(ImageEngineTest::addTenToList);
        list.forEach(System.out::println);
    }

    private static List<Integer> addTenToList(int i) {
        return List.of(i, 10);
    }

    @Test
    public void RegressionTest() {

    }

    @Test
    public void givenFile_whenChecksumUsingGuava_thenVerifying()
    throws IOException {
        String filename = "src/test/resources/test_md5.txt";
        String checksum = "5EB63BBBE01EEED093CB22BB8F5ACDC3";

        HashCode hash = com.google.common.io.Files
                        .hash(new File(filename), Hashing.md5());
        String myChecksum = hash.toString()
                                .toUpperCase();

        assertThat(myChecksum.equals(checksum)).isTrue();

}

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
}

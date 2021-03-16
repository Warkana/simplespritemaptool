package com.vptech.spritemaptool;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Collectors;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import com.google.gson.JsonObject;
;
import com.vptech.spritemaptool.images.ImageEngine;
import com.vptech.spritemaptool.images.ImageInfo;
import io.vavr.collection.List;
import io.vavr.collection.Seq;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

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
    public void testJsonBuilding() {
        final JsonObject obj = new JsonObject();
        final JsonObject obj2 = new JsonObject();
        obj.addProperty("name", "vova");
        obj.addProperty("age", "24");
        obj2.add("Student", obj);

        System.out.println(obj2.toString());
    }

    @SuppressWarnings("deprecation")
    @Test
    public void givenSpriteMapFile_thenChecksum_verifying()
    throws IOException {
        final String resFolder = "src/test/resources";
        final String fileName = "/spritemap";
        final String checksum = "8124BC8C0C7626542AD6DFBA04406560";


        final List<String> imagePaths = List.ofAll(SpriteMapTool.getImageInfoList(resFolder.concat("/testSet"))
                                                     .stream()
                                                     .sorted(Comparator.comparing(im -> (im.getHeight() * im.getWidth())))
                                                     .map(ImageInfo::getPath)
                                                     .map(Path::toString)
                                                     .collect(Collectors.toList()));//TODO move to vavr

        ImageEngine.concatImages(io.vavr.collection.List.ofAll(imagePaths), resFolder.concat(fileName));

        final HashCode hash = Files.hash(new File(resFolder.concat(fileName).concat(".json")), Hashing.md5());

        String myChecksum = hash.toString()
                                .toUpperCase();

        assertEquals(checksum, myChecksum);
    }
}

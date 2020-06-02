package com.vptech.spritemaptool.images;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class ImageMagick {

    private static final String SPACE_STR = " ";
    private static final String MONTAGE = "montage";
    private static final String BACKGROUND_TRANSPARENT = "-background transparent";
    private static final String GEOMETRY_1_1 = "-geometry +1+1";
    private static boolean isWindows;

    static {
        isWindows = System.getProperty("os.name")
                          .toLowerCase().startsWith("windows");
    }

    public static void montage(String imageFolder, List<String> imageNames) throws IOException, InterruptedException {
        ProcessBuilder builder = new ProcessBuilder();
        StringBuilder commandBuilder = new StringBuilder(MONTAGE);
        commandBuilder.append(SPACE_STR);
        commandBuilder.append(BACKGROUND_TRANSPARENT);
        commandBuilder.append(SPACE_STR);
        commandBuilder.append(GEOMETRY_1_1);
        commandBuilder.append(SPACE_STR);
        commandBuilder.append("-trim ");
        commandBuilder.append("-tile 3x4 ");
        commandBuilder.append(String.join(" ", imageNames));
        commandBuilder.append(" ../result.png");

        if (isWindows) {
            throw new RuntimeException("Windows is not supported now");
        } else {
            builder.command("sh", "-c", commandBuilder.toString());
        }

        builder.directory(new File(imageFolder));
        Process process = builder.start();
        StreamGobbler streamGobbler = new StreamGobbler(process.getInputStream(), process.getErrorStream(), System.out::println);
        Executors.newSingleThreadExecutor().submit(streamGobbler);
        int exitCode = process.waitFor();
        assert exitCode == 0;
    }

    private static class StreamGobbler implements Runnable {

        private InputStream inputStream;
        private InputStream errorStream;
        private Consumer<String> consumer;

        StreamGobbler(InputStream inputStream, InputStream errorStream, Consumer<String> consumer) {
            this.inputStream = inputStream;
            this.errorStream = errorStream;
            this.consumer = consumer;
        }

        @Override
        public void run() {
            new BufferedReader(new InputStreamReader(inputStream)).lines()
                                                                  .forEach(consumer);
            new BufferedReader(new InputStreamReader(errorStream)).lines()
                                                                  .forEach(consumer);
            System.exit(0);
        }
    }
}

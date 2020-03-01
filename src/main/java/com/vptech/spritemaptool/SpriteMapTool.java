package com.vptech.spritemaptool;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class SpriteMapTool {

    private final static String WORK_FOLDER_PATH = "folderpath";

    void run(String[] args) {

        CommandLine line = parseArguments(args);

        if (line.hasOption(WORK_FOLDER_PATH)) {

            System.out.println(line.getOptionValue(WORK_FOLDER_PATH));
            String folderPath = line.getOptionValue(WORK_FOLDER_PATH);
            try {
                List<Path> data = getImages(folderPath);
                data.forEach(p -> System.out.println(p.toString()));
            } catch (IOException e) {
                System.out.println("Can't read images from folder");
                e.printStackTrace();
            }
        } else {
            printAppHelp();
        }
    }

    private CommandLine parseArguments(String[] args) {

        Options options = getOptions();
        CommandLine line = null;

        CommandLineParser parser = new DefaultParser();

        try {
            line = parser.parse(options, args);

        } catch (ParseException ex) {

            System.err.println("Failed to parse command line arguments");
            System.err.println(ex.toString());
            printAppHelp();

            System.exit(1);
        }

        return line;
    }

    private static List<Path> getImages(String folderPath) throws IOException {
        try (Stream<Path> paths = Files.walk(Paths.get(folderPath))) {
            return paths.filter(Files::isRegularFile)
                        .filter(SpriteMapTool::isImage)
                        .collect(Collectors.toList());
        }
    }

    private static boolean isImage(Path path) {
        final String fileName = path.getFileName().toString();
        return getExtensionByStringHandling(fileName)
               .map(ext -> ext.equals("png"))
               .orElse(false);

    }

    private static Optional<String> getExtensionByStringHandling(String fileName) {
        return Optional.ofNullable(fileName)
                       .filter(f -> f.contains("."))
                       .map(f -> f.substring(fileName.lastIndexOf(".") + 1));
    }

    private static Options getOptions() {

        final Options options = new Options();

        options.addOption("f", WORK_FOLDER_PATH, true, "folder path to load data from");
        return options;
    }

    private void printAppHelp() {

        final Options options = getOptions();

        final HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("SSMTool", options, true);
    }
}


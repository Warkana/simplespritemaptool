package com.vptech.spritemaptool;

import com.vptech.spritemaptool.images.ImageEngine;
import com.vptech.spritemaptool.images.ImageInfo;
import com.vptech.spritemaptool.images.ImageMagick;
import io.vavr.collection.Seq;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.imageio.ImageIO;

class SpriteMapTool {

    private final static String WORK_FOLDER_PATH = "imagefolder";

    void run(String[] args) {

        CommandLine line = parseArguments(args);

        if (line.hasOption(WORK_FOLDER_PATH)) {
            final String folderPath = line.getOptionValue(WORK_FOLDER_PATH);
            try {
                final List<ImageInfo> imageInfoList = getImageInfoList(folderPath);
                ImageMagick.montage(folderPath, getImages(imageInfoList));
            } catch (InterruptedException | IOException e) {
                System.out.println("Can't read images from folder");
                e.printStackTrace();
            }
        } else {
            printAppHelp();
        }
    }

    private CommandLine parseArguments(String[] args) {

        final Options options = getOptions();
        CommandLine line = null;

        final CommandLineParser parser = new DefaultParser();

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

    public void buildSpriteMap(String[] args) {
        CommandLine line = parseArguments(args);

        if (line.hasOption(WORK_FOLDER_PATH)) {
            final String folderPath = line.getOptionValue(WORK_FOLDER_PATH);
            try {
                final List<String> imagePaths = getImageInfoList(folderPath).stream()
                                                                            .sorted(Comparator.comparing(im -> (im.getHeight() * im.getWidth())))
                                                                            .map(ImageInfo::getPath)
                                                                            .map(Path::toString)
                                                                            .collect(Collectors.toList());

                ImageEngine.concatImages(io.vavr.collection.List.ofAll(imagePaths));
            } catch (IOException e) {
                System.out.println("Can't read images from folder");
                e.printStackTrace();
            }
        } else {
            printAppHelp();
        }
    }

    private static List<ImageInfo> getImageInfoList(String folderPath) throws IOException {
        try (Stream<Path> paths = Files.walk(Paths.get(folderPath))) {
            return paths.filter(Files::isRegularFile)
                        .filter(SpriteMapTool::isImage)
                        .map(SpriteMapTool::getInfo)
                        .collect(Collectors.toList());
        }
    }

    private static List<String> getImages(List<ImageInfo> imageInfoList) {
        return imageInfoList.stream()
                            .sorted(Comparator.comparing(im -> -(im.getHeight() + im.getWidth())))
                            .map(i -> i.getPath().getFileName().toString())
                            .collect(Collectors.toList());
    }

    private static ImageInfo getInfo(Path imagePath) {
        try {
            final BufferedImage bimg = ImageIO.read(new File(imagePath.toString()));
            final int w = bimg.getWidth();
            final int h = bimg.getHeight();
            return new ImageInfo(imagePath, w, h);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Can't create image from file: " + imagePath.toString());
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


package com.vptech.spritemaptool.images;

import java.nio.file.Path;

public class ImageInfo {
    final private int w;
    final private int h;
    final private Path filePath;

    public ImageInfo(Path fileName, int w, int h){
        this.filePath = fileName;
        this.w = w;
        this.h = h;
    }

    public int getWidth() {
        return w;
    }

    public int getHeight() {
        return h;
    }

    public Path getPath() {
        return filePath;
    }
}

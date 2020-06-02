package com.vptech.spritemaptool.descriptor.model;

public class ImageBox {
    private int x;
    private int y;
    private int w;
    private int h;

    private ImageBox() {}

    public int getX() {
        return x;
    }
    public int getY() {
        return y;
    }
    public int getW() {
        return w;
    }
    public int getH() {
        return h;
    }

    public static class ImageSpecBuilder {
        private int x;
        private int y;
        private int w;
        private int h;

        public ImageSpecBuilder() {}

        public ImageSpecBuilder withX(int x) {
            this.x = x;
            return this;
        }

        public ImageSpecBuilder withY(int y) {
            this.y = y;
            return this;
        }

        public ImageSpecBuilder withWidth(int width) {
            this.w = width;
            return this;
        }

        public ImageSpecBuilder withHeight(int height) {
            this.h = height;
            return this;
        }

        public ImageBox build() {
            final ImageBox imageData = new ImageBox();
            imageData.x = this.x;
            imageData.y = this.y;
            imageData.h = this.h;
            imageData.w = this.w;
            return imageData;
        }
    }
}

package com.vptech.spritemaptool.descriptor.model;

public class ResourceData {
    private String fileName;
    private ImageBox frame;
    private Boolean rotated;
    private Boolean trimmed;
    private ImageBox spriteSourceSize;
    private Size sourceSize;

    public ImageBox getFrame() {
        return frame;
    }

    public String getFileName() {
        return fileName;
    }

    public Boolean isRotated() {
        return rotated;
    }

    public Boolean isTrimmed() {
        return trimmed;
    }

    public ImageBox getSpriteSourceSize() {
        return spriteSourceSize;
    }

    public Size getSourceSize() {
        return sourceSize;
    }

    private ResourceData() {

    }

    public static class ResourceBuilder {
        private String fileName;
        private ImageBox frame;
        private Boolean rotated;
        private Boolean trimmed;
        private ImageBox spriteSourceSize;
        private Size sourceSize;

        public ResourceBuilder(String fileName, ImageBox frame, Size sourceSize) {
            this.fileName = fileName;
            this.frame = frame;
            this.sourceSize = sourceSize;
        }

        public ResourceBuilder isRotated(Boolean isRotated) {
            this.rotated = isRotated;
            return this;
        }

        public ResourceBuilder isTrimmed(Boolean isTrimmed) {
            this.trimmed = isTrimmed;
            return this;
        }

        public ResourceBuilder withSpriteSourceSize(ImageBox spriteSourceSize) {
            this.spriteSourceSize = spriteSourceSize;
            return this;
        }

        public ResourceData build() {
            final ResourceData data = new ResourceData();
            data.fileName = this.fileName;
            data.frame = this.frame;
            data.rotated = this.rotated;
            data.trimmed = this.trimmed;
            data.sourceSize = this.sourceSize;
            data.spriteSourceSize = this.spriteSourceSize;
            return data;
        }
    }
}

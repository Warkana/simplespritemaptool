package com.vptech.spritemaptool.images;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import javax.imageio.ImageIO;

import com.google.gson.Gson;
import com.vptech.spritemaptool.descriptor.model.ImageBox;
import com.vptech.spritemaptool.descriptor.model.ResourceData;
import com.vptech.spritemaptool.descriptor.model.Size;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.collection.Seq;
import io.vavr.control.Option;

public class ImageEngine {

    public static Seq<ResourceData> concatImages(Seq<String> imagePaths) {
        final Map<String, BufferedImage> images = imagePaths.toMap(ImageEngine::readImage);
        final long area = images.map(im -> im._2.getHeight() * im._2.getWidth()).sum().longValue();
        final int maxWidth = 350;
        final int maxHeight = 400;

        final Seq<ResourceData> imageDescriptorList = getImageDescriptorByMaxRect(images, maxWidth, maxHeight);
        final BufferedImage concatImage = new BufferedImage(maxWidth, maxHeight, BufferedImage.TYPE_4BYTE_ABGR);
        final Graphics2D g2d = concatImage.createGraphics();

        imageDescriptorList.forEach(imData -> drawImage(imData, g2d, images));
        System.out.println(new Gson().toJson(imageDescriptorList.toJavaList()));

        g2d.dispose();
        try {
            ImageIO.write(concatImage, "png", new File("./concat.png"));
            return imageDescriptorList;
        } catch (IOException e) {
            throw new RuntimeException("Can't save spritemap", e);
        }
    }

    private static void drawImage(ResourceData imageData, Graphics2D g2d, Map<String, BufferedImage> images) {
        final BufferedImage image = images.get(imageData.getFileName())
                                          .getOrElseThrow(() -> new RuntimeException("Can't find fileName: " + imageData.getFileName()));

        g2d.drawImage(image, imageData.getFrame().getX(), imageData.getFrame().getY(), null);
    }

    private static List<ResourceData> getImageDescriptorByMaxRect(Map<String, BufferedImage> images, int maxWidth, int maxHeight) {
        final ImageBox firstEmptyBox = new ImageBox.ImageSpecBuilder().withX(0)
                                                                      .withY(0)
                                                                      .withWidth(maxWidth)
                                                                      .withHeight(maxHeight)
                                                                      .build();

        return maxRectPack(images, List.of(firstEmptyBox), maxWidth, maxHeight);
    }

    private static List<ResourceData> maxRectPack(Map<String, BufferedImage> images, Seq<ImageBox> emptyRectangles, int sheetWidth, int sheetHeight) {
        if (images.isEmpty()) {
            return List.empty();
        }
        final BufferedImage image = images.head()._2();

        final ImageBox emptyRectangle = emptyRectangles.find(er -> matchRect(image, er.getW(), er.getH()))
                                                       .getOrElseThrow(() -> new RuntimeException("Can't find empty rectangle for: " + image.getHeight() + ";" + image.getWidth() + ";" + images.head()._1()));
        final int x = emptyRectangle.getX();
        final int y = emptyRectangle.getY() + emptyRectangle.getH() - image.getHeight();
        final ResourceData imageData = buildResourceData(x, y, images.head());
        final ImageBox imageFrame = imageData.getFrame();

        final Seq<ImageBox> intersectionList = emptyRectangles.filter(emRect -> intersectWithImage(emRect, imageFrame));

        final Seq<ImageBox> emptyRectanglesToAdd = intersectionList.filter(emRect -> intersectWithImage(emRect, imageFrame))
                                                                   .flatMap(emRect -> divideEmptyByImage(emRect, imageFrame))
                                                                   .filter(emRect -> emptyRectMatchSheet(emRect, sheetWidth, sheetHeight));

        final Seq<ImageBox> updatedEmptyRectangles = emptyRectangles.removeAll(intersectionList)
                                                                    .appendAll(emptyRectanglesToAdd);

        final Seq<ImageBox> emptyRectanglesWithRemovedSubsets = updatedEmptyRectangles.removeAll(emRect -> haveSuperset(emRect, updatedEmptyRectangles));

        return maxRectPack(images.remove(images.head()._1), emptyRectanglesWithRemovedSubsets, sheetWidth, sheetHeight).append(imageData);
    }

    private static boolean haveSuperset(ImageBox subset, Seq<ImageBox> allEmptyRectangles) {
        return allEmptyRectangles.remove(subset)
                                 .exists(superSet -> isSubset(subset, superSet));
    }

    private static List<ImageBox> divideEmptyByImage(ImageBox emptyRectangle, ImageBox image) {
        return List.of(getLeftEmptyRect(emptyRectangle, image),
                       getRightEmptyRect(emptyRectangle, image),
                       getTopEmptyRect(emptyRectangle, image),
                       getBottomEmptyRect(emptyRectangle, image))
                   .filter(Option::isDefined)
                   .map(op -> op.getOrElseThrow(() -> new RuntimeException("Define exception")));
    }

    private static Option<ImageBox> getLeftEmptyRect(ImageBox emptyRect, ImageBox rect) {
        return isLeftRectEdgeOnEmpty(emptyRect, rect) ? Option.none()
                                                      : Option.of(new ImageBox.ImageSpecBuilder().withX(emptyRect.getX())
                                                                                                 .withY(emptyRect.getY())
                                                                                                 .withWidth(rect.getX() - emptyRect.getX())
                                                                                                 .withHeight(emptyRect.getH())
                                                                                                 .build());
    }

    private static Option<ImageBox> getRightEmptyRect(ImageBox emptyRect, ImageBox rect) {
        return isRightEdgeOnEmpty(emptyRect, rect) ? Option.none()
                                                   : Option.of(new ImageBox.ImageSpecBuilder().withX(rect.getX() + rect.getW())
                                                                                              .withY(emptyRect.getY())
                                                                                              .withWidth(emptyRect.getW() - (rect.getX() + rect.getW() - emptyRect.getX()))
                                                                                              .withHeight(emptyRect.getH())
                                                                                              .build());
    }

    private static Option<ImageBox> getTopEmptyRect(ImageBox emptyRect, ImageBox rect) {
        return isTopRectEdgeOnEmpty(emptyRect, rect) ? Option.none()
                                                     : Option.of(new ImageBox.ImageSpecBuilder().withX(emptyRect.getX())
                                                                                                .withY(emptyRect.getY())
                                                                                                .withWidth(emptyRect.getW())
                                                                                                .withHeight(rect.getY() - emptyRect.getY())
                                                                                                .build());
    }

    private static Option<ImageBox> getBottomEmptyRect(ImageBox emptyRect, ImageBox rect) {
        return isBottomEdgeOnEmpty(emptyRect, rect) ? Option.none()
                                                    : Option.of(new ImageBox.ImageSpecBuilder().withX(emptyRect.getX())
                                                                                               .withY(rect.getY() + rect.getH())
                                                                                               .withWidth(emptyRect.getW())
                                                                                               .withHeight(emptyRect.getY() + emptyRect.getH() - (rect.getY() + rect.getH()))
                                                                                               .build());
    }

    private static boolean isLeftRectEdgeOnEmpty(ImageBox emptyRect, ImageBox rect) {
        return rect.getX() <= emptyRect.getX();
    }

    private static boolean isTopRectEdgeOnEmpty(ImageBox emptyRect, ImageBox rect) {
        return rect.getY() <= emptyRect.getY();
    }

    private static boolean isBottomEdgeOnEmpty(ImageBox emptyRect, ImageBox rect) {
        return rect.getY() + rect.getH() >= emptyRect.getY() + emptyRect.getH();
    }

    private static boolean isRightEdgeOnEmpty(ImageBox emptyRect, ImageBox rect) {
        return rect.getX() + rect.getW() >= emptyRect.getX() + emptyRect.getW();
    }

    private static boolean intersectWithImage(ImageBox emptyRectangle, ImageBox image) {
        return image.getY() < emptyRectangle.getY() + emptyRectangle.getH()
               && image.getX() < emptyRectangle.getX() + emptyRectangle.getW()
               && image.getY() + image.getH() > emptyRectangle.getY()
               && image.getX() + image.getW() > emptyRectangle.getX();
    }

    private static boolean matchRect(BufferedImage image, int w, int h) {
        return image.getWidth() < w
               && image.getHeight() < h;
    }

    private static boolean emptyRectMatchSheet(ImageBox emptyRect, int sheetWidth, int sheetHeight) {
        return emptyRect.getX() + emptyRect.getW() <= sheetWidth
               && emptyRect.getY() + emptyRect.getH() <= sheetHeight;
    }

    private static boolean isSubset(ImageBox emptyRectSubset, ImageBox emptyRectSuperset) {
        return emptyRectSubset.getX() >= emptyRectSuperset.getX()
               && emptyRectSubset.getY() >= emptyRectSuperset.getY()
               && emptyRectSubset.getX() + emptyRectSubset.getW() <= emptyRectSuperset.getX() + emptyRectSuperset.getW()
               && emptyRectSubset.getY() + emptyRectSubset.getH() <= emptyRectSuperset.getY() + emptyRectSuperset.getH();
    }

    private static ResourceData buildResourceData(int x, int y, Tuple2<String, BufferedImage> image) {
        final int width = image._2.getWidth();
        final int height = image._2.getHeight();
        final ImageBox imageBox = new ImageBox.ImageSpecBuilder().withX(x)
                                                                 .withY(y)
                                                                 .withWidth(width)
                                                                 .withHeight(height)
                                                                 .build();

        final Size size = new Size(width, height);
        return new ResourceData.ResourceBuilder(image._1, imageBox, size).build();
    }

    private static Tuple2<String, BufferedImage> readImage(String path) {
        try {
            final File imageFile = new File(path);
            return Tuple.of(imageFile.getName(), ImageIO.read(imageFile));
        } catch (IOException e) {
            throw new RuntimeException("Can't read image: " + path, e);
        }

    }
}

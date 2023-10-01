package org.multithread;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Main {
    public static final String SOURCE_FILE = "src/main/resources/purple-flower.jpg";
    public static final String DESTINATION_FILE = "./out/yellow-flower.jpg";
    public static void main(String[] args) throws IOException {
        BufferedImage originalImage = ImageIO.read(new File(SOURCE_FILE));
        BufferedImage resultImage = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), BufferedImage.TYPE_INT_RGB);
        int numberOfThreads = 8;
        long startTime = System.currentTimeMillis();
        recolorMultithreaded(originalImage, resultImage, numberOfThreads);
        //recolorSingle(originalImage, resultImage);
        long endTime = System.currentTimeMillis();

        File outputFile = new File(DESTINATION_FILE);
        ImageIO.write(resultImage, "jpg", outputFile);

        System.out.println(endTime - startTime);
    }

    public static int getRed(int rgb) {
        return (rgb & 0x00ff0000) >> 16;
    }

    public static int getGreen(int rgb) {
        return (rgb & 0x0000ff00) >> 8;
    }

    public static int getBlue(int rgb) {
        return rgb & 0x000000ff;
    }

    public static int createRgb(int red, int green, int blue) {
        int rgb = 0;
        rgb |= blue;
        rgb |= green << 8;
        rgb |= red << 16;
        //set alpha channel 1111 1111 to make opaque
        rgb |= 0xff000000;
        return rgb;
    }

    public static void recolorImage(BufferedImage originalImage, BufferedImage resultImage, int leftCorner, int topCorner, int width, int height) {
        for(int x = leftCorner; x < leftCorner + width && x < originalImage.getWidth(); x++) {
            for(int y = topCorner; y < topCorner + height && y < originalImage.getHeight(); y++) {
                recolorPixel(originalImage, resultImage, x, y);
            }
        }
    }

    public static void recolorPixel(BufferedImage originalImage, BufferedImage resultImage, int leftCorner, int topCorner) {
        int rgb = originalImage.getRGB(leftCorner, topCorner);
        int red = getRed(rgb);
        int green = getGreen(rgb);
        int blue = getBlue(rgb);

        int newRed;
        int newGreen;
        int newBlue;

        if(isShadeOfPurple(red, green, blue)) {
            newRed = Math.min(255, red + 40);
            newGreen = Math.max(0, green + 50);
            newBlue = Math.max(0, blue - 150);
        } else {
            newRed = red;
            newGreen = green;
            newBlue = blue;
        }

        int newRgb = createRgb(newRed, newGreen, newBlue);
        setRgb(resultImage, leftCorner, topCorner, newRgb);
    }

    public static void setRgb(BufferedImage image, int leftCorner, int topCorner, int rgb) {
        image.getRaster().setDataElements(leftCorner, topCorner, image.getColorModel().getDataElements(rgb, null));
    }

    public static boolean isShadeOfPurple(int red, int green, int blue) {
        int rg = Math.abs(red - green);
        int rb = Math.abs(red - blue);
        int gb = Math.abs(green - blue);
        return rg < 80 && rb < 90 && rb > 20 && gb < 240 && gb > 15;
    }

    private static void recolorMultithreaded(BufferedImage originalImage, BufferedImage resultImage, int numberOfThreads) {
        int width = originalImage.getWidth();
        int height = originalImage.getHeight() / numberOfThreads;
        Thread[] threads = new Thread[numberOfThreads];
        for(int i = 0; i < numberOfThreads; i++) {
            final int threadMultiplier = i;
            threads[i] = new Thread(() -> {
                int leftCorner = 0;
                int topCorner = height * threadMultiplier;
                recolorImage(originalImage, resultImage, leftCorner, topCorner, width, height);
            });
        }
        for(Thread thread : threads) {
            thread.start();
        }
        for(Thread thread : threads) {
            try {
                thread.join();
            } catch(InterruptedException e) {
            }
        }
    }

    private static void recolorSingle(BufferedImage originalImage, BufferedImage resultImage) {
        recolorImage(originalImage, resultImage, 0, 0, originalImage.getWidth(), originalImage.getHeight());
    }
}
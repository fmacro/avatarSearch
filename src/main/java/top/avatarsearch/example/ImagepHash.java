package top.avatarsearch.example;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.io.InputStream;

/**
 * 图片PHash
 */
public class ImagepHash {
    private int size = 32;
    private int smallerSize = 9;

    /**
     * 图片PHash
     */
    public ImagepHash() {
        initCoefficients();
    }

    /**
     * 图片PHash
     */
    public ImagepHash(int size, int smallerSize) {
        this.size = size;
        this.smallerSize = smallerSize;

        initCoefficients();
    }

    /**
     * 计算距离
     * @param s1
     * @param s2
     * @return
     */
    public float distance(String s1, String s2) {
        int counter = 0;
        for (int k = 0; k < s1.length(); k++) {
            if (s1.charAt(k) != s2.charAt(k)) {
                counter++;
            }
        }
        return (float)(s1.length()-counter)/(s1.length())*100;
    }

    /**
     * 返回图片二进制流的字符串
     * @param is 输入流
     * @return
     * @throws Exception
     */
    public String getHash(InputStream is) throws Exception {
        BufferedImage img = ImageIO.read(is);

        /*
         * 简化图片尺寸
         */
        img = resize(img, size, size);

        /*
         *  减少图片颜色
         */
        img = grayscale(img);

        double[][] vals = new double[size][size];

        for (int x = 0; x < img.getWidth(); x++) {
            for (int y = 0; y < img.getHeight(); y++) {
                vals[x][y] = getBlue(img, x, y);
            }
        }

        /*
         * 计算DTC 采用32*32尺寸
         */
        long start = System.currentTimeMillis();
        double[][] dctVals = applyDCT(vals);
//        System.out.println("DCT: " + (System.currentTimeMillis() - start));

    
        /*
         * 计算平均值DTC
         */
        double total = 0;

        for (int x = 0; x < smallerSize; x++) {
            for (int y = 0; y < smallerSize; y++) {
                total += dctVals[x][y];
            }
        }
        total -= dctVals[0][0];

        double avg = total / (double) ((smallerSize * smallerSize) - 1);

        /*
         * 计算hash值
         */
        String hash = "";

        for (int x = 0; x < smallerSize; x++) {
            for (int y = 0; y < smallerSize; y++) {
                if (x != 0 && y != 0) {
                    hash += (dctVals[x][y] > avg ? "1" : "0");
                }
            }
        }

        return hash;
    }

    /**
     * 重置大小
     * @param image
     * @param width
     * @param height
     * @return
     */
    private BufferedImage resize(BufferedImage image, int width, int height) {
        BufferedImage resizedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = resizedImage.createGraphics();
        g.drawImage(image, 0, 0, width, height, null);
        g.dispose();
        return resizedImage;
    }

    /**
     *
     */
    private ColorConvertOp colorConvert = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null);

    /**
     * 设置灰度
     * @param img
     * @return
     */
    private BufferedImage grayscale(BufferedImage img) {
        colorConvert.filter(img, img);
        return img;
    }

    /**
     *
     * @param img
     * @param x
     * @param y
     * @return
     */
    private static int getBlue(BufferedImage img, int x, int y) {
        return (img.getRGB(x, y)) & 0xff;
    }


    private double[] c;

    /**
     *
     */
    private void initCoefficients() {
        c = new double[size];

        for (int i = 1; i < size; i++) {
            c[i] = 1;
        }
        c[0] = 1 / Math.sqrt(2.0);
    }

    /**
     * 获得DCT
     * @param val
     * @return
     */
    private double[][] applyDCT(double[][] val) {
        int n = size;

        double[][] f = new double[n][n];
        for (int u = 0; u < n; u++) {
            for (int v = 0; v < n; v++) {
                double sum = 0.0;
                for (int i = 0; i < n; i++) {
                    for (int j = 0; j < n; j++) {
                        sum += Math.cos(((2 * i + 1) / (2.0 * n)) * u * Math.PI)
                                * Math.cos(((2 * j + 1) / (2.0 * n)) * v * Math.PI) * (val[i][j]);
                    }
                }
                sum *= ((c[u] * c[v]) / 4.0);
                f[u][v] = sum;
            }
        }
        return f;
    }
}
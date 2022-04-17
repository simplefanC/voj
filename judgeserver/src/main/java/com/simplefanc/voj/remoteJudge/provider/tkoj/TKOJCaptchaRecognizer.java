package com.simplefanc.voj.remoteJudge.provider.tkoj;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TKOJCaptchaRecognizer {
    private static String[][] digitals = new String[][]{
            {
                    "..#####...",
                    ".#######..",
                    "#########.",
                    "###...###.",
                    "###...###.",
                    "###...###.",
                    "###....##.",
                    "###.##.##.",
                    "###.##.##.",
                    "###...###.",
                    "###...###.",
                    "###...###.",
                    "#########.",
                    ".#######..",
                    "..#####..."
            },
            {
                    "..#####...",
                    "..#####...",
                    "..#####...",
                    ".....##...",
                    ".....##...",
                    ".....##...",
                    ".....##...",
                    ".....##...",
                    ".....##...",
                    ".....##...",
                    ".....##...",
                    ".....##...",
                    ".....##...",
                    "..########",
                    "..########"
            },
            {
                    "########..",
                    "#########.",
                    "###..####.",
                    "......###.",
                    ".......###",
                    "......####",
                    "......###.",
                    ".....####.",
                    "....####..",
                    "...####...",
                    "..####....",
                    ".####.....",
                    "####......",
                    "##########",
                    "##########"
            },
            {
                    "########..",
                    "#########.",
                    "###..####.",
                    "......###.",
                    "......###.",
                    ".....####.",
                    "...#####..",
                    "...#####..",
                    ".....####.",
                    "......###.",
                    ".......##.",
                    "......###.",
                    "###.#####.",
                    "#########.",
                    "########.."
            },
            {
                    ".....###..",
                    "....####..",
                    "....####..",
                    "...#####..",
                    "...##.##..",
                    "..###.##..",
                    ".###..##..",
                    ".###..##..",
                    "###...##..",
                    "###...##..",
                    "##########",
                    "##########",
                    "......##..",
                    "......##..",
                    "......##.."
            },
            {
                    ".#######..",
                    ".#######..",
                    ".##.......",
                    ".##.......",
                    ".##.......",
                    ".#######..",
                    ".#######..",
                    ".##.#####.",
                    "......###.",
                    "......###.",
                    "......###.",
                    "......###.",
                    "###.#####.",
                    "########..",
                    "#######..."
            },
            {
                    "..######..",
                    ".#######..",
                    ".####.##..",
                    "####......",
                    "###.......",
                    "###.......",
                    "########..",
                    "#########.",
                    "####.####.",
                    "###...###.",
                    "##.....##.",
                    "###...###.",
                    "####.####.",
                    ".########.",
                    "..######.."
            },
            {
                    "#########.",
                    "#########.",
                    "......###.",
                    "......###.",
                    ".....###..",
                    ".....###..",
                    "....####..",
                    "....###...",
                    "....###...",
                    "...###....",
                    "...###....",
                    "...###....",
                    "..###.....",
                    "..###.....",
                    "..###....."
            },
            {
                    ".#######..",
                    "#########.",
                    "####.####.",
                    "###...###.",
                    "###...###.",
                    "####.####.",
                    ".#######..",
                    ".#######..",
                    "####.####.",
                    "###...###.",
                    "##.....##.",
                    "###...###.",
                    "####.####.",
                    "#########.",
                    ".#######.."
            },
            {
                    ".######...",
                    "########..",
                    "####.####.",
                    "###...###.",
                    "##.....##.",
                    "###...###.",
                    "####.####.",
                    "#########.",
                    ".########.",
                    "......###.",
                    "......###.",
                    ".....####.",
                    ".##.####..",
                    ".#######..",
                    ".######..."
            }
    };

    /**
     * @param image 需要被分割的验证码
     * @return
     */
    private static List<BufferedImage> splitImage(BufferedImage image) {
        List<BufferedImage> subImgs = new ArrayList<BufferedImage>();
        subImgs.add(image.getSubimage(5, 5, 10, 15));
        subImgs.add(image.getSubimage(17, 5, 10, 15));
        subImgs.add(image.getSubimage(29, 5, 10, 15));
        subImgs.add(image.getSubimage(41, 5, 10, 15));
        subImgs.add(image.getSubimage(53, 5, 5, 15));
        return subImgs;
    }

    /**
     * @param rgb 像素点RGB
     * @return 像素点灰度
     */
    private static int calGray(int rgb) {
        int argb = 0xff000000 | rgb;
        int r = (int) (((argb >> 16) & 0xFF));
        int g = (int) (((argb >> 8) & 0xFF));
        int b = (int) (((argb >> 0) & 0xFF));
        return r + g + b;
    }

    /**
     * @param image 目标图片
     * @return 灰度集
     */
    private static Set<Integer> getGraySet(BufferedImage image) {
        int h = image.getHeight();
        int w = image.getWidth();
        // 灰度统计
        Set<Integer> hashSet = new HashSet<Integer>();
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                hashSet.add(calGray(image.getRGB(x, y)));
            }
        }
        return hashSet;
    }

    /**
     * @param image   需要打印的图像
     * @param graySet 背景灰度集
     */
    private static void printImage(BufferedImage image, Set<Integer> graySet) {
        int h = image.getHeight();
        int w = image.getWidth();

        // 矩阵打印
        for (int y = 0; y < h; y++) {
            System.out.printf("\"");
            for (int x = 0; x < w; x++) {
                if (graySet.contains(calGray(image.getRGB(x, y)))) {
                    System.out.print(".");
                } else {
                    System.out.print("#");
                }
            }
            System.out.printf("%s", y == h - 1 ? "\"" : "\",");
            System.out.println();
        }
    }

    /**
     * @param image   待识别图片
     * @param graySet 背景灰度集
     * @return 符号
     */
    private static char recognizeSymbol(BufferedImage image, Set<Integer> graySet) {
        int h = image.getHeight();
        int w = image.getWidth();

        int minDiff = 999999;
        char symAns = 0;
        // 对于某个给定数值
        for (int i = 0; i < 10; i++) {
            int curDiff = 0;
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    boolean pixel1 = digitals[i][y].charAt(x) == '#';
                    boolean pixel2 = !graySet.contains(calGray(image.getRGB(x, y)));
                    if (pixel1 != pixel2) {
                        ++curDiff;
                    }
                }
            }
            if (curDiff < minDiff) {
                minDiff = curDiff;
                symAns = (char) ('0' + i);
            }
            if (minDiff == 0) {
                return symAns;
            }
        }

        return symAns;
    }

    /**
     * @param image 待识别的验证码
     * @return
     */
    public static String recognize(BufferedImage image) {
        StringBuilder ans = new StringBuilder();
        List<BufferedImage> subImgs = splitImage(image);
        Set<Integer> graySet = getGraySet(subImgs.get(subImgs.size() - 1));

//        printImage(image, graySet);
        for (int i = 0; i < subImgs.size() - 1; i++) {
            // 根据特征色，依次识别子图片
            ans.append(recognizeSymbol(subImgs.get(i), graySet));
        }
        System.out.printf("recognize: %s\n", ans.toString());
        return ans.toString();
    }
}

package kr.merutilm.rff.io;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import kr.merutilm.rff.struct.HexColor;
import kr.merutilm.rff.struct.IntMatrix;

import javax.imageio.ImageIO;

public class BitMap extends IntMatrix {

    

    public BitMap(int width, int height, HexColor[] canvas) {
        this(width, height, Arrays.stream(canvas).mapToInt(HexColor::toInteger).toArray());
    }


    public BitMap(int width, int height, int[] canvas) {
        super(width, height, canvas);
    }

    public BitMap(int width, int height){
        super(width, height);
    }

    
    public BufferedImage getImage(){

        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
        byte[] data = new byte[width * height * 4];
        
        for (int pixel = 0; pixel < data.length; pixel += 4) {
            int i = pixel / 4;
            int c = canvas[i];
            

            data[pixel + 3] = (byte) HexColor.intR(c);
            data[pixel + 2] = (byte) HexColor.intG(c);
            data[pixel + 1] = (byte) HexColor.intB(c);
            data[pixel] = (byte) HexColor.intA(c);
        }

        DataBufferByte buffer = new DataBufferByte(data, data.length);
        result.setData(Raster.createRaster(result.getSampleModel(), buffer, null));

        return result;
    }


    public static int[] redChannels(int[] pixelData) {
        return Arrays.stream(pixelData).map(HexColor::intR).toArray();
    }
    public static int[] greenChannels(int[] pixelData) {
        return Arrays.stream(pixelData).map(HexColor::intG).toArray();
    }
    public static int[] blueChannels(int[] pixelData) {
        return Arrays.stream(pixelData).map(HexColor::intB).toArray();
    }
    public void gaussianBlur(int range){
        gaussianBlur(canvas, width, range);
    }

    public int pipetteAdvanced(double x, double y) {

        double px = x % 1;
        double py = y % 1;
        int x1 = (int) x;
        int y1 = (int) y;
        int x2 = (int) x + 1;
        int y2 = (int) y + 1;

        int c1 = pipette(x1, y1);
        int c2 = pipette(x2, y1);
        int c3 = pipette(x1, y2);
        int c4 = pipette(x2, y2);

        int cc1 = HexColor.ratioDivide(c1, c2, px);
        int cc2 = HexColor.ratioDivide(c3, c4, px);

        return HexColor.ratioDivide(cc1, cc2, py);
    }

    public static void gaussianBlur(int[] target, int width, int range) {
        int[] bxs = boxesForGauss(range);
        int[] result = new int[target.length];
        int height = target.length / width;

        int[] r = redChannels(target);
        int[] g = greenChannels(target);
        int[] b = blueChannels(target);

        int[] rr = new int[target.length];
        int[] rg = new int[target.length];
        int[] rb = new int[target.length];


        boxBlur(r, rr, width, height, (bxs[0] - 1) / 2);
        boxBlur(rr, r, width, height, (bxs[1] - 1) / 2);
        boxBlur(r, rr, width, height, (bxs[2] - 1) / 2);

        boxBlur(g, rg, width, height, (bxs[0] - 1) / 2);
        boxBlur(rg, g, width, height, (bxs[1] - 1) / 2);
        boxBlur(g, rg, width, height, (bxs[2] - 1) / 2);

        boxBlur(b, rb, width, height, (bxs[0] - 1) / 2);
        boxBlur(rb, b, width, height, (bxs[1] - 1) / 2);
        boxBlur(b, rb, width, height, (bxs[2] - 1) / 2);

        for (int i = 0; i < target.length; i++) {
            result[i] = HexColor.toInteger(rr[i], rg[i], rb[i]);
        }
        System.arraycopy(result, 0, target, 0, target.length);
    }


    private static void boxBlur(int[] target, int[] result, int w, int h, int r) {
        int[] arr2 = new int[target.length];
        boxBlurH(target, arr2, w, h, r);
        boxBlurT(arr2, result, w, h, r);
    }

    private static void boxBlurH(int[] target, int[] result, int w, int h, int r) {
        float irr = 1f / (r + r + 1);
        for (int i = 0; i < h; i++) {
            int ti = i * w;
            int li = ti;
            int ri = ti + r;
            int fv = target[ti];
            int lv = target[ti + w - 1];
            int val = (r + 1) * fv;
            for (int j = 0; j < r; j++) val += target[ti + j];
            for (int j = 0; j <= r; j++) {
                val += target[Math.min(target.length - 1, ri++)] - fv;
                result[Math.min(target.length - 1, ti++)] = Math.round(val * irr);
            }
            for (int j = r + 1; j < w - r; j++) {
                val += target[Math.min(target.length - 1, ri++)] - target[li++];
                result[Math.min(target.length - 1, ti++)] = Math.round(val * irr);
            }
            for (int j = w - r; j < w; j++) {
                val += lv - target[li++];
                result[Math.min(target.length - 1, ti++)] = Math.round(val * irr);
            }
        }
    }

    private static void boxBlurT(int[] target, int[] result, int w, int h, int r) {
        float irr = 1f / (r + r + 1);
        for (int i = 0; i < w; i++) {
            int ti = i;
            int li = ti;
            int ri = ti + r * w;
            int fv = target[ti];
            int lv = target[ti + w * (h - 1)];
            int val = (r + 1) * fv;
            for (int j = 0; j < r; j++) val += target[ti + j * w];
            for (int j = 0; j <= r; j++) {
                val += target[Math.min(target.length - 1, ri)] - fv;
                result[Math.min(target.length - 1, ti)] = Math.round(val * irr);
                ri += w;
                ti += w;
            }
            for (int j = r + 1; j < h - r; j++) {
                val += target[Math.min(target.length - 1, ri)] - target[li];
                result[Math.min(target.length - 1, ti)] = Math.round(val * irr);
                li += w;
                ri += w;
                ti += w;
            }
            for (int j = h - r; j < h; j++) {
                val += lv - target[li];
                result[Math.min(target.length - 1, ti)] = Math.round(val * irr);
                li += w;
                ti += w;
            }
        }
    }

    private static int[] boxesForGauss(double sigma)  // standard deviation, number of boxes
    {
        double wIdeal = Math.sqrt((4 * sigma * sigma) + 1);  // Ideal averaging filter width
        int wl = (int) Math.floor(wIdeal);
        if (wl % 2 == 0) {
            wl--;
        }
        int wu = wl + 2;

        double mIdeal = (12 * sigma * sigma - 3 * wl * wl - 12 * wl - 9) / (-4 * wl - 4);
        int m = (int) Math.round(mIdeal);

        int[] sizes = new int[3];
        for (int i = 0; i < 3; i++) sizes[i] = (i < m ? wl : wu);
        return sizes;
    }

    public static void export(BufferedImage image, File file) throws IOException {
        ImageIO.write(image, "png", file);
    }



    public static void highGraphics(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
        g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
    }


    @Override
    public BitMap cloneCanvas() {
        return new BitMap(width, height, captureCurrentCanvas());
    }

    @Override
    public BitMap createAnother(int[] another) {
        return new BitMap(width, height, another);
    }

}

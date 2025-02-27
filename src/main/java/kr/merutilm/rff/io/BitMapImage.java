package kr.merutilm.rff.io;

import javax.annotation.Nonnull;
import javax.imageio.ImageIO;

import kr.merutilm.rff.parallel.IllegalParallelRenderStateException;
import kr.merutilm.rff.parallel.ParallelBitMapDispatcher;
import kr.merutilm.rff.parallel.ParallelBitMapRenderer;
import kr.merutilm.rff.parallel.ParallelRenderState;
import kr.merutilm.rff.struct.HexColor;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.*;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class BitMapImage {
    
    private BitMap bitMap;
    /**
     * 이미지
     */
    private BufferedImage image;
    /**
     * image 값에 따라 픽셀값 수정 여부
     */
    private boolean isRefreshed = false;

    /**
     * 크기 조정 시 최대 길이가 해당 값을 초과하면 사진 크기가 조정됩니다
     */
    private static final int MAX_SIZE = 4096;

    public BitMapImage(String filePath) throws IOException {
        this(ImageIO.read(new File(filePath)));
    }

    public BitMapImage(BufferedImage image) {
        this.image = image;
    }

    public void export(File file) throws IOException{
        ImageIO.write(image, "png", file);
    }

    private void refreshBitMap() {
        final byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        final int width = image.getWidth();
        final int height = image.getHeight();

        HexColor[] canvas = new HexColor[width * height];


        boolean alpha = image.getAlphaRaster() != null;
        int pixelLength = alpha ? 4 : 3;

        for (int pixel = 0; pixel + pixelLength <= pixels.length; pixel += pixelLength) {
            int i = pixel / pixelLength;
            int r = pixels[pixel + pixelLength - 1] & 0x00ff;
            int g = pixels[pixel + pixelLength - 2] & 0x00ff;
            int b = pixels[pixel + pixelLength - 3] & 0x00ff;
            int a = alpha ? pixels[pixel] & 0x00ff : 255;

            canvas[i] = HexColor.get(r, g, b, a);
            
        }
        this.bitMap = new BitMap(width, height, canvas);
        isRefreshed = true;
    }


    public int getWidth() {
        return image.getWidth();
    }

    public int getHeight() {
        return image.getHeight();
    }

    /**
     * 이미지의 픽셀 데이터를 얻습니다.
     */
    public BitMap getBitMap() {
        if (!isRefreshed) {
            refreshBitMap();
        }
        return bitMap;
    }
    public ParallelBitMapDispatcher createShader(ParallelBitMapRenderer... renderers) throws IllegalParallelRenderStateException{
        ParallelRenderState state = new ParallelRenderState();
        ParallelBitMapDispatcher dispatcher = new ParallelBitMapDispatcher(state, state.currentID(), getBitMap());
        for (ParallelBitMapRenderer renderer : renderers) {
            dispatcher.createRenderer(renderer);
        }
        return dispatcher;
    }
    /**
     * 이미지 얻기
     */
    public BufferedImage getImage() {
        return image;
    }





    /**
     * @return 빈 이미지 (투명)
     */
    public static BufferedImage emptyImage(int w, int h) {
        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_4BYTE_ABGR);
        image.createGraphics();
        return image;
    }
    /**
     * @return 빈 이미지 (투명)
     */
    public static BitMapImage emptyBImage(int w, int h) {
        BufferedImage image = emptyImage(w, h);
        return new BitMapImage(image);
    }

    public void rotate(double rotation) {
        isRefreshed = false;

        int cx = image.getWidth() / 2;
        int cy = image.getHeight() / 2;
        int scale = (int) Math.hypot(image.getWidth(), image.getHeight());

        BufferedImage result = emptyImage(scale, scale);
        Graphics2D g = (Graphics2D) result.getGraphics();
        highGraphics(g);
        g.rotate(Math.toRadians(rotation), scale / 2.0, scale / 2.0);
        g.drawImage(image, scale / 2 - cx, scale / 2 - cy, null);
        image = result;
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
    public static void lowGraphics(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        g.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DISABLE);
        g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED);
    }

    public void scale(double mx, double my) {
        isRefreshed = false;

        int w = (int) (image.getWidth() * mx);
        int h = (int) (image.getHeight() * my);
        double m = rescale(w, h);

        w = (int) (w * m);
        h = (int) (h * m);

        if (w == 0 || h == 0) {
            image = emptyImage(100, 100);
            return;
        }

        if (w < 0) {
            w = -w;
            flipX();
        }
        if (h < 0) {
            h = -h;
            flipY();
        }

        BufferedImage result = emptyImage(w, h);

        Graphics2D g = result.createGraphics();
        highGraphics(g);
        g.drawImage(image, 0, 0, w, h, null);

        image = result;
    }

    private static double rescale(int w, int h) {
        double m = 1;

        if (w == 0 || h == 0) {
            return 1;
        }
        if (w < 0) {
            w = -w;
        }
        if (h < 0) {
            h = -h;
        }
        if (w > MAX_SIZE) {
            m *= (double) MAX_SIZE / w;
            h = h * MAX_SIZE / w;
        }
        if (h > MAX_SIZE) {
            m *= (double) MAX_SIZE / h;
        }
        return m;
    }

    private void flipX() {
        AffineTransform tx = AffineTransform.getScaleInstance(-1, 1);
        tx.translate(-image.getWidth(null), 0);
        AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
        image = op.filter(image, null);
    }

    private void flipY() {
        AffineTransform tx = AffineTransform.getScaleInstance(1, -1);
        tx.translate(0, -image.getHeight(null));
        AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
        image = op.filter(image, null);
    }

    public void blackToAlpha() {
        image = blackToAlpha(bitMap).getImage();
    }

    public static BitMap blackToAlpha(BitMap bitMap) {
        int[] result = Arrays.stream(bitMap.getCanvas())
                .map(HexColor::toRGBA)
                .toArray();
        return bitMap.createAnother(result);
    }

    public static BufferedImage deepCopy(BufferedImage bi) {
        BufferedImage image = emptyImage(bi.getWidth(), bi.getHeight());
        image.setData(bi.getData());
        return image;
    }

    public static double accuracy(BufferedImage i1, BufferedImage i2) {
        final byte[] p1 = ((DataBufferByte) i1.getRaster().getDataBuffer()).getData();
        final byte[] p2 = ((DataBufferByte) i2.getRaster().getDataBuffer()).getData();
        if (i1.getAlphaRaster() == null || i2.getAlphaRaster() == null) {
            throw new IllegalArgumentException("hasn't Alpha");
        }
        if (p1.length != p2.length) {
            throw new IllegalArgumentException("size not match");
        }
        double err = compare(p1, p2);
        return 100 - err;
    }

    private static double compare(byte[] p1, byte[] p2) {
        int colors = p1.length / 4;
        double err = 0;

        for (int pixel = 0; pixel + 4 <= p1.length; pixel += 4) {
            double m1 = (p1[pixel] & 0x00ff) / 255.0;
            double m2 = (p2[pixel] & 0x00ff) / 255.0;

            double dr = Math.abs((p2[pixel + 3] & 0x00ff) * m2 - (p1[pixel + 3] & 0x00ff) * m1);
            double dg = Math.abs((p2[pixel + 2] & 0x00ff) * m2 - (p1[pixel + 2] & 0x00ff) * m1);
            double db = Math.abs((p2[pixel + 1] & 0x00ff) * m2 - (p1[pixel + 1] & 0x00ff) * m1);
            err += (dr + dg + db) * 0.130719 / colors;
        }
        return err;
    }

    public static BufferedImage color(BufferedImage image, HexColor color) {

        final byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        if (image.getAlphaRaster() == null) {
            throw new IllegalArgumentException("hasn't Alpha");
        }

        DataBuffer buffer = getColoredData(color, pixels);

        BufferedImage result = emptyImage(image.getWidth(), image.getHeight());
        result.setData(Raster.createRaster(image.getSampleModel(), buffer, null));

        return result;
    }

    @Nonnull
    private static DataBuffer getColoredData(HexColor color, byte[] pixels) {
        DataBuffer buffer = new DataBufferByte(pixels.length);


        for (int pixel = 0; pixel + 4 <= pixels.length; pixel += 4) {
            int r = pixels[pixel + 3] & 0x00ff;
            int g = pixels[pixel + 2] & 0x00ff;
            int b = pixels[pixel + 1] & 0x00ff;
            int a = pixels[pixel] & 0x00ff;
            buffer.setElem(pixel + 3, (byte) (r * color.r() / 255));
            buffer.setElem(pixel + 2, (byte) (g * color.g() / 255));
            buffer.setElem(pixel + 1, (byte) (b * color.b() / 255));
            buffer.setElem(pixel, (byte) (a * color.a() / 255));
        }
        return buffer;
    }

    public static void checkDivision(int length, int width) {
        if (length % width != 0) {
            throw new IllegalArgumentException("Indivisible Array Length");
        }
    }
}

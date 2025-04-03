package kr.merutilm.rff.io;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
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

    
    public static BufferedImage getImage(int width, int height, ByteBuffer buffer){

        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
        byte[] data = new byte[width * height * 4];
        
        for (int pixel = 0; pixel < data.length; pixel += 4) {
            data[pixel + 3] = buffer.get(); //r
            data[pixel + 2] = buffer.get(); //g
            data[pixel + 1] = buffer.get(); //b
            data[pixel] = buffer.get(); //a
        }

        DataBufferByte imageBuffer = new DataBufferByte(data, data.length);
        result.setData(Raster.createRaster(result.getSampleModel(), imageBuffer, null));
        AffineTransform tx = new AffineTransform();
        tx.scale(1, -1);
        tx.translate(0, -height);

        AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_BILINEAR);
        return op.filter(result, null);
    }




    public static void export(BufferedImage image, File file) throws IOException {
        ImageIO.write(image, "png", file);
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

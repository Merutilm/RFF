package kr.merutilm.fractal.settings;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import kr.merutilm.base.struct.Struct;
import kr.merutilm.base.struct.StructBuilder;
import kr.merutilm.fractal.RFFUtils;
import kr.merutilm.fractal.io.IOManager;

public record DataSettings(double defaultZoomIncrement) implements Struct<DataSettings>{
    @Override
    public Builder edit() {
        return new Builder()
        .setDefaultZoomIncrement(defaultZoomIncrement);
    }

    public static final class Builder implements StructBuilder<DataSettings>{

        private double defaultZoomIncrement;

        public Builder setDefaultZoomIncrement(double defaultZoomIncrement) {
            this.defaultZoomIncrement = defaultZoomIncrement;
            return this;
        }
        

        @Override
        public DataSettings build() {
            return new DataSettings(defaultZoomIncrement);
        }
    }

    private static File generateFile(File dir){
        return new File(dir, RFFUtils.DefaultFileName.VIDEO_DATA_SETTINGS + "." + RFFUtils.Extension.VIDEO_DATA_SETTINGS);
    }

    public static DataSettings read(File dir){

        File file = generateFile(dir);
        if(file == null || !file.exists()){
            return null;
        }

        RFFUtils.checkInvalidExtension(file, RFFUtils.Extension.VIDEO_DATA_SETTINGS.toString());

        byte[] data;

        try(FileInputStream stream = new FileInputStream(file)) {

            data = stream.readNBytes(Double.BYTES);
            double defaultZoomIncrement = IOManager.byteArrayToDouble(data);
            
            return new DataSettings(defaultZoomIncrement);
        }catch (IOException e) {
            throw new IllegalStateException();
        }
    }
    public void export(File dir){
        File file = generateFile(dir);
        try(FileOutputStream stream = new FileOutputStream(file)) {
            
            stream.write(IOManager.doubleToByteArray(defaultZoomIncrement));
        } catch (IOException e) {
            throw new IllegalStateException();
        }
    }
}

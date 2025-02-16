package kr.merutilm.rff.settings;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import kr.merutilm.rff.struct.Struct;
import kr.merutilm.rff.struct.StructBuilder;
import kr.merutilm.rff.io.IOBinaryParser;
import kr.merutilm.rff.io.IOUtilities;

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
        return new File(dir, IOUtilities.DefaultFileName.VIDEO_DATA_SETTINGS + "." + IOUtilities.Extension.VIDEO_DATA_SETTINGS);
    }

    public static DataSettings read(File dir){

        File file = generateFile(dir);
        if(file == null || !file.exists()){
            return null;
        }

        IOUtilities.checkInvalidExtension(file, IOUtilities.Extension.VIDEO_DATA_SETTINGS.toString());

        byte[] data;

        try(FileInputStream stream = new FileInputStream(file)) {

            data = stream.readNBytes(Double.BYTES);
            double defaultZoomIncrement = IOBinaryParser.byteArrayToDouble(data);
            
            return new DataSettings(defaultZoomIncrement);
        }catch (IOException e) {
            throw new IllegalStateException();
        }
    }
}

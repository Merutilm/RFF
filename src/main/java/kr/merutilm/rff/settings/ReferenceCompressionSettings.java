package kr.merutilm.rff.settings;

import kr.merutilm.rff.struct.Struct;
import kr.merutilm.rff.struct.StructBuilder;

public record ReferenceCompressionSettings(
    int compressCriteria,
    int compressionThresholdPower
) implements Struct<ReferenceCompressionSettings> {

public static final double ZOOM_VALUE = 0.235;

public static final double MINIMUM_ZOOM = 1;

@Override
public Builder edit() {
    return new Builder()
            .setCompressCriteria(compressCriteria)
            .setCompressionThresholdPower(compressionThresholdPower);
}

public static final class Builder implements StructBuilder<ReferenceCompressionSettings> {

    private int compressCriteria;
    private int compressionThresholdPower;

    public Builder setCompressCriteria(int compressCriteria) {
        this.compressCriteria = compressCriteria;
        return this;
    }
    
    public Builder setCompressionThresholdPower(int compressionThresholdPower) {
        this.compressionThresholdPower = compressionThresholdPower;
        return this;
    }


    @Override
    public ReferenceCompressionSettings build() {
        return new ReferenceCompressionSettings(compressCriteria, compressionThresholdPower);
    }
}

}

package kr.merutilm.fractal.theme;

import kr.merutilm.fractal.settings.*;
import kr.merutilm.fractal.struct.LWBigComplex;

public interface BasicTheme extends Theme {

    double INIT_MULTIPLIER = 1;
    long INIT_ITERATION = 3000;
    double INIT_LOG_ZOOM = 2;
    double INIT_BAILOUT = 2;
    boolean INIT_AUTO_ITERATION = true;
    ReuseReferenceSettings INIT_REUSE_REFERENCE = ReuseReferenceSettings.DISABLED;
    BLASettings INIT_BLA = new BLASettings(
            -5,
            3
    );
    String INIT_RE = "-0.85";
    String INIT_IM = "0";
    LWBigComplex INIT_C = LWBigComplex.valueOf(INIT_RE, INIT_IM, -(int)INIT_LOG_ZOOM - 10);

    CalculationSettings DEFAULT_CALC = new CalculationSettings(
            INIT_LOG_ZOOM,
            INIT_ITERATION,
            INIT_BAILOUT,
            INIT_C,
            INIT_AUTO_ITERATION,
            INIT_REUSE_REFERENCE,
            INIT_BLA
    );

    @Override
    default Settings generate() {
        return new Settings(DEFAULT_CALC, new ImageSettings(INIT_MULTIPLIER, colorSettings(), slopeSettings(), colorFilterSettings(), fogSettings(), bloomSettings()));
    }


    String getName();

    ColorSettings colorSettings();

    SlopeSettings slopeSettings();

    ColorFilterSettings colorFilterSettings();

    FogSettings fogSettings();

    BloomSettings bloomSettings();
}

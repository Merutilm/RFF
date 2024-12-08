package kr.merutilm.fractal.theme;

import kr.merutilm.base.selectable.Ease;
import kr.merutilm.fractal.settings.*;
import kr.merutilm.fractal.struct.LWBigComplex;

public interface BasicTheme extends Theme {

    double INIT_MULTIPLIER = 1;
    long INIT_ITERATION = 3000;
    double INIT_LOG_ZOOM = 2;
    double INIT_BAILOUT = 2;
    DecimalIterationSettings INIT_DECIMAL_ITERATION = DecimalIterationSettings.LINEAR;
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
            INIT_DECIMAL_ITERATION,
            INIT_C,
            INIT_AUTO_ITERATION,
            INIT_REUSE_REFERENCE,
            INIT_BLA
    );

    VideoSettings DEFAULT_VID = new VideoSettings(
            new DataSettings(2),
            new AnimationSettings( Ease.IN_EXPONENTIAL, 1),
            new ExportSettings(30, 1, 2, 1, 5000));

    @Override
    default Settings generate() {
        return new Settings(
                DEFAULT_CALC,
                new ImageSettings(INIT_MULTIPLIER),
                new ShaderSettings(colorSettings(), stripeSettings(), slopeSettings(),
                        colorFilterSettings(), fogSettings(), bloomSettings()),
                DEFAULT_VID);
    }


    String getName();

    ColorSettings colorSettings();

    StripeSettings stripeSettings();

    SlopeSettings slopeSettings();

    ColorFilterSettings colorFilterSettings();

    FogSettings fogSettings();

    BloomSettings bloomSettings();
}

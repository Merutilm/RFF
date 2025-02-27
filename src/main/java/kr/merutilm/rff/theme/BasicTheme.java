package kr.merutilm.rff.theme;

import kr.merutilm.rff.selectable.Ease;
import kr.merutilm.rff.settings.*;
import kr.merutilm.rff.struct.LWBigComplex;

public interface BasicTheme extends Theme {

    double INIT_MULTIPLIER = 1;
    BasicThemes DEFAULT_THEME = BasicThemes.RANDOMIZED_RAINBOW;
    DecimalizeIterationMethod INIT_DECIMAL_ITERATION = DecimalizeIterationMethod.LOG_LOG;
    boolean INIT_AUTO_ITERATION = true;
    ReuseReferenceMethod INIT_REUSE_REFERENCE = ReuseReferenceMethod.DISABLED;
    R3ASettings INIT_R3A = new R3ASettings(
            16,
            4,
            -3.0,
            R3ASelectionMethod.HIGHEST,
            false
    );
    String INIT_RE = "-0.85";
    String INIT_IM = "0";
    double INIT_LOG_ZOOM = 2;

    LWBigComplex INIT_C = LWBigComplex.valueOf(INIT_RE, INIT_IM, -(int)INIT_LOG_ZOOM - 10);

    long INIT_ITERATION = 300;
    double INIT_BAILOUT = 2;
    int INIT_COMPRESS_CRITERIA = -1;
    int INIT_COMPRESS_THREASHOLD_POWER = -1;

    CalculationSettings DEFAULT_CALC = new CalculationSettings(
            INIT_LOG_ZOOM,
            INIT_ITERATION,
            INIT_BAILOUT,
            INIT_DECIMAL_ITERATION,
            INIT_C,
            INIT_AUTO_ITERATION,
            INIT_REUSE_REFERENCE,
            INIT_R3A,
            INIT_COMPRESS_CRITERIA,
            INIT_COMPRESS_THREASHOLD_POWER
    );

    VideoSettings DEFAULT_VID = new VideoSettings(
            new DataSettings(2),
            new AnimationSettings(2, true, 1, Ease.LINEAR, 1),
            new ExportSettings(30, 1, 5000));

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

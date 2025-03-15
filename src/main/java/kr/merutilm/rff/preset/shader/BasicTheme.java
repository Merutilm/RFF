package kr.merutilm.rff.preset.shader;

import kr.merutilm.rff.selectable.Ease;
import kr.merutilm.rff.settings.*;
import kr.merutilm.rff.struct.LWBigComplex;

public interface BasicTheme extends Shader {

    double INIT_MULTIPLIER = 1;
    BasicThemes DEFAULT_THEME = BasicThemes.LONG_RAINBOW_SHADED;
    DecimalizeIterationMethod INIT_DECIMAL_ITERATION = DecimalizeIterationMethod.LOG_LOG;
    boolean INIT_AUTO_ITERATION = true;
    boolean INIT_ABS_ITERATION = false;
    ReuseReferenceMethod INIT_REUSE_REFERENCE = ReuseReferenceMethod.DISABLED;
    R3ASettings INIT_R3A = new R3ASettings(
            16,
            4,
            -4.0,
            R3ASelectionMethod.HIGHEST,
            R3ACompressionMethod.STRONGEST
    );
    String INIT_RE = "-1.94048274925665765940041977472222909176519086459295222485599652383404819337168623886822635";
    String INIT_IM = "-0.00003404217091445309929957338110642027622069481234857037980194247495984288831945274955585";
    double INIT_LOG_ZOOM = 51;

    LWBigComplex INIT_C = LWBigComplex.valueOf(INIT_RE, INIT_IM, -(int)INIT_LOG_ZOOM - 10);

    long INIT_ITERATION = 30000;
    double INIT_BAILOUT = 2;
    int INIT_COMPRESS_CRITERIA = 1;
    int INIT_COMPRESS_THREASHOLD_POWER = 5;

    CalculationSettings DEFAULT_CALC = new CalculationSettings(
            INIT_C,
            INIT_LOG_ZOOM,
            INIT_ITERATION,
            INIT_BAILOUT,
            INIT_DECIMAL_ITERATION,
            INIT_R3A,
            new ReferenceCompressionSettings(INIT_COMPRESS_CRITERIA, INIT_COMPRESS_THREASHOLD_POWER),
            INIT_REUSE_REFERENCE,
            INIT_AUTO_ITERATION,
            INIT_ABS_ITERATION
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

package kr.merutilm.rff.theme;

import kr.merutilm.rff.selectable.Ease;
import kr.merutilm.rff.settings.*;
import kr.merutilm.rff.struct.LWBigComplex;

public interface BasicTheme extends Theme {

    double INIT_MULTIPLIER = 0.4;
    BasicThemes DEFAULT_THEME = BasicThemes.LONG_RAINBOW_SHADED;
    DecimalizeIterationMethod INIT_DECIMAL_ITERATION = DecimalizeIterationMethod.LOG_LOG;
    boolean INIT_AUTO_ITERATION = true;
    ReuseReferenceMethod INIT_REUSE_REFERENCE = ReuseReferenceMethod.DISABLED;
    R3ASettings INIT_R3A = new R3ASettings(
            16,
            4,
            -3.0,
            R3ASelectionMethod.HIGHEST,
            R3ACompressionMethod.STRONGEST
    );
    String INIT_RE = "-1.94049753314295569426879934099924017193708945395883825527159050727270475699799188638368721363557122837537433";
    String INIT_IM = "-0.00002167558003007276340571795702783203463377018180015617368527563874749808740635139031584401899283992486779";
    double INIT_LOG_ZOOM = 90.4645786285399;

    LWBigComplex INIT_C = LWBigComplex.valueOf(INIT_RE, INIT_IM, -(int)INIT_LOG_ZOOM - 10);

    long INIT_ITERATION = 3000000;
    double INIT_BAILOUT = 2;
    int INIT_COMPRESS_CRITERIA = 10000;
    int INIT_COMPRESS_THREASHOLD_POWER = 5;

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

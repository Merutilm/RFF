package kr.merutilm.fractal.locater;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.DoubleConsumer;

import kr.merutilm.base.exception.IllegalRenderStateException;
import kr.merutilm.base.parallel.RenderState;
import kr.merutilm.fractal.formula.DeepMandelbrotPerturbator;
import kr.merutilm.fractal.formula.MandelbrotPerturbator;
import kr.merutilm.fractal.formula.MandelbrotReference;
import kr.merutilm.fractal.formula.Perturbator;
import kr.merutilm.fractal.settings.CalculationSettings;
import kr.merutilm.fractal.struct.DoubleExponent;
import kr.merutilm.fractal.struct.LWBigComplex;
import kr.merutilm.fractal.util.DoubleExponentMath;

public record MandelbrotLocator(LWBigComplex center, DoubleExponent dcMax, double logZoom) implements Locator {

    private static final double MINIBROT_LOG_ZOOM_OFFSET = -1.5;
    private static final double ZOOM_INCREMENT_LIMIT = 0.05;

    public static LWBigComplex findCenter(MandelbrotPerturbator scene) {
        LWBigComplex center = scene.getReference().refCenter();
        int precision = Perturbator.precision(scene.getLogZoom());
        LWBigComplex dc = findCenterOffset(scene);
        DoubleExponent dcMax = scene.getDcMaxByDoubleExponent();

        if (DoubleExponent.valueOf(dc.radius2()).isLargerThan(dcMax.square())) {
            return null;
        }
        return center.add(dc, precision);
    }

    private static LWBigComplex findCenterOffset(MandelbrotPerturbator scene) {
        int precision = Perturbator.precision(scene.getLogZoom());
        MandelbrotReference reference = scene.getReference();
        LWBigComplex bn = reference.fpgBn();
        LWBigComplex z = reference.lastReference();
        return z.divide(bn.negate(), precision);
    }
    

    private static DeepMandelbrotPerturbator findAccurateCenterPerturbator(RenderState state, int currentID, MandelbrotPerturbator scene, BiConsumer<Integer, Integer> actionWhileFindingMinibrotCenter) throws IllegalRenderStateException{
        
        // multiply zoom by 2 and find center offset.
        // set the center to center + centerOffset.
        
        int period = scene.getReference().period();
        double zoom = scene.getLogZoom();
        CalculationSettings calc = scene.getCalc();
        long maxIteration = calc.maxIteration();
        int doubledZoomPrecision = Perturbator.precision(zoom * 2);
        CalculationSettings doubledZoomCalc = calc.edit().addCenter(findCenterOffset(scene), doubledZoomPrecision).setLogZoom(zoom * 2).build();
        DoubleExponent doubledZoomDcMax = scene.getDcMaxByDoubleExponent().divide(DoubleExponentMath.pow10(zoom));

        AtomicInteger centerFixCount = new AtomicInteger();
        DeepMandelbrotPerturbator doubledZoomScene = null;
        while (doubledZoomScene == null || !checkMaxIterationOnly(doubledZoomScene, doubledZoomDcMax, maxIteration)) {
            
            if(doubledZoomScene != null){
                LWBigComplex off = findCenterOffset(doubledZoomScene);
                doubledZoomCalc = doubledZoomCalc.edit().addCenter(off, doubledZoomPrecision).build();    
            }
            centerFixCount.getAndIncrement();
            doubledZoomScene = new DeepMandelbrotPerturbator(state, currentID, doubledZoomCalc, doubledZoomDcMax, doubledZoomPrecision, period, p -> actionWhileFindingMinibrotCenter.accept(p, centerFixCount.get()), true);
        }


        return doubledZoomScene;
    }
    
    public static MandelbrotLocator locateMinibrot(RenderState state, int currentID, MandelbrotPerturbator scene, BiConsumer<Integer, Integer> actionWhileFindingMinibrotCenter, DoubleConsumer actionWhileFindingMinibrotZoom) {

        // code flowing
        // e.g zoom * 2 -> zoom * 1.5 -> zoom * 1.75.....
        // it is not required referecnce calculations.
        // check 'dcMax' iterate and check its iteration is max iteration
        // if true, zoom out. otherwise, zoom in.
        // it can approximate zoom when repeats until zoom increment is lower than
        // specific small number. O(log N)

        try {
           
            MandelbrotPerturbator resultScene = findAccurateCenterPerturbator(state, currentID, scene, actionWhileFindingMinibrotCenter);
            DoubleExponent resultDcMax = resultScene.getDcMaxByDoubleExponent();
            CalculationSettings resultCalc = resultScene.getCalc();
            double resultZoom = resultCalc.logZoom();
            long maxIteration = resultCalc.maxIteration();
            double zoomIncrement = resultZoom / 4;
            MandelbrotPerturbator resultSceneTemp = resultScene;

            while (zoomIncrement > ZOOM_INCREMENT_LIMIT) {
                if(checkMaxIterationOnly(resultScene, resultDcMax, maxIteration)){
                    resultZoom -= zoomIncrement;
                    resultDcMax = resultDcMax.multiply(DoubleExponentMath.pow10(zoomIncrement));
                }else{
                    resultZoom += zoomIncrement;
                    resultDcMax = resultDcMax.divide(DoubleExponentMath.pow10(zoomIncrement));
                }

                actionWhileFindingMinibrotZoom.accept(resultZoom);
                
                resultCalc = resultCalc.edit().setLogZoom(resultZoom).build();
                resultScene = resultSceneTemp.reuse(state, currentID, resultCalc, resultDcMax, Perturbator.precision(resultZoom));
                zoomIncrement /= 2;
            }

            return new MandelbrotLocator(resultCalc.center(), resultDcMax, resultZoom + MINIBROT_LOG_ZOOM_OFFSET);
        } catch (IllegalRenderStateException e) {
            return null;
        }
    }

    private static boolean checkMaxIterationOnly(MandelbrotPerturbator scene, DoubleExponent resultDcMax, long maxIteration){
        return (long) scene.iterate(resultDcMax, DoubleExponent.ZERO) == maxIteration;
    }

}

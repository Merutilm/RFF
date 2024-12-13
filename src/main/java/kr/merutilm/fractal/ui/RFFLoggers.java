package kr.merutilm.fractal.ui;

import java.util.logging.Level;
import java.util.logging.Logger;

final class RFFLoggers {
    private RFFLoggers(){

    }

    public static void logCancelledMessage(String name, int id){
        Logger.getGlobal().log(Level.WARNING, "{0} Render Cancelled, (ID = {1})", new Object[]{name, id});
    }
}

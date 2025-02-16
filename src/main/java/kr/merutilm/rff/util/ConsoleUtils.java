package kr.merutilm.rff.util;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class ConsoleUtils {

    private ConsoleUtils(){

    }

    public static void logError(Throwable e){
        Logger.getGlobal().log(Level.WARNING, e, () -> "");
    }


}

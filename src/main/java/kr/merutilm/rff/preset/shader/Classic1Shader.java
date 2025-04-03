package kr.merutilm.rff.preset.shader;

import kr.merutilm.rff.settings.ColorSettings;
import kr.merutilm.rff.struct.HexColor;

public interface Classic1Shader extends Shader{


    @Override
    default ColorSettings colorSettings(){
        return initColorSettings();
    }
    static ColorSettings initColorSettings() {
        ColorSettings.Builder p = new ColorSettings.Builder();
        for (double i = 0; i < Math.PI * 2; i += Math.PI / 100) {

            double r = 127.5 + 127.5 * Math.sin(i - 2);
            double g = 127.5 + 127.5 * Math.sin(i - 1.3);
            double b = 127.5 + 127.5 * Math.sin(i - 0.6);
            HexColor c = new HexColor((int)r, (int)g, (int)b, 255);
            p.add(c);
        }
        p.setIterationInterval(250);
        return p.build();
    }
}

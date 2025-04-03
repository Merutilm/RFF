package kr.merutilm.rff.opengl;

import java.util.ArrayList;
import java.util.List;

public class GLMultiPassRenderer {
    private final List<GLRenderer> renderers;
    private float time;

    public GLMultiPassRenderer() {
        this.renderers = new ArrayList<>();
    }

    public void addRenderer(GLRenderer renderer) {
        this.renderers.add(renderer);
    }

    public void reloadSize(int w, int h){
        renderers.forEach(r -> r.reloadSize(w, h));
    }

    public void setTime(float time) {
        this.time = time;
    }

    public void update(){
        int iterationTextureID = 0;
        for (int i = 0; i < renderers.size(); i++) {
            GLRenderer renderer = renderers.get(i);
            if(renderer instanceof GLIterationTextureProvider p){
                iterationTextureID = p.getIterationTextureID();
            }
            if(renderer instanceof GLIterationTextureRenderer r){
                r.setIterationTextureID(iterationTextureID);
            }
            if(renderer instanceof GLTimeRenderer t){
                t.setTime(time);
            }
            if(i >= 1){
                GLShader prevShader = renderers.get(i - 1).getShader();
                renderer.setPreviousFBOTextureID(prevShader.getFboTextureID());
            }
            if(i == renderers.size() - 1){
                renderer.getShader().setAsLastFBO();
            }
            renderer.render();
        }

    }

}

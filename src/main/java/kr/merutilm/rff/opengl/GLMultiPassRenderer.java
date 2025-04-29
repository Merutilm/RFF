package kr.merutilm.rff.opengl;

import java.util.ArrayList;
import java.util.List;


public class GLMultiPassRenderer {
    private final List<GLRenderer> renderers;
    private float time;

    public GLMultiPassRenderer() {
        this.renderers = new ArrayList<>();
    }


    public void addRenderer(int index, GLRenderer renderer) {
        this.renderers.add(index, renderer);
    }

    public void addRenderer(GLRenderer renderer) {
        this.renderers.add(renderer);
    }

    public void reloadSize(int w, int h) {
        renderers.forEach(r -> r.reloadSize(w, h));
    }

    public void setTime(float time) {
        this.time = time;
    }

    public void update() {
        int iterationTextureID = 0;
        float resolutionMultiplier = 1;

        for (int i = 0; i < renderers.size(); i++) {
            GLRenderer renderer = renderers.get(i);

            if (renderer instanceof GLIterationTextureProvider p && iterationTextureID == 0) {
                iterationTextureID = p.getIterationTextureID();
                resolutionMultiplier = p.getResolutionMultiplier();
            }
            if (renderer instanceof GLIterationTextureRenderer r) {
                r.setIterationTextureID(iterationTextureID);
                r.setResolutionMultiplier(resolutionMultiplier);
            }
            if (renderer instanceof GLTimeRenderer t) {
                t.setTime(time);
            }
            if (i >= 1) {
                GLShaderLoader prevShader = renderers.get(i - 1).getShader();
                renderer.setPreviousFBOTextureID(prevShader.getFboTextureID());
            }
            if (i == renderers.size() - 1) {
                renderer.getShader().setAsLastFBO();
            }
            renderer.render();
        }

    }

}

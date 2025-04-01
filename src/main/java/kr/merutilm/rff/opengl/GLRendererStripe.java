package kr.merutilm.rff.opengl;

import kr.merutilm.rff.settings.AnimationSettings;
import kr.merutilm.rff.settings.StripeSettings;

import static org.lwjgl.opengl.GL45.*;

public class GLRendererStripe extends GLRenderer implements GLIterationTextureRenderer{


    private int iterationTextureID;
    private StripeSettings stripeSettings;
    private AnimationSettings animationSettings;

    public GLRendererStripe() {
        super(new GLShader(DEFAULT_VERTEX_PATH, "stripe"));
    }

    @Override
    public void setIterationTextureID(int iterationTextureID) {
        this.iterationTextureID = iterationTextureID;
    }

    public void setStripeSettings(StripeSettings stripeSettings) {
        this.stripeSettings = stripeSettings;
    }

    public void setAnimationSettings(AnimationSettings animationSettings) {
        this.animationSettings = animationSettings;
    }

    @Override
    protected void update() {

        shader.uploadTexture2D("inputTex", GL_TEXTURE0, previousFBOTextureID);
        shader.uploadTexture2D("iterations", GL_TEXTURE1, iterationTextureID);
        shader.upload2i("resolution", w, h);

        shader.uploadBool("use", stripeSettings.use());
        shader.uploadFloat("firstInterval", (float) stripeSettings.firstInterval());
        shader.uploadFloat("secondInterval", (float) stripeSettings.secondInterval());
        shader.uploadFloat("opacity", (float) stripeSettings.opacity());
        shader.uploadFloat("offset", (float) (stripeSettings.offset() + animationSettings.stripeAnimationSpeed() * getTime()));

    }
}

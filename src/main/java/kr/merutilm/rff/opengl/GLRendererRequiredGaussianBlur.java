package kr.merutilm.rff.opengl;


import java.util.function.Consumer;

public abstract class GLRendererRequiredGaussianBlur extends GLRenderer{

    private final GLRendererGaussianBlurSinglePass[] gaussianBlurs;

    private static final int MAX_SIZE = 200;

    protected GLRendererRequiredGaussianBlur(GLShader shader, String blurName, int blurTimes){
        super(shader);
        this.gaussianBlurs = new GLRendererGaussianBlurSinglePass[blurTimes];
        for (int i = 0; i < gaussianBlurs.length; i++) {
            gaussianBlurs[i] = new GLRendererGaussianBlurSinglePass(blurName);
        }
    }

    @Override
    public void reloadSize(int w, int h) {
        double aspectRatio = (double) w / h;
        for (GLRendererGaussianBlurSinglePass gaussianBlur : this.gaussianBlurs) {
            gaussianBlur.reloadSize((int) Math.min(MAX_SIZE * aspectRatio, w), Math.min(MAX_SIZE, h));
        }
        super.reloadSize(w, h);

    }

    public void setAdditionalBlurParams(Consumer<GLShader> additionalBlurParams){
        for (GLRendererGaussianBlurSinglePass gaussianBlur : gaussianBlurs) {
            gaussianBlur.setAdditionalParams(additionalBlurParams);
        }
    }

    public int getBlurredTextureID(){
        return gaussianBlurs[gaussianBlurs.length - 1].getShader().getFboTextureID();
    }

    @Override
    public void setPreviousFBOTextureID(int previousFBOTextureID) {
        super.setPreviousFBOTextureID(previousFBOTextureID);
        for (int i = 0; i < gaussianBlurs.length; i++) {
            if(i == 0){
                gaussianBlurs[i].setPreviousFBOTextureID(previousFBOTextureID);
            }else{
                gaussianBlurs[i].setPreviousFBOTextureID(gaussianBlurs[i - 1].getShader().getFboTextureID());
            }
        }
    }

    @Override
    protected void beforeUpdate() {
        for (GLRendererGaussianBlurSinglePass gaussianBlur : gaussianBlurs) {
            gaussianBlur.render();
        }
        super.beforeUpdate();
    }

}

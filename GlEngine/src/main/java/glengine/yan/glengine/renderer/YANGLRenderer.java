package glengine.yan.glengine.renderer;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;

import glengine.yan.glengine.EngineActivity;
import glengine.yan.glengine.assets.YANAssetManager;
import glengine.yan.glengine.nodes.YANCircleNode;
import glengine.yan.glengine.nodes.YANIRenderableNode;
import glengine.yan.glengine.nodes.YANTextNode;
import glengine.yan.glengine.nodes.YANTexturedNode;
import glengine.yan.glengine.programs.YANColorShaderProgram;
import glengine.yan.glengine.programs.YANTextShaderProgram;
import glengine.yan.glengine.programs.YANTextureShaderProgram;
import glengine.yan.glengine.screens.YANIScreen;
import glengine.yan.glengine.service.ServiceLocator;
import glengine.yan.glengine.tasks.YANTaskManager;
import glengine.yan.glengine.util.colors.YANColor;
import glengine.yan.glengine.util.geometry.YANVector2;
import glengine.yan.glengine.util.helpers.YANMatrixHelper;
import glengine.yan.glengine.util.object_pool.YANObjectPool;

import static android.opengl.GLES20.glEnable;

/**
 * Created by Yan-Home on 10/3/2014.
 * <p/>
 * Implementer of graphics rendering using openGL
 */
public class YANGLRenderer {

    private final EngineActivity mEngineActivity;
    private YANIScreen mCurrentScreen;
    private YANVector2 mSurfaceSize;

    // shader programs
    private YANTextureShaderProgram mTextureShaderProgram;
    private YANTextShaderProgram mTextShaderProgram;
    private YANColorShaderProgram colorProgram;
    private long mPreviousFrameTime;
    private final Context mCtx;
    private YANColor mClearColor;
    private boolean mShuttingDown;


    public YANGLRenderer(EngineActivity engineActivity) {
        mCtx = engineActivity.getApplicationContext();
        mSurfaceSize = new YANVector2();
        mClearColor = new YANColor(1f, 1f, 1f, 1f);
        mEngineActivity = engineActivity;
    }

    public void onGLSurfaceCreated() {
        // Enable blending using pre-multiplied alpha.
        setGlInitialStates();
        loadShaderPrograms();
        mPreviousFrameTime = System.currentTimeMillis();
    }

    public void onGLSurfaceChanged(int width, int height) {
        // Set the OpenGL viewport to fill the entire surface.
        GLES20.glViewport(0, 0, width, height);

        //the size of the surface will be used by each screen
        mSurfaceSize = new YANVector2(width, height);

        //when context is recreated all previously loaded textures must be cleaned.
        ServiceLocator.locateService(YANAssetManager.class).reloadAllLoadedTextures();

        //set orthographic projection
        Matrix.orthoM(YANMatrixHelper.projectionMatrix, 0, 0, width, height, 0, 50, 1000);

        //fill view matrix
        Matrix.setLookAtM(YANMatrixHelper.viewMatrix, 0, 0f, 0.0f, 2.0f, 0f, 0f, 0f, 0f, 1f, 0f);

        if (mCurrentScreen != null) {
            //call screen on resize method
            mCurrentScreen.onResize(mSurfaceSize.getX(), mSurfaceSize.getY());
        }
    }

    public void onDrawFrame() {

        float deltaTimeSeconds = ((float) (System.currentTimeMillis() - mPreviousFrameTime)) / 1000f;
        mPreviousFrameTime = System.currentTimeMillis();

        if (mShuttingDown) {
            //release resources
            mCurrentScreen.onSetNotActive();
            ServiceLocator.clearAllServices();
            YANObjectPool.getInstance().empty();
            mEngineActivity.finish();
            return;
        }

        //onUpdate tasks
        YANTaskManager.getInstance().update(deltaTimeSeconds);

        //onUpdate screen state
        if (mCurrentScreen != null) {
            mCurrentScreen.onUpdate(deltaTimeSeconds);
        }

        // Clear the rendering surface.
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        // Update the viewProjection matrix
        Matrix.multiplyMM(YANMatrixHelper.viewProjectionMatrix, 0, YANMatrixHelper.projectionMatrix, 0, YANMatrixHelper.viewMatrix, 0);

        //Inverted View Projection matrix can be used for view picking , but we are not using it right now
//        Matrix.invertM(YANMatrixHelper.invertedViewProjectionMatrix, 0, YANMatrixHelper.viewProjectionMatrix, 0);

        //render each node
        if (mCurrentScreen != null) {
            drawNodes();
        }
    }

    private void drawNodes() {

        for (int i = 0; i < mCurrentScreen.getNodeList().size(); i++) {
            YANIRenderableNode iNode = mCurrentScreen.getNodeList().get(i);

            //do not draw invisible nodes
            if (iNode.getOpacity() == 0)
                continue;

            YANMatrixHelper.positionObjectInScene(iNode);

            //rendering texture node
            if (iNode instanceof YANTexturedNode) {
                mTextureShaderProgram.useProgram();

                //TODO : extract unified model matrix to node
                //TODO : extract texture opacity and overlay to material
                mTextureShaderProgram.setUniforms(
                        YANMatrixHelper.modelViewProjectionMatrix,
                        ServiceLocator.locateService(YANAssetManager.class).getLoadedTextureOpenGLHandle(((YANTexturedNode) iNode).getTextureRegion().getAtlas().getAtlasImageFilePath()),
                        iNode.getOpacity(), iNode.getOverlayColor().asFloatArray());

                //TODO :extract all bind data to mesh of the node
                //bind data
                iNode.bindData(mTextureShaderProgram);
            }

            //rendering text node
            else if (iNode instanceof YANTextNode) {
                mTextShaderProgram.useProgram();
                YANTextNode textNode = (YANTextNode) iNode;
                String texturePath = textNode.getFont().getGlyphImageFilePath();
                mTextShaderProgram.setUniforms(
                        YANMatrixHelper.modelViewProjectionMatrix,
                        ServiceLocator.locateService(YANAssetManager.class).getLoadedTextureOpenGLHandle(texturePath),
                        iNode.getOpacity(), textNode.getTextColor().asFloatArray());

                //bind data
                iNode.bindData(mTextShaderProgram);
            } else if (iNode instanceof YANCircleNode) {
                colorProgram.useProgram();
                YANColor color = ((YANCircleNode) iNode).getColor();
                colorProgram.setUniforms(
                        YANMatrixHelper.modelViewProjectionMatrix,
                        color.asFloatArray(), iNode.getOpacity());

                //bind data
                iNode.bindData(colorProgram);
            }

            //don't know how to render a node
            else {
                throw new RuntimeException("Don't know how to render node of type " + iNode.getClass().getSimpleName());
            }

            //render the node
            iNode.render(this);
        }
    }

    private void setGlInitialStates() {

        //clear color
        GLES20.glClearColor(mClearColor.getR(), mClearColor.getG(), mClearColor.getB(), mClearColor.getA());

        //TODO : when batching will be implemented , consider enable and disable this

        //enable blending by default for all nodes
        glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);

        //enable face culling by default for all nodes
        GLES20.glFrontFace(GLES20.GL_CCW);
        glEnable(GLES20.GL_CULL_FACE);
        GLES20.glCullFace(GLES20.GL_BACK);

    }

    private void loadShaderPrograms() {
        mTextureShaderProgram = new YANTextureShaderProgram(mCtx);
        colorProgram = new YANColorShaderProgram(mCtx);
        mTextShaderProgram = new YANTextShaderProgram(mCtx);
    }

    public void setActiveScreen(YANIScreen screen) {
        if (mCurrentScreen != null) {
            mCurrentScreen.onSetNotActive();
        }
        mCurrentScreen = screen;
        mCurrentScreen.onSetActive();
    }

    public YANVector2 getSurfaceSize() {
        return mSurfaceSize;
    }

    public void setRendererBackgroundColor(YANColor clearColor) {
        mClearColor = clearColor;
        //clear color
        GLES20.glClearColor(mClearColor.getR(), mClearColor.getG(), mClearColor.getB(), mClearColor.getA());
    }

    /**
     * Called when user presses back button
     */
    public void onBackPressed() {
        if (mCurrentScreen != null) {
            mCurrentScreen.onBackPressed();
        }
    }

    public Context getAppContext() {
        return mCtx;
    }

    public EngineActivity getEngineActivity() {
        return mEngineActivity;
    }

    public void shutDown() {
        mShuttingDown = true;
    }
}

package freed.cam.apis.basecamera;

import com.troop.freedcam.utils.Log;

import freed.ActivityInterface;
import freed.cam.apis.basecamera.modules.ModuleHandlerAbstract;
import freed.cam.apis.basecamera.parameters.AbstractParameterHandler;
import freed.renderscript.RenderScriptManager;
import freed.renderscript.RenderScriptProcessorInterface;

public abstract class AbstractCameraController<P extends AbstractParameterHandler,C extends CameraHolderAbstract> implements CameraInterface, CameraControllerInterface
{
    private final String TAG = AbstractCameraController.class.getSimpleName();
    protected RenderScriptManager renderScriptManager;
    public ModuleHandlerAbstract moduleHandler;
    /**
     * parameters for avail for the cameraHolder
     */
    public P parametersHandler;
    /**
     * holds the current camera
     */
    public C cameraHolder;
    /**
     * handels focus releated stuff for the current camera
     */
    public AbstractFocusHandler focusHandler;

    protected boolean PreviewSurfaceRdy;
    private ActivityInterface activityInterface;

    /**
     * holds handler to invoke stuff in ui or camera thread
     */
    protected MainToCameraHandler mainToCameraHandler;
    protected CameraToMainHandler cameraToMainHandler;

    public void init(MainToCameraHandler mainToCameraHandler, CameraToMainHandler cameraToMainHandler, ActivityInterface activityInterface)
    {
        Log.d(TAG, "init handler");
        this.mainToCameraHandler = mainToCameraHandler;
        this.cameraToMainHandler = cameraToMainHandler;
        this.activityInterface = activityInterface;
    }

    @Override
    public void startCameraAsync() {
        Log.d(TAG, "startCameraAsync");
        if (mainToCameraHandler != null)
            mainToCameraHandler.startCamera();
        else
            Log.d(TAG, "MainToCameraHandler is null");
    }

    @Override
    public void stopCameraAsync() {
        if (mainToCameraHandler != null)
            mainToCameraHandler.stopCamera();
        else
            Log.d(TAG, "MainToCameraHandler is null");
    }

    @Override
    public void restartCameraAsync() {
        if (mainToCameraHandler != null)
            mainToCameraHandler.restartCamera();
        else
            Log.d(TAG, "MainToCameraHandler is null");
    }

    @Override
    public void startPreviewAsync() {
        if (mainToCameraHandler != null)
            mainToCameraHandler.startPreview();
        else
            Log.d(TAG, "MainToCameraHandler is null");
    }

    @Override
    public void stopPreviewAsync() {
        if (mainToCameraHandler != null)
            mainToCameraHandler.stopPreview();
        else
            Log.d(TAG, "MainToCameraHandler is null");
    }

    @Override
    public void restartPreviewAsync() {
        if (mainToCameraHandler != null)
            mainToCameraHandler.restartPreview();
        else
            Log.d(TAG, "MainToCameraHandler is null");
    }

    @Override
    public RenderScriptManager getRenderScriptManager() {
        return renderScriptManager;
    }

    @Override
    public ActivityInterface getActivityInterface() {
        return activityInterface;
    }

    @Override
    public boolean isAeMeteringSupported() {
        return focusHandler.isAeMeteringSupported();
    }

    @Override
    public RenderScriptProcessorInterface getFocusPeakProcessor() {
        return null;
    }

    @Override
    public AbstractFocusHandler getFocusHandler() {
        return focusHandler;
    }

    @Override
    public C getCameraHolder() {
        return cameraHolder;
    }

    @Override
    public P getParameterHandler() {
        return parametersHandler;
    }

    @Override
    public ModuleHandlerAbstract getModuleHandler() {
        return moduleHandler;
    }


}

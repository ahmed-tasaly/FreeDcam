package freed.cam.previewpostprocessing;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.view.Surface;
import android.view.View;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.troop.freedcam.R;

import freed.cam.apis.PreviewFragment;
import freed.cam.apis.basecamera.CameraThreadHandler;
import freed.cam.histogram.HistogramController;
import freed.cam.histogram.HistogramFeed;
import freed.utils.Log;

public class PreviewController implements PreviewControllerInterface
{
    private static final String TAG = PreviewController.class.getSimpleName();
    private Preview preview;
    PreviewEvent eventListner;

    private int fragmentHolderId;
    private FragmentManager fragmentManager;
    //private CameraFragmentAbstract cameraFragment;
    private PreviewFragment previewFragment;

    public void init(FragmentManager fragmentManager, int fragmentHolderId) {
        this.fragmentManager = fragmentManager;
        this.fragmentHolderId = fragmentHolderId;
    }

    public boolean isPreviewInit()
    {
        return previewFragment != null;
    }

    @Override
    public void initPreview(PreviewPostProcessingModes previewPostProcessingModes, Context context, HistogramController histogram)
    {
        Log.d(TAG, "init preview " +previewPostProcessingModes.name());
        if (preview != null)
            preview.close();
        switch (previewPostProcessingModes)
        {
            case off:
                preview = new NormalPreview(context);
                break;
            case RenderScript:
                preview = new RenderScriptPreview(context,histogram);
                break;
            case OpenGL:
                preview = new OpenGLPreview(context,histogram);
                break;
        }
        preview.setPreviewEventListner(eventListner);
    }

    @Override
    public void setHistogramFeed(HistogramFeed feed) {
        if (preview != null)
            this.preview.setHistogramFeed(feed);
    }

    @Override
    public void clear() {
        preview.clear();
    }

    public Preview getPreview() {
        return preview;
    }

    @Override
    public void close() {
        if (preview != null)
            preview.close();
    }

    public SurfaceTexture getSurfaceTexture()
    {
        return preview.getSurfaceTexture();
    }

    public Surface getInputSurface()
    {
        return preview.getInputSurface();
    }

    @Override
    public void setOutputSurface(Surface surface) {
        preview.setOutputSurface(surface);
    }

    @Override
    public void setSize(int width, int height) {
        preview.setSize(width,height);
    }

    @Override
    public boolean isSucessfullLoaded() {
        return preview.isSucessfullLoaded();
    }

    @Override
    public void setBlue(boolean blue) {
        preview.setBlue(blue);
    }

    @Override
    public void setRed(boolean red) {
        if (preview != null)
            preview.setRed(red);
    }

    @Override
    public void setGreen(boolean green) {
        if (preview != null)
            preview.setGreen(green);
    }

    @Override
    public void setFocusPeak(boolean on) {
        preview.setFocusPeak(on);
    }

    @Override
    public boolean isFocusPeak() {
        if (preview == null)
            return false;
        return preview.isFocusPeak();
    }

    @Override
    public void setClipping(boolean on) {
        if (preview != null)
            preview.setClipping(on);
    }

    @Override
    public boolean isClipping() {
        if (preview == null)
            return false;
        return preview.isClipping();
    }

    @Override
    public void setHistogram(boolean on) {
        if (preview != null)
            preview.setHistogram(on);
    }

    @Override
    public boolean isHistogram() {
        if (preview == null)
            return false;
        return preview.isHistogram();
    }

    @Override
    public void start() {
        preview.start();
    }

    @Override
    public void stop() {
        preview.stop();
    }

    @Override
    public View getPreviewView() {
        return preview.getPreviewView();
    }

    @Override
    public void setPreviewEventListner(PreviewEvent eventListner) {
        this.eventListner = eventListner;
        if (preview != null)
            preview.setPreviewEventListner(eventListner);
    }

    @Override
    public int getPreviewWidth() {
        return preview.getPreviewWidth();
    }

    @Override
    public int getPreviewHeight() {
        return preview.getPreviewHeight();
    }

    @Override
    public void setRotation(int width, int height, int rotation) {
        preview.setRotation(width,height,rotation);
    }

    @Override
    public int getMargineLeft() {
        if (preview.getPreviewView() == null)
            return 0;
        return preview.getPreviewView().getLeft();
    }

    @Override
    public int getMargineRight() {
        return preview.getPreviewView().getRight();
    }

    @Override
    public int getMargineTop() {
        return preview.getPreviewView().getTop();
    }

    public void changePreviewPostProcessing()
    {
        if (previewFragment != null) {
            Log.d(TAG, "unload old Preview");
            //kill the cam befor the fragment gets removed to make sure when
            //new cameraFragment gets created and its texture view is created the cam get started
            //when its done in textureview/surfaceview destroy method its already to late and we get a security ex lack of privilege
            FragmentTransaction transaction  = fragmentManager.beginTransaction();
            transaction.setCustomAnimations(R.anim.right_to_left_enter, R.anim.right_to_left_exit);
            transaction.remove(previewFragment);
            transaction.commit();
            previewFragment = null;
        }
        Log.d(TAG, "load new Preview");
        previewFragment = new PreviewFragment();
        FragmentTransaction transaction  = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(R.anim.left_to_right_enter, R.anim.left_to_right_exit);
        transaction.replace(fragmentHolderId, previewFragment, previewFragment.getClass().getSimpleName());
        transaction.commit();
    }


}

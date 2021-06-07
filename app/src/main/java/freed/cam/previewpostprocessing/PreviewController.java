package freed.cam.previewpostprocessing;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.view.Surface;
import android.view.View;

import freed.cam.histogram.HistogramController;
import freed.cam.histogram.HistogramFeed;
import freed.utils.Log;

public class PreviewController implements PreviewControllerInterface
{
    private static final String TAG = PreviewController.class.getSimpleName();
    private Preview preview;
    PreviewEvent eventListner;

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


}

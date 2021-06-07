package freed.cam.apis;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.fragment.app.Fragment;

import com.troop.freedcam.R;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import dagger.hilt.android.EntryPointAccessors;
import freed.cam.histogram.HistogramController;
import freed.cam.previewpostprocessing.PreviewController;
import freed.cam.previewpostprocessing.PreviewPostProcessingModes;
import freed.settings.SettingKeys;
import freed.settings.SettingsManager;
import freed.utils.Log;
import freed.viewer.screenslide.views.MyHistogram;
import hilt.PreviewControllerEntryPoint;

@AndroidEntryPoint
public class PreviewFragment  extends Fragment {

    private static final String TAG = PreviewFragment.class.getSimpleName();

    protected View view;
    @Inject
    protected PreviewController preview;
    @Inject
    SettingsManager settingsManager;

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        Log.d(TAG, "onCreateView");
        view = layoutInflater.inflate(R.layout.camerafragment, viewGroup, false);
        MyHistogram histogram = view.findViewById(R.id.hisotview);
        HistogramController histogramController = new HistogramController(histogram);
        if (settingsManager.getGlobal(SettingKeys.PREVIEW_POST_PROCESSING_MODE).get() == null)
            preview.initPreview(PreviewPostProcessingModes.off,getContext(),histogramController);
        else if (settingsManager.getGlobal(SettingKeys.PREVIEW_POST_PROCESSING_MODE).get().equals(PreviewPostProcessingModes.RenderScript.name()))
            preview.initPreview(PreviewPostProcessingModes.RenderScript, getContext(), histogramController);
        else if (settingsManager.getGlobal(SettingKeys.PREVIEW_POST_PROCESSING_MODE).get().equals(PreviewPostProcessingModes.OpenGL.name()))
            preview.initPreview(PreviewPostProcessingModes.OpenGL, getContext(), histogramController);
        else
            preview.initPreview(PreviewPostProcessingModes.off,getContext(),histogramController);
        FrameLayout frameLayout = view.findViewById(R.id.autofitview);
        frameLayout.addView(preview.getPreviewView());
        return view;
    }

    @Override
    public void onDestroy() {
        FrameLayout frameLayout = view.findViewById(R.id.autofitview);
        frameLayout.removeAllViews();
        super.onDestroy();
    }
}

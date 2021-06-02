package freed.cam.apis;


import android.content.Context;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.troop.freedcam.R;

import javax.inject.Inject;

import freed.ActivityInterface;
import freed.cam.apis.basecamera.CameraFragmentAbstract;
import freed.cam.apis.basecamera.CameraThreadHandler;
import freed.cam.apis.camera1.Camera1Fragment;
import freed.cam.apis.camera2.Camera2Fragment;
import freed.cam.apis.featuredetector.CameraFeatureDetector;
import freed.cam.apis.sonyremote.SonyCameraRemoteFragment;
import freed.settings.SettingKeys;
import freed.settings.SettingsManager;
import freed.utils.BackgroundHandlerThread;
import freed.utils.Log;

public class CameraFragmentManager {
    private final String TAG = CameraFragmentManager.class.getSimpleName();

    private int fragmentHolderId;
    private FragmentManager fragmentManager;
    private CameraFragmentAbstract cameraFragment;

    private BackgroundHandlerThread backgroundHandlerThread;
    private SettingsManager settingsManager;

    @Inject
    public CameraFragmentManager(SettingsManager settingsManager)
    {
        this.settingsManager = settingsManager;
    }

    public void init(FragmentManager fragmentManager, int fragmentHolderId)
    {
        this.fragmentManager = fragmentManager;
        this.fragmentHolderId = fragmentHolderId;
        Log.d(TAG,"Create camera BackgroundHandler");
        backgroundHandlerThread = new BackgroundHandlerThread(TAG);
        backgroundHandlerThread.create();
        new CameraThreadHandler(backgroundHandlerThread.getThread().getLooper());
    }



    public void destroy()
    {
        Log.d(TAG,"Destroy camera BackgroundHandler");
        backgroundHandlerThread.destroy();
        CameraThreadHandler.close();
    }

    public CameraFragmentAbstract getCameraFragment()
    {
        return cameraFragment;
    }


    private void replaceCameraFragment(Fragment fragment, String id)
    {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(R.anim.left_to_right_enter, R.anim.left_to_right_exit);
        transaction.replace(fragmentHolderId, fragment, id);
        transaction.commit();
    }

    private void loadFeatureDetector() {
        Log.d(TAG, "Start FeatureDetector");
        settingsManager.setAreFeaturesDetected(false);
        new CameraFeatureDetector().detectFeatures();
    }


    public void onResume()
    {
        Log.d(TAG, "onResume");
        if (cameraFragment != null) {
            Log.d(TAG, "Reuse CamaraFragment");
        }
        else {
            Log.d(TAG, "create new CameraFragment");
            switchCameraFragment();
        }
    }

    public void  onPause()
    {
        Log.d(TAG, "onPause");
        if (cameraFragment != null) {
            //unloadCameraFragment();
            /*cameraFragment.stopCameraAsync();
            mainToCameraHandler.setCameraInterface(null);*/
        }
    }

    public void switchCameraFragment()
    {
        Log.d(TAG, "BackgroundHandler is null: " + (backgroundHandlerThread.getThread() == null) +
                " features detected: " + settingsManager.getAreFeaturesDetected() + " app version changed: " + settingsManager.appVersionHasChanged());
        if ((!settingsManager.getAreFeaturesDetected() || settingsManager.appVersionHasChanged()))
        {
            Log.d(TAG, "load featuredetector");
            if (cameraFragment != null)
                unloadCameraFragment();
            loadFeatureDetector();
        }
        else
        {
            if (cameraFragment == null) {
                String api = settingsManager.getCamApi();
                switch (api) {
                    case SettingsManager.API_SONY:
                        Log.d(TAG, "load sony remote");
                        cameraFragment = SonyCameraRemoteFragment.getInstance();
                        break;
                    case SettingsManager.API_2:
                        Log.d(TAG, "load camera2");
                        cameraFragment = Camera2Fragment.getInstance();
                        break;
                    default:
                        Log.d(TAG, "load camera1");
                        cameraFragment = Camera1Fragment.getInstance();
                        break;
                }

                replaceCameraFragment(cameraFragment, cameraFragment.getClass().getSimpleName());
            }
        }
    }

    public void unloadCameraFragment()
    {
        Log.d(TAG, "unloadCameraFragment");
        if (cameraFragment != null) {
            //kill the cam befor the fragment gets removed to make sure when
            //new cameraFragment gets created and its texture view is created the cam get started
            //when its done in textureview/surfaceview destroy method its already to late and we get a security ex lack of privilege
            CameraThreadHandler.stopCameraAsync();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.setCustomAnimations(R.anim.right_to_left_enter, R.anim.right_to_left_exit);
            transaction.remove(cameraFragment);
            transaction.commit();
            cameraFragment = null;
            CameraThreadHandler.setCameraInterface(null);
        }
    }

    public void runFeatureDetector() {
        unloadCameraFragment();
        boolean legacy = settingsManager.get(SettingKeys.openCamera1Legacy).get();
        boolean showHelpOverlay = settingsManager.getShowHelpOverlay();
        settingsManager.RESET();
        settingsManager.get(SettingKeys.openCamera1Legacy).set(legacy);
        settingsManager.setshowHelpOverlay(showHelpOverlay);
        switchCameraFragment();
    }

}

package freed.cam.apis.camera2.parameters.manual;

import android.hardware.camera2.CaptureRequest;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.qcom.CaptureRequestQcom;

import freed.cam.apis.basecamera.CameraWrapperInterface;
import freed.cam.apis.basecamera.parameters.AbstractParameter;
import freed.cam.apis.camera2.Camera2Fragment;
import freed.settings.SettingKeys;
import freed.settings.SettingsManager;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class ManualSharpnessQcomApi2 extends AbstractParameter {

    public ManualSharpnessQcomApi2(CameraWrapperInterface cameraUiWrapper) {
        super(cameraUiWrapper,SettingKeys.M_Sharpness);
        if (SettingsManager.get(SettingKeys.M_Sharpness).isSupported())
        {
            setViewState(ViewState.Visible);
            stringvalues = SettingsManager.get(SettingKeys.M_Sharpness).getValues();
            if (stringvalues == null || stringvalues.length == 0) {
                setViewState(ViewState.Hidden);
            }
            else
                setViewState(ViewState.Visible);
            currentInt = 0;
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void SetValue(int valueToSet, boolean setToCamera) {
        super.setValue(valueToSet, setToCamera);
        ((Camera2Fragment) cameraUiWrapper).captureSessionHandler.SetParameterRepeating(CaptureRequestQcom.sharpness, currentInt,setToCamera);
    }


    @Override
    public String GetStringValue() {
        return stringvalues[currentInt];
    }
}
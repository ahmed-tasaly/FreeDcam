/*
 *
 *     Copyright (C) 2015 Ingo Fuchs
 *     This program is free software; you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation; either version 2 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc.,
 *     51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * /
 */

package freed.cam.apis.camera1.parameters.modes;

import android.hardware.Camera.Parameters;

import freed.cam.apis.basecamera.CameraWrapperInterface;
import freed.cam.apis.basecamera.modules.ModuleChangedEvent;
import freed.cam.apis.basecamera.parameters.AbstractParameter;
import freed.cam.apis.basecamera.parameters.ParameterEvents;
import freed.cam.apis.camera1.parameters.ParametersHandler;
import freed.utils.AppSettingsManager;
import freed.utils.Log;

/**
 * Created by troop on 17.08.2014.
 * That class handel basic parameter logic and
 * expect a value String like "antibanding" and a values String "antibanding-values"
 * if one of the key is empty the parameters is set as unsupported
 * when extending that class make sure you set isSupported and isVisible
 */
public class BaseModeParameter extends AbstractParameter implements ModuleChangedEvent, ParameterEvents
{
    /*
    The Key to set/get a value from the parameters
     */
    protected String key_value;
    //the parameters from the android.Camera
    protected Parameters  parameters;
    protected CameraWrapperInterface cameraUiWrapper;
    private final String TAG = BaseModeParameter.class.getSimpleName();


    public BaseModeParameter(Parameters  parameters, CameraWrapperInterface cameraUiWrapper)
    {
        this.parameters = parameters;
        this.cameraUiWrapper = cameraUiWrapper;
    }

    public BaseModeParameter(Parameters  parameters, CameraWrapperInterface cameraUiWrapper, AppSettingsManager.SettingMode settingMode)
    {
        this(parameters,cameraUiWrapper);
        this.key_value = settingMode.getKEY();
        this.stringvalues = settingMode.getValues();
        this.isSupported = settingMode.isSupported();
    }

    @Override
    public void SetValue(String valueToSet,  boolean setToCam)
    {
        super.SetValue(valueToSet,setToCam);
        if (valueToSet == null)
            return;
        parameters.set(key_value, valueToSet);
        Log.d(TAG, "set " + key_value + " to " + valueToSet);
        if (setToCam) {

            ((ParametersHandler) cameraUiWrapper.getParameterHandler()).SetParametersToCamera(parameters);
        }
    }

    @Override
    public void onModuleChanged(String module) {

    }

    @Override
    public void onIsSupportedChanged(boolean value) {
    }

    @Override
    public void onIsSetSupportedChanged(boolean value) {

    }

    @Override
    public void onIntValueChanged(int current) {

    }

    @Override
    public void onValuesChanged(String[] values) {

    }

    @Override
    public void onStringValueChanged(String value) {

    }

    @Override
    public void onStringValuesChanged(String[] values) {

    }
}

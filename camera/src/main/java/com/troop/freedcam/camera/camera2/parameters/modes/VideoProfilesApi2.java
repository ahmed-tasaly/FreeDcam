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

package com.troop.freedcam.camera.camera2.parameters.modes;

import com.troop.freedcam.camera.R;

import com.troop.freedcam.utils.ContextApplication;
import com.troop.freedcam.camera.basecamera.CameraControllerInterface;
import com.troop.freedcam.camera.camera1.parameters.modes.VideoProfilesParameter;

/**
 * Created by troop on 24.02.2016.
 */
public class VideoProfilesApi2 extends VideoProfilesParameter
{
    final String TAG = VideoProfilesApi2.class.getSimpleName();

    public VideoProfilesApi2(CameraControllerInterface cameraUiWrapper)
    {
        super(cameraUiWrapper);
    }

    @Override
    protected void setValue(String valueToSet, boolean setToCamera) {
        profile = valueToSet;
        currentString = valueToSet;
        fireStringValueChanged(currentString);
        if (settingMode != null)
            settingMode.set(valueToSet);
        if (cameraUiWrapper !=null && cameraUiWrapper.getModuleHandler().getCurrentModule() != null
                && cameraUiWrapper.getModuleHandler().getCurrentModuleName().equals(ContextApplication.getStringFromRessources(R.string.module_video)))
        {
            cameraUiWrapper.getModuleHandler().getCurrentModule().DestroyModule();
            cameraUiWrapper.getModuleHandler().getCurrentModule().InitModule();
        }
    }
}

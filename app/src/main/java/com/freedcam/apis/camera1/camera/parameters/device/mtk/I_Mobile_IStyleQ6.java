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

package com.freedcam.apis.camera1.camera.parameters.device.mtk;

import android.hardware.Camera;
import android.os.Handler;

import com.freedcam.apis.basecamera.camera.parameters.modes.MatrixChooserParameter;
import com.freedcam.apis.camera1.camera.CameraUiWrapper;
import com.freedcam.apis.camera1.camera.parameters.device.BaseMTKDevice;
import com.troop.androiddng.DngProfile;

/**
 * Created by troop on 01.06.2016.
 */
public class I_Mobile_IStyleQ6 extends BaseMTKDevice {
    public I_Mobile_IStyleQ6(Handler uihandler, Camera.Parameters parameters, CameraUiWrapper cameraUiWrapper) {
        super(uihandler, parameters, cameraUiWrapper);
    }

    @Override
    public DngProfile getDngProfile(int filesize)
    {
        switch (filesize) {
            case 9631728: //I_Mobile_I_StyleQ6
                return new DngProfile(0, 2532, 1902, DngProfile.Plain, DngProfile.GRBG, 0,
                        matrixChooserParameter.GetCustomMatrix(MatrixChooserParameter.OmniVision));
        }
        return null;
    }
}
package com.troop.freecam;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.troop.freecam.activitys.BaseActivity;
import com.troop.freecam.activitys.ButtonsActivity;
import com.troop.freecam.activitys.InfoScreenActivity;
import com.troop.freecam.controls.ExtendedButton;
import com.troop.freecam.manager.Drawing.DrawingOverlaySurface;
import com.troop.freecam.manager.ManualSaturationManager;
import com.troop.freecam.manager.MyTimer;
import com.troop.freecam.manager.ParametersManager;
import com.troop.freecam.manager.interfaces.ParametersChangedInterface;
import com.troop.freecam.utils.DeviceUtils;
import com.troop.menu.AFPriorityMenu;
import com.troop.menu.ColorMenu;
import com.troop.menu.DenoiseMenu;
import com.troop.menu.ExposureMenu;
import com.troop.menu.FlashMenu;
import com.troop.menu.FocusMenu;
import com.troop.menu.IppMenu;
import com.troop.menu.IsoMenu;
import com.troop.menu.MeteringMenu;
import com.troop.menu.PictureFormatMenu;
import com.troop.menu.PictureSizeMenu;
import com.troop.menu.PreviewFormatMenu;
import com.troop.menu.PreviewSizeMenu;
import com.troop.menu.SceneMenu;
import com.troop.menu.VideoSizesMenu;
import com.troop.menu.WhiteBalanceMenu;
import com.troop.menu.ZslMenu;
import com.troop.menu.switchcameramenu;

import java.io.File;

public class MainActivity extends InfoScreenActivity implements ParametersChangedInterface
{
	public CamPreview mPreview;
    public DrawingOverlaySurface drawSurface;
    public CheckBox checkBoxZSL;
    //*******************
	Camera.Parameters paras;
    SurfaceHolder holder;
    public Boolean AFS_enable;
    Button AfAssitButton;
    Button manualLayoutButton;
    Button autoLayoutButton;
    Button settingLayoutButton;
    LinearLayout baseMenuLayout;
    LinearLayout manualMenuLayout;
    LinearLayout autoMenuLayout;
    LinearLayout settingsMenuLayout;
    public boolean hideManualMenu = true;
    public boolean hideSettingsMenu = true;
    public boolean hideAutoMenu = true;
    int currentZoom = 0;
    SensorManager sensorManager;
    Sensor sensor;
    public Button button_stab;
    View view;

    //private final int DEFAULT_SYSTEM_UI_VISIBILITY = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
    //        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        drawSurface = (DrawingOverlaySurface) findViewById(R.id.view);
		mPreview = (CamPreview) findViewById(R.id.camPreview1);
        mPreview.setKeepScreenOn(true);
        holder = mPreview.getHolder();
        camMan = new CameraManager(mPreview, this, preferences);
        camMan.parametersManager.setParametersChanged(this);

        mPreview.SetCameraManager(camMan);
        drawSurface.SetCameraManager(camMan);

        sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        //List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);

        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;

        initMenu();
        recordTimer = new MyTimer(recordingTimerTextView);
        chipsetProp();

        showtext();
        hidenavkeys();

        hideCurrentConfig();

	}

   /* @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        getWindow().getDecorView()
                .setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        //Check the event and do magic here, such as...
        if (event.getAction() == MotionEvent.ACTION_DOWN) {


        }

        //Be careful not to override the return unless necessary
        return super.dispatchTouchEvent(event);
    } */

    public void videoui()
    {


    }

    public void initMenu()
    {
        baseMenuLayout = (LinearLayout)findViewById(R.id.baseMenuLayout);
        autoMenuLayout = (LinearLayout)findViewById(R.id.LayoutAuto);
        manualMenuLayout = (LinearLayout)findViewById(R.id.Layout_Manual);
        settingsMenuLayout = (LinearLayout)findViewById(R.id.LayoutSettings);


        manualLayoutButton = (Button)findViewById(R.id.buttonManualMode);
        manualLayoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                if (hideManualMenu == false)
                {
                    hideManualMenu = true;
                    manualMenuLayout.setVisibility(View.GONE);
                }
                else
                {
                    hideManualMenu = false;
                    //if (baseMenuLayout.findViewById(R.id.Layout_Manual) == null)
                        manualMenuLayout.setVisibility(View.VISIBLE);
                    if (hideAutoMenu == false)
                    {
                        hideAutoMenu = true;
                        autoMenuLayout.setVisibility(View.GONE);
                    }
                    if (hideSettingsMenu == false)
                    {
                        hideSettingsMenu = true;
                        settingsMenuLayout.setVisibility(View.GONE);
                    }
                }

            }
        });

        autoLayoutButton = (Button)findViewById(R.id.buttonAutoMode);
        autoLayoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                if (hideAutoMenu == false)
                {
                    hideAutoMenu = true;

                    autoMenuLayout.setVisibility(View.GONE);
                }
                else
                {
                    hideAutoMenu = false;
                    //if (baseMenuLayout.findViewById(R.id.LayoutAuto) == null)
                        autoMenuLayout.setVisibility(View.VISIBLE);

                    if (hideSettingsMenu == false)
                    {
                        hideSettingsMenu = true;
                        settingsMenuLayout.setVisibility(View.GONE);
                    }
                    if (hideManualMenu == false)
                    {
                        hideManualMenu = true;
                        manualMenuLayout.setVisibility(View.GONE);
                    }

                }

            }
        });
        settingLayoutButton = (Button)findViewById(R.id.buttonSettingsMode);
        settingLayoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                if (hideSettingsMenu == false)
                {
                    hideSettingsMenu = true;
                    settingsMenuLayout.setVisibility(View.GONE);
                }
                else
                {
                    hideSettingsMenu = false;
                    //if (baseMenuLayout.findViewById(R.id.LayoutSettings) == null)
                        settingsMenuLayout.setVisibility(View.VISIBLE);
                    if (hideAutoMenu == false)
                    {
                        hideAutoMenu = true;
                        autoMenuLayout.setVisibility(View.GONE);
                    }
                    if (hideManualMenu == false)
                    {
                        hideManualMenu = true;
                        manualMenuLayout.setVisibility(View.GONE);
                    }

                }

            }
        });

        autoMenuLayout.setVisibility(View.GONE);
        manualMenuLayout.setVisibility(View.GONE);
        settingsMenuLayout.setVisibility(View.GONE);


    }







    public void tabletScaling()
    {
        //Will Scale Entire UI For Tablet Mode Current Tabs Nexus 7 / Nexus 10
    }


    public void chipsetProp()
    {
        try {
            String s = Build.MODEL;

            if(!DeviceUtils.isG2())
                manualFocus.setVisibility(View.GONE);

            if (!DeviceUtils.isQualcomm())
                checkBoxZSL.setEnabled(false);
                buttonMetering.setEnabled(false);

            if(!s.equals("LG-P720") || !s.equals("LG-P725"))
                upsidedown.setVisibility(View.GONE);

            if (!DeviceUtils.isOmap())
                ippButton.setEnabled(false);
                exposureButton.setEnabled(false);
        }
        catch (NullPointerException ex)
        {


        }


    }
    public void hidenavkeys()
    {
        try {

            view.setSystemUiVisibility(2);
        }
        catch (NullPointerException ex)
        {

        }
    }






    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int key = event.getKeyCode();

            if(key == KeyEvent.KEYCODE_VOLUME_UP)
            {
                camMan.zoomManager.setZoom(1);
            }
            else if(key == KeyEvent.KEYCODE_VOLUME_DOWN)
            {
                camMan.zoomManager.setZoom(-1);
            }
            else if(key == KeyEvent.KEYCODE_3D_MODE ||key == KeyEvent.KEYCODE_POWER )
            {
                camMan.StartTakePicture();
            }
            else
            {
                super.dispatchKeyEvent(event);
            }

        return true;
    }


    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

    View.OnClickListener AFSListner = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            if (AFS_enable != true)
            {
                AFS_enable = true;
            }
            else
            {
                AFS_enable = false;
            }

        }
    };
	


    public void SwitchCropButton()
    {
        if(!preferences.getString(ParametersManager.SwitchCamera, ParametersManager.SwitchCamera_MODE_2D).equals(ParametersManager.SwitchCamera_MODE_3D))
        {
            settingsMenuLayout.removeView(crop_box);
        }
        else
        {
            try
            {
            settingsMenuLayout.addView(crop_box);
            }
            catch (Exception ex)
            {
                Log.d("MainActivity", "CropBox is already added");
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(camMan, sensor, SensorManager.SENSOR_DELAY_NORMAL);

    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(camMan);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {

            if(resultCode == RESULT_OK){
                String result=data.getStringExtra("result");
                camMan.onPictureSaved(new File(result));
            }
            if (resultCode == RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }
    }

    ///this updates the complete ui. its called everytime the camera parameters are changed
    @Override
    public void parametersHasChanged(boolean restarted)
    {
        try{
            buttonPreviewFormat.SetValue(camMan.parametersManager.getParameters().get("preview-format"));
            sceneButton.setText(camMan.parametersManager.getParameters().getSceneMode());
            previewSizeButton.SetValue(camMan.parametersManager.getParameters().getPreviewSize().width + "x" + camMan.parametersManager.getParameters().getPreviewSize().height);
            button_denoise.SetValue(camMan.parametersManager.getDenoiseValue());
            String size1 = String.valueOf(camMan.parametersManager.getParameters().getPictureSize().width) + "x" + String.valueOf(camMan.parametersManager.getParameters().getPictureSize().height);
            pictureSizeButton.SetValue(size1);
            videoSizeButton.SetValue(camMan.parametersManager.videoModes.Width + "x" + camMan.parametersManager.videoModes.Height);

            //ZeroShutterLag
            if (camMan.parametersManager.getSupportZSL())
            {
                if (button_zsl.getVisibility() == View.GONE)
                    button_zsl.setVisibility(View.VISIBLE);
                button_zsl.SetValue(camMan.parametersManager.ZSLModes.getValue());
            }
            else
                if (button_zsl.getVisibility() == View.VISIBLE)
                    button_zsl.setVisibility(View.GONE);

            //ImagePostProcessing
            if (camMan.parametersManager.getSupportIPP())
            {
                if (ippButton.getVisibility() == View.GONE)
                    ippButton.setVisibility(View.VISIBLE);
                ippButton.SetValue(camMan.parametersManager.getParameters().get("ipp"));
            }
            else
                ippButton.setVisibility(View.GONE);

            //ManualExposure
            camMan.manualExposureManager.SetMinMax(camMan.parametersManager.getParameters().getMinExposureCompensation(), camMan.parametersManager.getParameters().getMaxExposureCompensation());
            camMan.manualExposureManager.ExternalSet = true;
            camMan.manualExposureManager.SetCurrentValue(camMan.parametersManager.getParameters().getExposureCompensation());
            exposureTextView.setText("Exposure: " + camMan.parametersManager.getParameters().getExposureCompensation());
            //Sharpness
            if (camMan.parametersManager.getSupportSharpness())
            {
                sharpnessSeekBar.setMax(180);
                sharpnessSeekBar.setProgress(camMan.parametersManager.getParameters().getInt("sharpness"));
                sharpnessTextView.setText("Sharpness: " + camMan.parametersManager.getParameters().getInt("sharpness"));
            }
            //Contrast
            if (camMan.parametersManager.getSupportContrast())
            {
                contrastSeekBar.setMax(180);
                camMan.manualContrastManager.ExternalSet = true;
                contrastSeekBar.setProgress(camMan.parametersManager.getParameters().getInt("contrast"));
                contrastTextView.setText("Contrast: " + camMan.parametersManager.getParameters().get("contrast"));
            }
            //Brightness
            if (camMan.parametersManager.getSupportBrightness())
            {
                brightnessSeekBar.setMax(100);
                brightnessSeekBar.setProgress(camMan.parametersManager.Brightness.Get());
                brightnessTextView.setText("Brightness: " + camMan.parametersManager.Brightness.Get());
            }
            //Saturation
            if (camMan.parametersManager.getSupportSaturation())
            {
                saturationSeekBar.setMax(180);
                saturationTextView.setText("Saturation: " + camMan.parametersManager.getParameters().get("saturation"));
            }
            //Cropping
            if (camMan.parametersManager.is3DMode())
            {
                crop_box.setVisibility(View.VISIBLE);
                crop_box.setChecked(camMan.parametersManager.doCropping());
            }
            else
                crop_box.setVisibility(View.GONE);
            //FLASH
            if (!camMan.parametersManager.getSupportFlash())
                flashButton.setVisibility(View.GONE);
            else
            {
                if (flashButton.getVisibility() == View.GONE)
                    flashButton.setVisibility(View.VISIBLE);
                flashButton.SetValue(camMan.parametersManager.getParameters().getFlashMode());
            }


            //info Screen
            showtext();
            focusButton.SetValue(camMan.parametersManager.getParameters().getFocusMode());
            //AF Priority
            if (!camMan.parametersManager.getSupportAfpPriority())
                buttonAfPriority.setVisibility(View.GONE);
            else
            {
                if (buttonAfPriority.getVisibility() == View.GONE)
                    buttonAfPriority.setVisibility(View.VISIBLE);
                OnScreenFocusValue.setText("AFP:"+ camMan.parametersManager.AfPriority.Get());
                buttonAfPriority.SetValue(camMan.parametersManager.AfPriority.Get());
            }
            //AutoExposure
            if (!camMan.parametersManager.getSupportAutoExposure())
            {
                buttonMetering.setVisibility(View.GONE);
            }
            //Select Camera
            String tmp = preferences.getString(ParametersManager.SwitchCamera, ParametersManager.SwitchCamera_MODE_2D);
            switch3dButton.SetValue(tmp);
            //Crosshair appairing
            if (camMan.parametersManager.getParameters().getFocusMode().equals("auto"))
            {
                drawSurface.drawingRectHelper.Enabled = true;
            }
            else
            {
                drawSurface.drawingRectHelper.Enabled = false;
            }

        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

}
	





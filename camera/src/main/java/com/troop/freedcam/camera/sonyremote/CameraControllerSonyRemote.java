package com.troop.freedcam.camera.sonyremote;

import com.troop.freedcam.camera.basecamera.AbstractCameraController;
import com.troop.freedcam.camera.sonyremote.parameters.ParameterHandler;
import com.troop.freedcam.camera.sonyremote.parameters.modes.I_SonyApi;
import com.troop.freedcam.camera.sonyremote.sonystuff.JsonUtils;
import com.troop.freedcam.camera.sonyremote.sonystuff.ServerDevice;
import com.troop.freedcam.camera.sonyremote.sonystuff.SimpleCameraEventObserver;
import com.troop.freedcam.camera.sonyremote.sonystuff.SimpleRemoteApi;
import com.troop.freedcam.camera.sonyremote.sonystuff.SonyUtils;
import com.troop.freedcam.camera.sonyremote.sonystuff.WifiHandler;
import com.troop.freedcam.eventbus.EventBusHelper;
import com.troop.freedcam.eventbus.enums.CaptureStates;
import com.troop.freedcam.eventbus.events.CameraStateEvents;
import com.troop.freedcam.eventbus.events.CaptureStateChangedEvent;
import com.troop.freedcam.eventbus.events.UserMessageEvent;
import com.troop.freedcam.processor.RenderScriptProcessorInterface;
import com.troop.freedcam.settings.SettingKeys;
import com.troop.freedcam.settings.SettingsManager;
import com.troop.freedcam.utils.ContextApplication;
import com.troop.freedcam.utils.Log;

import org.greenrobot.eventbus.Subscribe;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class CameraControllerSonyRemote extends AbstractCameraController<
        ParameterHandler
        ,CameraHolderSony,FocusHandler,ModuleHandlerSony>
        implements WifiHandler.WifiEvents, CameraHolderSony.CameraRemoteEvents {

    private final String TAG = CameraControllerSonyRemote.class.getSimpleName();

    private ServerDevice serverDevice;

    private final int STATE_IDEL = 0;
    private final int STATE_DEVICE_CONNECTED = 3;
    private int STATE = STATE_IDEL;
    WifiHandler wifiHandler;
    private SimpleRemoteApi mRemoteApi;
    private SimpleCameraEventObserver mEventObserver;
    private final Set<String> mAvailableCameraApiSet = new HashSet<>();
    private PreviewStreamDrawer previewStreamDrawer;


    @Subscribe
    public void onCameraClose(CameraStateEvents.CameraCloseEvent message) {
        if(mEventObserver != null)
            mEventObserver.release();
    }

    @Subscribe
    public void onCameraError(CameraStateEvents.CameraErrorEvent error)
    {
        Log.d(TAG, "###################### onCamerError:"+ error + " ################################");
        EventBusHelper.post(new UserMessageEvent(error.msg,false));
        serverDevice = null;
        STATE = STATE_IDEL;
        mEventObserver.stop();
        previewStreamDrawer.stop();
        //setCameraEventListner(SonyCameraRemoteFragment.this);
        mainToCameraHandler.postDelayed(() -> startCameraAsync(),5000);

    }

    @Override
    public void onApiSetChanged(Set<String> mAvailableCameraApiSet) {
        ((ParameterHandler)parametersHandler).SetCameraApiSet(mAvailableCameraApiSet);
    }

    @Override
    public void createCamera() {
        previewStreamDrawer = new PreviewStreamDrawer(textureHolder.getTextureView(),renderScriptManager);

        //textView_wifi = view.findViewById(id.textView_wificonnect);

        wifiHandler = new WifiHandler(permissionManager);
        SettingsManager.getInstance().SetCurrentCamera(0);
        parametersHandler = new ParameterHandler(this, previewStreamDrawer);

        moduleHandler = new ModuleHandlerSony(this);
        focusHandler = new FocusHandler(this);
        ((ParameterHandler)parametersHandler).addApiChangedListner((I_SonyApi) focusHandler);
        cameraHolder = new CameraHolderSony(ContextApplication.getContext(), previewStreamDrawer, this);
        moduleHandler.initModules();
    }

    @Override
    public void initCamera() {

    }

    @Override
    public void startCamera() {
        startSonyCamera();
        Log.d(TAG, "onCameraOpen State:" + STATE);
        STATE = STATE_DEVICE_CONNECTED;
    }

    @Override
    public void stopCamera() {
        if (mEventObserver != null)
            mEventObserver.stop();
        if (cameraHolder !=  null)
            cameraHolder.CloseCamera();
        STATE = STATE_IDEL;
    }

    @Override
    public void restartCamera() {
        if (mEventObserver != null)
            mEventObserver.stop();
        cameraHolder.CloseCamera();
        STATE = STATE_IDEL;

        if (serverDevice == null)
        {
            wifiHandler.setEventsListner(this);
            wifiHandler.StartLookUp();
            return;
        }
        Log.d(TAG,"startCamera");

        startCameraAsync();
        Log.d(TAG, "onCameraOpen State:" + STATE);
        STATE = STATE_DEVICE_CONNECTED;
    }

    @Override
    public void startPreview() {

    }

    @Override
    public void stopPreview() {

    }


    private void startSonyCamera()
    {
        Log.d(TAG, "########################### start Camera ##########################");
        if (mRemoteApi == null)
        {
            mRemoteApi = new SimpleRemoteApi(serverDevice);
            ((ParameterHandler)parametersHandler).SetRemoteApi(mRemoteApi);

        }
        mEventObserver = new SimpleCameraEventObserver(ContextApplication.getContext(), mRemoteApi);


        ((CameraHolderSony)cameraHolder).setRemoteApi(mRemoteApi);
        ((CameraHolderSony)cameraHolder).cameraRemoteEventsListner =this;

       /* try {
            JSONObject replyJson;
            replyJson = mRemoteApi.getAccessMethodTypes();
            replyJson = mRemoteApi.getAccessVersions();
            replyJson = mRemoteApi.actEnableMethods("","","","");
            //result = {"result":[{"dg":"4b263abeeb922f3070f452553fb6bd9b04605d25928932922d2957ab092a4a99"}],"id":3}
            String dg =replyJson.getJSONArray("result").getJSONObject(0).getString("dg");
            String sg  = new Auth().SHA256(dg);
            replyJson = mRemoteApi.actEnableMethods(Auth.METHODS_TO_ENABLE,"Sony Corporation","7DED695E-75AC-4ea9-8A85-E5F8CA0AF2F3",sg);
            Log.d(TAG,replyJson.toString());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }*/

        try {
            JSONObject replyJson;
            // Get supported API list (Camera API)
            Log.d(TAG, "get event longpool false");
            replyJson =mRemoteApi.getAvailableApiList();
            JsonUtils.loadAvailableCameraApiList(replyJson, mAvailableCameraApiSet);
           /* replyJson = mRemoteApi.getEvent(false, "1.0");
            JSONArray resultsObj = replyJson.getJSONArray("result");
            JsonUtils.loadSupportedApiListFromEvent(resultsObj.getJSONObject(0), mAvailableCameraApiSet);*/
            ((ParameterHandler) parametersHandler).SetCameraApiSet(mAvailableCameraApiSet);


            if (JsonUtils.isApiSupported("startContShooting",mAvailableCameraApiSet))
                EventBusHelper.post(new CaptureStateChangedEvent(CaptureStates.cont_capture_stop_while_working));
            else if (JsonUtils.isApiSupported("stopContShooting",mAvailableCameraApiSet))
                EventBusHelper.post(new CaptureStateChangedEvent(CaptureStates.continouse_capture_start));
            else if (JsonUtils.isApiSupported("actTakePicture",mAvailableCameraApiSet))
                EventBusHelper.post(new CaptureStateChangedEvent(CaptureStates.image_capture_stop));
            else if (JsonUtils.isApiSupported("awaitTakePicture",mAvailableCameraApiSet))
                EventBusHelper.post(new CaptureStateChangedEvent(CaptureStates.image_capture_start));

            if (!JsonUtils.isApiSupported("setCameraFunction", mAvailableCameraApiSet) &&
                    !(JsonUtils.isApiSupported("startContShooting",mAvailableCameraApiSet) && JsonUtils.isApiSupported("stopContShooting",mAvailableCameraApiSet))) {
                // this device does not support setCameraFunction.
                // No need to check camera status.
                Log.d(TAG, "prepareOpenConnection->openconnection, no setCameraFunciton");
                openConnection();
            }
            else
            {
                // this device supports setCameraFunction.
                // after confirmation of camera state, open connection.
                Log.d(TAG, "this device support set camera function");

                if (!JsonUtils.isApiSupported("getEvent", mAvailableCameraApiSet)) {
                    Log.e(TAG, "this device is not support getEvent");
                    openConnection();
                    return;
                }

                // confirm current camera status
                String cameraStatus = null;
                replyJson = mRemoteApi.getEvent(false,"1.0");
                JSONArray resultsObj = replyJson.getJSONArray("result");
                JSONObject cameraStatusObj = resultsObj.getJSONObject(1);
                String type = cameraStatusObj.getString("type");
                if ("cameraStatus".equals(type)) {
                    cameraStatus = cameraStatusObj.getString("cameraStatus");

                    Log.d(TAG,"prepareOpenConnection camerastatusChanged" + cameraStatus );
                } else {
                    throw new IOException();
                }

                if (SonyUtils.isShootingStatus(cameraStatus)) {
                    Log.d(TAG, "camera function is Remote Shooting.");

                    openConnection();

                } else {
                    // set Listener
                    startOpenConnectionAfterChangeCameraState();
                    Log.d(TAG,"Change function to remote shooting");
                    // set Camera function to Remote Shooting
                    replyJson = mRemoteApi.setCameraFunction();
                    openConnection();
                }
            }
        } catch (IOException e) {
            Log.w(TAG, "prepareToStartContentsListMode: IOException: " + e.getMessage());

        } catch (JSONException e) {
            Log.w(TAG, "prepareToStartContentsListMode: JSONException: " + e.getMessage());

        }
    }

    private void startOpenConnectionAfterChangeCameraState() {
        Log.d(TAG, "startOpenConectiontAfterChangeCameraState() exec");

        mEventObserver.setCameraStateChangedListener(status -> {
            Log.d(TAG, "onCameraStatusChanged:" + status);
            if ("IDLE".equals(status)) {
                openConnection();
            }
        });

        mEventObserver.start();
    }

    private void openConnection()
    {
        Log.d(TAG, "########################### openConnection ##########################");
        Log.d(TAG, "openConnection(): exec.");



        try {
            JSONObject replyJson = null;

            replyJson = mRemoteApi.getCameraMethodTypes();
            Log.d(TAG,replyJson.toString());
            //find api version for requests
            replyJson = mRemoteApi.getVersions();
            JSONArray array = replyJson.getJSONArray("result");
            Log.d(TAG,array.toString());
            array = array.getJSONArray(0);
            String eventid = array.getString(array.length()-1);
            Log.d(TAG,"SetEventVersion:" +eventid);
            mEventObserver.setEventVersion(eventid);

            replyJson = mRemoteApi.getEvent(false, eventid);
            JSONArray resultsObj = replyJson.getJSONArray("result");
            mEventObserver.setEventChangeListener((ParameterHandler)parametersHandler);
            JsonUtils.loadSupportedApiListFromEvent(resultsObj.getJSONObject(0), mAvailableCameraApiSet);
            ((ParameterHandler) parametersHandler).SetCameraApiSet(mAvailableCameraApiSet);

            if (!mEventObserver.isActive())
                mEventObserver.activate();
            mEventObserver.processEvents(replyJson);

            // startRecMode if necessary.
            Log.d(TAG, "openConnection(): startRecMode");
            if (JsonUtils.isCameraApiAvailable("startRecMode", mAvailableCameraApiSet)) {
                Log.d(TAG, "openConnection(): startRecMode()");
                replyJson = mRemoteApi.startRecMode();

                // Call again.
                replyJson = mRemoteApi.getAvailableApiList();
                JsonUtils.loadAvailableCameraApiList(replyJson, mAvailableCameraApiSet);
                ((ParameterHandler) parametersHandler).SetCameraApiSet(mAvailableCameraApiSet);
            }


            Log.d(TAG, "openConnection(): setLiveViewFrameInfo");
            if(serverDevice != null &&(serverDevice.getFriendlyName().contains("ILCE-QX1") || serverDevice.getFriendlyName().contains("ILCE-QX30"))
                    && JsonUtils.isApiSupported("setLiveviewFrameInfo", (mAvailableCameraApiSet)) && parametersHandler.get(SettingKeys.FocusMode) != null)
            {
                if (!parametersHandler.get(SettingKeys.FocusMode).GetStringValue().equals("MF"))
                    ((CameraHolderSony) getCameraHolder()).SetLiveViewFrameInfo(true);
                else
                    ((CameraHolderSony) getCameraHolder()).SetLiveViewFrameInfo(false);
            }

            // Liveview start
            Log.d(TAG, "openConnection(): startLiveView");
            if (JsonUtils.isCameraApiAvailable("startLiveview", mAvailableCameraApiSet)) {
                Log.d(TAG, "openConnection(): LiveviewSurface.start()");
                cameraHolder.StartPreview();
            }

            // getEvent start
            Log.d(TAG, "openConnection(): getEvent");
            if (JsonUtils.isCameraApiAvailable("getEvent", mAvailableCameraApiSet)) {
                Log.d(TAG, "openConnection(): EventObserver.start()");
                if (!mEventObserver.isStarted())
                    mEventObserver.start();

            }

            Log.d(TAG, "openConnection(): completed.");
        } catch (IOException e) {
            Log.w(TAG, "openConnection : IOException: " + e.getMessage());

        } catch (JSONException e) {
            e.printStackTrace();
        }
        CameraStateEvents.fireCameraOpenFinishEvent();
    }

    @Override
    public void onDeviceFound(ServerDevice serverDevice) {
        this.serverDevice = serverDevice;
        wifiHandler.setEventsListner(null);
        startCameraAsync();
    }

    @Override
    public void onMessage(String msg) {
        EventBusHelper.post(new UserMessageEvent(msg,false));
    }

    public Set<String> getAvailableApiSet(){return mAvailableCameraApiSet;}

    public void stopEventObserver()
    {
        mEventObserver.stop();
        mEventObserver.clearEventChangeListener();
    }

    @Override
    public RenderScriptProcessorInterface getFocusPeakProcessor() {
        return previewStreamDrawer;
    }
}

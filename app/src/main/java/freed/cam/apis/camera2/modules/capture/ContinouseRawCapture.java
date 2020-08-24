package freed.cam.apis.camera2.modules.capture;

import android.graphics.ImageFormat;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.DngCreator;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.util.Size;
import androidx.annotation.RequiresApi;

import org.json.JSONArray;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.LinkedBlockingQueue;

import freed.ActivityInterface;
import freed.cam.apis.basecamera.modules.ModuleInterface;
import freed.cam.events.EventBusHelper;
import freed.cam.events.UserMessageEvent;
import freed.dng.DngProfile;
import freed.image.EmptyTask;
import freed.image.ImageManager;
import freed.image.ImageSaveTask;
import freed.image.ImageTask;
import freed.jni.ExifInfo;
import freed.jni.RawStack;
import freed.settings.SettingKeys;
import freed.settings.SettingsManager;
import freed.utils.Log;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class ContinouseRawCapture extends RawImageCapture {


    private StackRunner stackRunner;
    private final String TAG = ContinouseRawCapture.class.getSimpleName();
    private LinkedBlockingQueue<Image> imageBlockingQueue;
    public ContinouseRawCapture(Size size, int format, boolean setToPreview, ActivityInterface activityInterface, ModuleInterface moduleInterface, String file_ending,int max_images) {
        super(size, format, setToPreview,activityInterface,moduleInterface,file_ending,max_images);
        imageBlockingQueue = new LinkedBlockingQueue<>(max_images);
    }

    @Override
    public void onImageAvailable(ImageReader reader) {

        synchronized (imageBlockingQueue)
        {
            if(imageBlockingQueue.remainingCapacity() == 1) {
                try {
                    Log.d(TAG, "wait for queue free");
                    imageBlockingQueue.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        try {
            Log.d(TAG, "add new img to queue");
            imageBlockingQueue.put(reader.acquireLatestImage());
        } catch (InterruptedException | NullPointerException e) {
            e.printStackTrace();
        }
       synchronized (this)
        {
            createTask();
            this.notifyAll();
        }
    }

    @Override
    protected void createTask() {
        task = new EmptyTask();
    }

    public void startStack(int burstcount, int upshift)
    {
        Log.d(TAG, "start stack");
        if (stackRunner != null)
            stackRunner.stop();
        stackRunner = null;
        stackRunner = new StackRunner(burstcount,upshift);
        new Thread(stackRunner).start();
    }

    public void startStackALL(int bust)
    {
        StackAllRunner stackAllRunner = new StackAllRunner(bust);
        new Thread(stackAllRunner).start();
    }

    public void stopStack()
    {
        stackRunner.stop();
    }

    private class StackRunner implements Runnable
    {
        private final int burst;
        boolean run = false;
        long rawsize = 0;
        private final int upshift;
        private RawStack rawStack;
        int w;
        int h;
        public StackRunner(int burst, int upshift)
        {
            run = true;
            this.burst = burst;
            this.upshift = upshift;
        }

        public void stop(){ run = false; }

        @Override
        public void run() {
            Log.d(TAG, "start stack");
            rawStack = new RawStack();
            rawStack.setShift(upshift);
            Image image = null;
            int stackCoutn = 0;

                try {
                    image = imageBlockingQueue.take();
                } catch (InterruptedException e) {
                    Log.WriteEx(e);
                }
                ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                w = image.getWidth();
                h = image.getHeight();
                rawStack.setFirstFrame(buffer, w, h);
                image.close();
            synchronized (imageBlockingQueue) {
                imageBlockingQueue.notifyAll();
            }
            stackCoutn++;
            while (run && stackCoutn < burst) {

                try {
                        image = imageBlockingQueue.take();
                        Log.d(TAG, "take result");
                } catch (InterruptedException e) {
                    Log.WriteEx(e);
                }

                    buffer = image.getPlanes()[0].getBuffer();
                    Log.d(TAG, "stackframes");
                    rawStack.stackNextFrame(buffer);
                    image.close();
                    buffer.clear();
                synchronized (imageBlockingQueue) {
                    imageBlockingQueue.notifyAll();
                }
                stackCoutn++;
                EventBusHelper.post(new UserMessageEvent("Stacked:" +stackCoutn,false));
                Log.d(TAG, "stackframes done " + stackCoutn);
            }
            String f = getFilepath() + "_hdr_frames" + burst + file_ending;
            ImageTask task;
            task = process_rawWithDngConverter(rawStack.getOutputBuffer(), 6, new File(f), result, characteristics, w,h,activityInterface,moduleInterface,customMatrix,orientation,externalSD,toneMapProfile);
            ImageSaveTask itask = (ImageSaveTask)task;
            if (upshift > 0) {
                int bl = itask.getDngProfile().getBlacklvl();
                itask.getDngProfile().setBlackLevel(bl << upshift);
                int wl = itask.getDngProfile().getWhitelvl();
                itask.getDngProfile().setWhiteLevel(wl << upshift);
            }
            itask.getDngProfile().setRawType(DngProfile.S16bit_To_16bit);

            if (task != null) {
                ImageManager.putImageSaveTask(task);
                Log.d(TAG, "Put task to Queue");
            }
            rawStack.clear();
            rawStack = null;
            ContinouseRawCapture.this.stackRunner = null;
        }
    }



    private class StackAllRunner implements Runnable
    {

        private int burst;

        public StackAllRunner(int burst)
        {
            this.burst = burst;
        }

        @Override
        public void run() {
            //List<ByteBuffer> bufferList = new ArrayList<>();
            //List<Image> images = new ArrayList<>();
            RawStack rawStack = new RawStack();
            rawStack.setShift(0);
            int count = 0;
            int w = imageBlockingQueue.peek().getWidth();
            int h = imageBlockingQueue.peek().getHeight();
            Image img = null;
            if (imageBlockingQueue.peek() != null) {
                try {
                    img = imageBlockingQueue.take();
                    rawStack.setFirstFrame(img.getPlanes()[0].getBuffer(),img.getWidth(),img.getHeight(),burst);
                    img.close();
                    count++;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            while (imageBlockingQueue.peek() != null)
            {
                try {
                    img = imageBlockingQueue.take();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                rawStack.setNextFrame(img.getPlanes()[0].getBuffer());
                img.close();
                count++;
                //images.add(img);
                //bufferList.add(img.getPlanes()[0].getBuffer());
            }

            byte[] bytes = rawStack.stackAll();
            Log.d(TAG, "Stacked " + count +"/"+burst);

            String f = getFilepath() + "_hdr_frames" + burst + file_ending;
            ImageTask task;
            task = process_rawWithDngConverter(bytes, 6, new File(f), result, characteristics, w,h,activityInterface,moduleInterface,customMatrix,orientation,externalSD,toneMapProfile);
            if (task != null) {
                ImageManager.putImageSaveTask(task);
                Log.d(TAG, "Put task to Queue");
            }
            rawStack.clear();
        }
    }
}

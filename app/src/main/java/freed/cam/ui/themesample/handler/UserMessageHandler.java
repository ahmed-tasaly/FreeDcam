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

package freed.cam.ui.themesample.handler;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import freed.cam.apis.basecamera.CameraHolderEvent;
import freed.cam.apis.basecamera.Size;


/**
 * Created by troop on 04.10.2015.
 */
public class UserMessageHandler implements Runnable, CameraHolderEvent
{

    private Context context;
    private TextView messageTextView1;
    private LinearLayout messageHolder1;

    public UserMessageHandler(Context contextt)
    {
        this.context = contextt;
    }


    public void setMessageTextView(TextView messageTextView1, LinearLayout messageHolder1)
    {
        this.messageTextView1 = messageTextView1;
        this.messageHolder1 = messageHolder1;
    }

    public void sendMSG(String msg,boolean asToast)
    {
        messageHolder1.post(new Runnable() {
            @Override
            public void run() {
                setUserMessage(msg,asToast);
            }
        });

    }

    private void setUserMessage(String msg,boolean asToast)
    {
        if (asToast) {
            if (context != null)
                Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
        }
        else {
            if (messageHolder1 != null) {
                messageHolder1.removeCallbacks(this);
                messageHolder1.setVisibility(View.VISIBLE);
                if (messageTextView1 != null)
                    messageTextView1.setText(msg);
                messageHolder1.postDelayed(this, 3000);
            }
        }
    }

    @Override
    public void run()
    {
        if (messageHolder1 != null && messageTextView1 !=null) {
            messageTextView1.setText("");
            messageHolder1.setVisibility(View.GONE);
        }
    }

    @Override
    public void onCameraOpen() {

    }

    @Override
    public void onCameraOpenFinished() {

    }

    @Override
    public void onCameraClose() {

    }

    @Override
    public void onCameraError(String error) {
        setUserMessage(error,true);
    }

    @Override
    public void onCameraChangedAspectRatioEvent(Size size) {

    }
}

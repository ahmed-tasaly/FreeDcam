package freed.cam.ui.themenextgen.adapter;

import android.os.Parcelable;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import javax.inject.Inject;

import freed.FreedApplication;
import freed.cam.ui.EmptyFragment;
import freed.cam.ui.themenextgen.fragment.NextGenCameraSettingFragment;
import freed.cam.ui.themenextgen.fragment.NextGenCameraUiFragment;
import freed.settings.SettingKeys;
import freed.settings.SettingsManager;
import freed.viewer.screenslide.views.ScreenSlideFragment;

public class NextGenCameraUiSlidePagerAdapter extends FragmentStatePagerAdapter
{
    private final NextGenCameraSettingFragment settingsMenuFragment = new NextGenCameraSettingFragment();
    private final ScreenSlideFragment screenSlideFragment = new ScreenSlideFragment();
    private final NextGenCameraUiFragment cameraUiFragment = new NextGenCameraUiFragment();
    private final EmptyFragment emptyFragment = new EmptyFragment();

    ScreenSlideFragment.ButtonClick click;
    SettingsManager settingsManager;

    public NextGenCameraUiSlidePagerAdapter(FragmentManager fm, ScreenSlideFragment.ButtonClick click) {
        super(fm);
        this.click = click;
        settingsManager = FreedApplication.settingsManager();
    }

    @Override
    public Fragment getItem(int position) {
        if (position == 0) {
            return settingsMenuFragment;
        }
        else if (position == 2) {
            if (screenSlideFragment != null) {
                screenSlideFragment.setOnBackClickListner(click);
            }
            return screenSlideFragment;
        }
        else {
            if (settingsManager.get(SettingKeys.HIDE_CAMERA_UI).get())
                return emptyFragment;
            return cameraUiFragment;
        }
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Nullable
    @Override
    public Parcelable saveState() {
        return null;
    }
}
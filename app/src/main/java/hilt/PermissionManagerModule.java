package hilt;

import android.content.Context;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.components.ActivityComponent;
import dagger.hilt.android.qualifiers.ActivityContext;
import freed.utils.PermissionManager;

@Module
@InstallIn(ActivityComponent.class)
public class PermissionManagerModule {

    @Provides
    public PermissionManager permissionManager(@ActivityContext Context context)
    {
        return new PermissionManager(context);
    }
}

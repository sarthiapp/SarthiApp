package com.example.sarthi;

import android.app.Application;

import com.mapbox.mapboxsdk.MapmyIndia;
import com.mmi.services.account.MapmyIndiaAccountManager;

public class MapApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

       MapmyIndiaAccountManager.getInstance().setRestAPIKey("j6fwc3moyp8sttsnjh6ujm2osc2lvree");
        MapmyIndiaAccountManager.getInstance().setMapSDKKey("w5akjw9hls9qxdegtvaotoomzp77swkp");
        MapmyIndiaAccountManager.getInstance().setAtlasClientId("33OkryzDZsLz2lmTOKKh8gH7apaplrO3EXknMVxedv-quL2_rLH4OLVUtq5Zd23BMGh6BbS_YbYlhjigCfLg5bJ3dDK09N0b9-iSCeRSh-4McbpbDEUcug==");
        MapmyIndiaAccountManager.getInstance().setAtlasClientSecret("lrFxI-iSEg-MyQs5i0w6OqurUc_XlS9jTjx4ChXwrCaJFXR-UXHI-6Z3m5XkfR2tCzIb7K8vAiknygOlFuYhBxshnVVmsmU_8Fnp6LPe8jC3mC_-fMgRO2pm10eeelFQ");
        MapmyIndiaAccountManager.getInstance().setAtlasGrantType("client_credentials");
        MapmyIndia.getInstance(this);

        //Intent intent = new Intent(MapApplication.this, PickerActivity.class);
        //startActivity(intent);
    }

    public String getAtlasClientId() {
        return "";
    }

    public String getAtlasClientSecret() {
        return "";
    }


    public String getAtlasGrantType() {
        return "";
    }

    public String getMapSDKKey() {
        return "";
    }

    public String getRestAPIKey() {
        return "";
    }
}

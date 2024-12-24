package com.appier.ads.applovin;

import com.appier.ads.Appier;
import com.appier.mediation.applovin.BuildConfig;


public class AppierAdapterConfiguration {

    public AppierAdapterConfiguration() {}

    public static String getMediationVersion() {
        return BuildConfig.VERSION_NAME;
    }

    public static String getNetworkSdkVersion() {
        return Appier.getVersionName();
    }

    public static String getAdvertiserName() {
        return BuildConfig.APPIER_ADVERTISER_NAME;
    }
}
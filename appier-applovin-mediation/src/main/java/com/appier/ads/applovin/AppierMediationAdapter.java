package com.appier.ads.applovin;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.appier.ads.Appier;
import com.appier.ads.AppierAdUnitIdentifier;
import com.appier.ads.AppierBannerAd;
import com.appier.ads.AppierError;
import com.appier.ads.AppierInterstitialAd;
import com.appier.ads.AppierNativeAd;
import com.appier.ads.common.BrowserUtil;
import com.applovin.mediation.MaxAdFormat;
import com.applovin.mediation.adapter.MaxAdViewAdapter;
import com.applovin.mediation.adapter.MaxAdapterError;
import com.applovin.mediation.adapter.MaxInterstitialAdapter;
import com.applovin.mediation.adapter.MaxNativeAdAdapter;
import com.applovin.mediation.adapter.listeners.MaxAdViewAdapterListener;
import com.applovin.mediation.adapter.listeners.MaxInterstitialAdapterListener;
import com.applovin.mediation.adapter.listeners.MaxNativeAdAdapterListener;
import com.applovin.mediation.adapter.parameters.MaxAdapterInitializationParameters;
import com.applovin.mediation.adapter.parameters.MaxAdapterResponseParameters;
import com.applovin.mediation.adapters.MediationAdapterBase;
import com.applovin.mediation.nativeAds.MaxNativeAd;
import com.applovin.sdk.AppLovinSdk;

import org.json.JSONException;

import java.util.List;

public class AppierMediationAdapter extends MediationAdapterBase implements MaxInterstitialAdapter, MaxNativeAdAdapter, MaxAdViewAdapter {
    private AppierInterstitialAd appierInterstitialAd;
    private AppierBannerAd bannerAd;
    private AppierNativeAd nativeAd;

    public AppierMediationAdapter(AppLovinSdk appLovinSdk) {
        super(appLovinSdk);
    }

    protected static MaxAdapterError toMaxError(int errorCode, String errorMessage) {
        return new MaxAdapterError(errorCode, errorMessage);
    }

    protected static MaxAdapterError toMaxError(int errorCode, AppierError appierError) {
        try {
            return new MaxAdapterError(errorCode, appierError.toString());
        } catch (Exception e) {
            return new MaxAdapterError(errorCode, "Unknown Appier Error");
        }
    }

    private void AppierLog(String messages) {
        Appier.log("[AppierMediationAdapter]", messages);
    }

    @Override
    public void initialize(MaxAdapterInitializationParameters maxAdapterInitializationParameters, Activity activity, OnCompletionListener onCompletionListener) {
        onCompletionListener.onCompletion(InitializationStatus.INITIALIZED_SUCCESS, null);
    }

    @Override
    public String getSdkVersion() {
        return AppierAdapterConfiguration.getNetworkSdkVersion();
    }

    @Override
    public String getAdapterVersion() {
        return AppierAdapterConfiguration.getMediationVersion();
    }

    @Override
    public void onDestroy() {
        if (appierInterstitialAd != null) {
            appierInterstitialAd.destroy();
            appierInterstitialAd = null;
        }

        if (bannerAd != null) {
            bannerAd.destroy();
            bannerAd = null;
        }

        if (nativeAd != null) {
            nativeAd.destroy();
            nativeAd = null;
        }
    }

    @Override
    public void loadAdViewAd(MaxAdapterResponseParameters parameters, MaxAdFormat maxAdFormat, Activity activity, final MaxAdViewAdapterListener adapterListener) {
        String adUnitId = parameters.getAdUnitId();
        String placementId = parameters.getThirdPartyAdPlacementId();
        AppierLog("Load AdViewAd for placement id:" + placementId + ", type:" + maxAdFormat.getLabel() + "...");
        if (bannerAd != null) {
            AppierLog("BannerAd: isLoaded: " + bannerAd.isLoaded() + ", ZoneId:" + bannerAd.getZoneId());
        }
        bannerAd = new AppierBannerAd(activity.getApplicationContext(), new AppierAdUnitIdentifier(adUnitId), new AppierBannerAd.EventListener() {
            @Override
            public void onAdLoaded(AppierBannerAd appierBannerAd) {
                AppierLog("onAdLoaded");
                adapterListener.onAdViewAdLoaded(appierBannerAd.getView());
            }

            @Override
            public void onAdNoBid(AppierBannerAd appierBannerAd) {
                AppierLog("onAdNoBid");
                adapterListener.onAdViewAdLoadFailed(toMaxError(MaxAdapterError.ERROR_CODE_UNSPECIFIED, "Ad No Bid"));
            }

            @Override
            public void onAdLoadFail(AppierError appierError, AppierBannerAd appierBannerAd) {
                AppierLog("onAdLoadFail");
                adapterListener.onAdViewAdLoadFailed(toMaxError(MaxAdapterError.ERROR_CODE_INVALID_LOAD_STATE, appierError));
            }

            @Override
            public void onViewClick(AppierBannerAd appierBannerAd) {
                AppierLog("onViewClick");
                adapterListener.onAdViewAdClicked();
            }
        });
        bannerAd.setZoneId(placementId);
        bannerAd.loadAd();
    }

    @Override
    public void loadInterstitialAd(MaxAdapterResponseParameters parameters, Activity activity, final MaxInterstitialAdapterListener adapterListener) {
        String adUnitId = parameters.getAdUnitId();
        String placementId = parameters.getThirdPartyAdPlacementId();
        AppierLog("Load InterstitialAd for placement id:" + placementId + "...");

        appierInterstitialAd = new AppierInterstitialAd(activity.getApplicationContext(), new AppierAdUnitIdentifier(adUnitId), new AppierInterstitialAd.EventListener() {
            @Override
            public void onAdLoaded(AppierInterstitialAd appierInterstitialAd) {
                AppierLog("onAdLoaded");
                adapterListener.onInterstitialAdLoaded();
            }

            @Override
            public void onAdNoBid(AppierInterstitialAd appierInterstitialAd) {
                AppierLog("onAdNoBid");
                adapterListener.onInterstitialAdLoadFailed(toMaxError(MaxAdapterError.ERROR_CODE_NO_FILL, "Ad No Bid"));
            }

            @Override
            public void onAdLoadFail(AppierError appierError, AppierInterstitialAd appierInterstitialAd) {
                AppierLog("onAdLoadFail");
                adapterListener.onInterstitialAdLoadFailed(toMaxError(MaxAdapterError.ERROR_CODE_INVALID_LOAD_STATE, appierError));
            }

            @Override
            public void onViewClick(AppierInterstitialAd appierInterstitialAd) {
                AppierLog("onViewClick");
                adapterListener.onInterstitialAdClicked();
            }

            @Override
            public void onShown(AppierInterstitialAd appierInterstitialAd) {
                AppierLog("onShown");
                adapterListener.onInterstitialAdDisplayed();
            }

            @Override
            public void onShowFail(AppierError appierError, AppierInterstitialAd appierInterstitialAd) {
                AppierLog("onShowFail");
                adapterListener.onInterstitialAdDisplayFailed(toMaxError(MaxAdapterError.ERROR_CODE_AD_DISPLAY_FAILED, appierError));
            }

            @Override
            public void onDismiss(AppierInterstitialAd appierInterstitialAd) {
                AppierLog("onDismiss");
                adapterListener.onInterstitialAdHidden();
            }
        });
        appierInterstitialAd.setZoneId(placementId);
        appierInterstitialAd.loadAd();
    }

    @Override
    public void showInterstitialAd(MaxAdapterResponseParameters parameters, Activity activity, MaxInterstitialAdapterListener adapterListener) {
        String placementId = parameters.getThirdPartyAdPlacementId();
        AppierLog("Show InterstitialAd for placement id:" + placementId + "...");
        if (appierInterstitialAd.isLoaded()) {
            appierInterstitialAd.showAd();
        } else {
            AppierLog("Interstitial Ad is not loaded");
            adapterListener.onInterstitialAdDisplayFailed(MaxAdapterError.AD_NOT_READY);
        }
    }

    @Override
    public void loadNativeAd(MaxAdapterResponseParameters parameters, Activity activity, final MaxNativeAdAdapterListener adapterListener) {
        String adUnitId = parameters.getAdUnitId();
        String placementId = parameters.getThirdPartyAdPlacementId();
        final Context context = activity.getApplicationContext();
        AppierLog("Load Native Ad for placement id:" + placementId + "...");

        nativeAd = new AppierNativeAd(activity.getApplicationContext(), new AppierAdUnitIdentifier(adUnitId), new AppierNativeAd.EventListener() {
            @Override
            public void onAdLoaded(AppierNativeAd appierNativeAd) {
                AppierLog("onAdLoaded" + ", network host:" + appierNativeAd.getNetworkHost());
                nativeAd = appierNativeAd;
                try {
                    final MaxNativeAd.Builder builder = new MaxNativeAd.Builder()
                            .setAdvertiser(AppierAdapterConfiguration.getAdvertiserName())
                            .setAdFormat(MaxAdFormat.NATIVE)
                            .setTitle(appierNativeAd.getTitle())
                            .setBody(appierNativeAd.getText())
                            .setCallToAction(appierNativeAd.getCallToActionText());

                    ImageView main = new ImageView(context);
                    ImageView icon = new ImageView(context);
                    ImageView option = new ImageView(context);
                    appierNativeAd.setupAdImages(main, icon, option);

                    builder.setMediaView(main);
                    builder.setIcon(new MaxNativeAd.MaxNativeAdImage(icon.getDrawable()));
                    builder.setOptionsView(option);

                    MaxAppierNativeAd maxAppierNativeAd = new MaxAppierNativeAd(builder, context, adapterListener);
                    adapterListener.onNativeAdLoaded(maxAppierNativeAd, null);
                } catch (JSONException e) {
                    adapterListener.onNativeAdLoadFailed(toMaxError(MaxAdapterError.ERROR_CODE_MISSING_REQUIRED_NATIVE_AD_ASSETS, "Fail to load images:" + e.getMessage()));
                }
            }

            @Override
            public void onAdNoBid(AppierNativeAd appierNativeAd) {
                AppierLog("onAdNoBid");
                adapterListener.onNativeAdLoadFailed(toMaxError(MaxAdapterError.ERROR_CODE_UNSPECIFIED, "Ad No Bid"));
            }

            @Override
            public void onAdLoadFail(AppierError appierError, AppierNativeAd appierNativeAd) {
                AppierLog("onAdLoadFail");
                adapterListener.onNativeAdLoadFailed(toMaxError(MaxAdapterError.ERROR_CODE_INVALID_LOAD_STATE, appierError));
            }

            @Override
            public void onAdShown(AppierNativeAd appierNativeAd) {
                AppierLog("onAdShown");
                adapterListener.onNativeAdDisplayed(null);
            }

            @Override
            public void onImpressionRecorded(AppierNativeAd appierNativeAd) {
                AppierLog("onImpressionRecorded");
            }

            @Override
            public void onImpressionRecordFail(AppierError appierError, AppierNativeAd appierNativeAd) {
                AppierLog("onImpressionRecorded");
            }

            @Override
            public void onAdClick(AppierNativeAd appierNativeAd) {
                AppierLog("onAdClick");
                adapterListener.onNativeAdClicked();
            }

            @Override
            public void onAdClickFail(AppierError appierError, AppierNativeAd appierNativeAd) {
                AppierLog("onAdClickFail");
            }
        });
        nativeAd.setZoneId(placementId);
        nativeAd.loadAdWithExternalCache();
    }

    // Helper class to map AppierNativeAd to MaxNativeAd
    private class MaxAppierNativeAd
            extends MaxNativeAd {

        final MaxNativeAdAdapterListener listener;
        final Context context;
        final BrowserUtil browserUtil;

        private MaxAppierNativeAd(final Builder builder, Context context, MaxNativeAdAdapterListener listener) {
            super(builder);
            this.listener = listener;
            this.context = context;

            browserUtil = new BrowserUtil(this.context);
            if (Appier.getBrowserAgent() == Appier.BrowserAgent.NATIVE) {
                browserUtil.disableInternalBrowser();
            } else {
                browserUtil.enableInternalBrowser();
            }
        }

        @Override
        public boolean prepareForInteraction(final List<View> clickableViews, final ViewGroup container) {
            final AppierNativeAd appierNativeAd = nativeAd;

            if (nativeAd == null) {
                Appier.log("Failed to register native ad views: Native ad is null.");
                return false;
            }

            if (container == null) {
                Appier.log("Failed to register native ad views: Container is null.");
                return false;
            }

            // To avoid `java.lang.IllegalArgumentException: Invalid set of clickable views` with size=0
            if (clickableViews.isEmpty()) {
                Appier.log("Failed to register native ad views: No clickable views to prepare");
                return false;
            }

            // Add media view to clickableViews if exists
            if (getMediaView() != null) {
                clickableViews.add(getMediaView());
            }

            // Bind view events
            appierNativeAd.bindAdViewInteractions(container, getOptionsView(), clickableViews);
            return true;
        }
    }
}

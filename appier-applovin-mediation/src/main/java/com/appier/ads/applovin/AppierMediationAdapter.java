package com.appier.ads.applovin;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
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
import com.appier.ads.common.ConsentStatus;
import com.appier.ads.common.ImageLoader;
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

import java.util.Arrays;
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
        Appier.setTestMode(Appier.TestMode.BID);
        Appier.setGDPRApplies(true);
        Appier.setConsentStatus(ConsentStatus.EXPLICIT_YES);
        Appier.setCoppaApplies(true);
        Appier.setBrowserAgent(Appier.BrowserAgent.NATIVE);

        onCompletionListener.onCompletion(InitializationStatus.INITIALIZED_SUCCESS, null);
    }

    @Override
    public String getSdkVersion() {
        return Appier.getVersionName();
    }

    @Override
    public String getAdapterVersion() {
        return "1.0.0";
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

                    String iconImageUrl = appierNativeAd.getIconImageUrl();
                    String mainImageUrl = appierNativeAd.getMainImageUrl();
                    String optionImageUrl = appierNativeAd.getPrivacyInformationIconImageUrl();

                    ImageLoader imageLoader = new ImageLoader(context);
                    imageLoader.batchLoadImages(Arrays.asList(mainImageUrl, iconImageUrl, optionImageUrl), new ImageLoader.OnBatchImageEventListenerWithResult() {
                        @Override
                        public void onBatchImageLoadedAndCached(List<Drawable> drawables) {
                            Drawable mainImageDrawable = drawables.get(0);
                            Drawable iconImageDrawable = drawables.get(1);
                            Drawable optionImageDrawable = drawables.get(2);
                            // The main image must be set by setMediaView
                            if (mainImageDrawable != null) {
                                ImageView imageView = new ImageView(context);
                                imageView.setImageDrawable(mainImageDrawable);
                                imageView.setAdjustViewBounds(true);
                                builder.setMediaView(imageView);
                            } else {
                                AppierLog("Drawable for media view is null");
                            }

                            if (iconImageDrawable != null) {
                                builder.setIcon(new MaxNativeAd.MaxNativeAdImage(iconImageDrawable));
                            } else {
                                AppierLog("Drawable for icon is null");
                            }

                            if (optionImageDrawable != null) {
                                ImageView imageView = new ImageView(context);
                                imageView.setImageDrawable(optionImageDrawable);
                                imageView.setBackgroundColor(Color.TRANSPARENT);
                                builder.setOptionsView(imageView);
                            }
                            else {
                                AppierLog("Drawable for option view is null");
                            }

                            MaxAppierNativeAd maxAppierNativeAd = new MaxAppierNativeAd(builder, context, adapterListener);
                            adapterListener.onNativeAdLoaded(maxAppierNativeAd, null);
                        }

                        @Override
                        public void onBatchImageLoadFail() {
                            adapterListener.onNativeAdLoadFailed(toMaxError(MaxAdapterError.ERROR_CODE_MISSING_REQUIRED_NATIVE_AD_ASSETS, "Fail to load images"));
                        }
                    });
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

    // helper class to map AppierNativeAd to MaxNativeAd
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
                Appier.log("Failed to register native ad views: native ad is null.");
                return false;
            }

            // To avoid `java.lang.IllegalArgumentException: Invalid set of clickable views` with size=0
            if (clickableViews.isEmpty()) {
                Appier.log("No clickable views to prepare");
                return false;
            }

            // Add media view to clickableViews if exists
            if (getMediaView() != null) {
                clickableViews.add(getMediaView());
            }

            // Setup option view click action if exists
            if (getOptionsView() != null) {
                getOptionsView().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            if (browserUtil.tryToOpenUrl(appierNativeAd.getPrivacyInformationIconClickThroughUrl())) {
                                listener.onNativeAdClicked();
                            }
                        } catch (JSONException e) {
                            AppierLog("Fail to open privacy information url when native ad clicked");
                        }
                    }
                });
            }

            // setup click action for other clickable views
            for (final View clickableView : clickableViews) {
                clickableView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            if (browserUtil.tryToOpenUrl(appierNativeAd.getClickDestinationUrl())) {
                                listener.onNativeAdClicked();
                            }
                        } catch (JSONException e) {
                            AppierLog("Fail to open url when native ad clicked");
                        }
                    }
                });
            }
            return true;
        }
    }
}

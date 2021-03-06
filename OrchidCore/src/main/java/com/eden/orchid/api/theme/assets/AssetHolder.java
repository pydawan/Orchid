package com.eden.orchid.api.theme.assets;

import java.util.List;

public interface AssetHolder {

    AssetHolder getAssetHolder();

    void addAssets();

    default void withNamespace(String namespace, Runnable cb) {
        getAssetHolder().withNamespace(namespace, cb);
    }

    default AssetPage addJs(AssetPage jsAsset) {
        return getAssetHolder().addJs(jsAsset);
    }

    default AssetPage addJs(String jsAsset) {
        return getAssetHolder().addJs(jsAsset);
    }

    default AssetPage addCss(AssetPage cssAsset) {
        return getAssetHolder().addCss(cssAsset);
    }

    default AssetPage addCss(String cssAsset) {
        return getAssetHolder().addCss(cssAsset);
    }

    default List<AssetPage> getScripts() {
        return getAssetHolder().getScripts();
    }

    default List<AssetPage> getStyles() {
        return getAssetHolder().getStyles();
    }

    default void flushJs() {
        getAssetHolder().flushJs();
    }

    default void flushCss() {
        getAssetHolder().flushCss();
    }
    
    default void clearAssets() {
        getAssetHolder().clearAssets();
    }

    default boolean shouldDownloadExternalAssets() {
        return getAssetHolder().shouldDownloadExternalAssets();
    }
}

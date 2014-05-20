package com.hust.software;

import com.intellij.CommonBundle;
import com.intellij.reference.SoftReference;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.PropertyKey;

import java.lang.ref.Reference;
import java.util.ResourceBundle;

/**
 * Used for support i18n
 * Created by Yan Yu on 2014-05-19.
 */
public class LogAnalysisBundle {

    private static Reference<ResourceBundle> bundleRef;

    @NonNls
    private static final String BUNDLE = "logAnalysis";

    private LogAnalysisBundle() {
    }

    public static String message(String key, Object... params) {
        return CommonBundle.message(getBundle(), key, params);
    }

    private static ResourceBundle getBundle() {
        ResourceBundle bundle = null;
        if (LogAnalysisBundle.bundleRef != null) {
            bundle = LogAnalysisBundle.bundleRef.get();
        }

        if (bundle == null) {
            bundle = ResourceBundle.getBundle(BUNDLE);
            LogAnalysisBundle.bundleRef = new SoftReference<>(bundle);
        }
        return bundle;
    }
}

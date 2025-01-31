/*
 * Copyright (C) 2024 Project-Pixelstar
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.star.field.category;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserHandle;
import android.provider.SearchIndexableResource;
import android.provider.Settings;
import android.view.View;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreferenceCompat;

import com.android.internal.logging.nano.MetricsProto;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.Indexable;
import com.android.settingslib.search.SearchIndexable;

import com.android.internal.util.pixelstar.ThemeUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lineageos.preference.LineageSecureSettingListPreference;
import lineageos.preference.LineageSecureSettingSwitchPreference;
import lineageos.preference.LineageSystemSettingListPreference;
import lineageos.providers.LineageSettings;

@SearchIndexable
public class QuickSettings extends SettingsPreferenceFragment 
            implements Preference.OnPreferenceChangeListener {

    private static final String KEY_INTERFACE_CATEGORY = "quick_settings_interface_category";
    private static final String KEY_QUICK_PULLDOWN = "qs_quick_pulldown";
    private static final String KEY_SHOW_BRIGHTNESS_SLIDER = "qs_show_brightness_slider";
    private static final String KEY_BRIGHTNESS_SLIDER_POSITION = "qs_brightness_slider_position";
    private static final String KEY_SHOW_AUTO_BRIGHTNESS = "qs_show_auto_brightness";

    private static final int PULLDOWN_DIR_NONE = 0;
    private static final int PULLDOWN_DIR_RIGHT = 1;
    private static final int PULLDOWN_DIR_LEFT = 2;
    private static final int PULLDOWN_DIR_BOTH = 3;

    private static final String KEY_QS_SPLIT_SHADE = "qs_split_shade";

    private static final String QS_SPLIT_SHADE_LAYOUT_CTG = "android.theme.customization.qs_landscape_layout";
    private static final String QS_SPLIT_SHADE_LAYOUT_PKG = "com.android.systemui.qs.landscape.split_shade_layout";
    private static final String QS_SPLIT_SHADE_LAYOUT_TARGET = "com.android.systemui";
    private static final String QS_SPLIT_SHADE_CUTOUT_CTG = "android.theme.customization.qs_landscape_cutout";
    private static final String QS_SPLIT_SHADE_CUTOUT_PKG = "android.landscape.split_shade_cutout";
    private static final String QS_SPLIT_SHADE_CUTOUT_TARGET = "android";

    private SwitchPreferenceCompat mSplitShade;

    private ThemeUtils mThemeUtils;

    private PreferenceCategory mInterfaceCategory;
    private LineageSecureSettingListPreference mShowBrightnessSlider;
    private LineageSecureSettingListPreference mBrightnessSliderPosition;
    private LineageSecureSettingSwitchPreference mShowAutoBrightness;

    private LineageSystemSettingListPreference mQuickPulldown;
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.category_quicksettings);
        PreferenceScreen prefSet = getPreferenceScreen();
        mThemeUtils = new ThemeUtils(getContext());

        final Context context = getContext();
        final ContentResolver resolver = context.getContentResolver();
        final Resources res = getResources();
        final PreferenceScreen prefScreen = getPreferenceScreen();

        mQuickPulldown =
                (LineageSystemSettingListPreference) findPreference(KEY_QUICK_PULLDOWN);
        mQuickPulldown.setOnPreferenceChangeListener(this);
        updateQuickPulldownSummary(mQuickPulldown.getIntValue(0));

        mShowBrightnessSlider = (LineageSecureSettingListPreference) findPreference(KEY_SHOW_BRIGHTNESS_SLIDER);
        mBrightnessSliderPosition = (LineageSecureSettingListPreference) findPreference(KEY_BRIGHTNESS_SLIDER_POSITION);
        mShowAutoBrightness = (LineageSecureSettingSwitchPreference) findPreference(KEY_SHOW_AUTO_BRIGHTNESS);

        mShowBrightnessSlider.setOnPreferenceChangeListener(this);
        boolean showSlider = LineageSettings.Secure.getIntForUser(resolver,
                LineageSettings.Secure.QS_SHOW_BRIGHTNESS_SLIDER, 1, UserHandle.USER_CURRENT) > 0;

        mBrightnessSliderPosition.setEnabled(showSlider);

        boolean autoBrightnessAvailable = res.getBoolean(
                com.android.internal.R.bool.config_automatic_brightness_available);
        if (autoBrightnessAvailable) {
            mShowAutoBrightness.setEnabled(showSlider);
        } else {
            mInterfaceCategory.removePreference(mShowAutoBrightness);
        }

        if (getResources().getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
            mQuickPulldown.setEntries(R.array.status_bar_quick_pull_down_entries_rtl);
            mQuickPulldown.setEntryValues(R.array.status_bar_quick_pull_down_values_rtl);
        }

        mSplitShade = findPreference(KEY_QS_SPLIT_SHADE);
        boolean ssEnabled = isSplitShadeEnabled();
        mSplitShade.setChecked(ssEnabled);
        mSplitShade.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        final Context context = getContext();
        final ContentResolver resolver = context.getContentResolver();
        if (preference == mQuickPulldown) {
            int value = Integer.parseInt((String) newValue);
            updateQuickPulldownSummary(value);
            return true;
        } else if (preference == mShowBrightnessSlider) {
            int value = Integer.parseInt((String) newValue);
            mBrightnessSliderPosition.setEnabled(value > 0);
            if (mShowAutoBrightness != null)
                mShowAutoBrightness.setEnabled(value > 0);
            return true;
        } else if (preference == mSplitShade) {
            updateSplitShadeState((Boolean) newValue);
            return true;
        }
        return false;
    }

    private boolean isSplitShadeEnabled() {
        return mThemeUtils.isOverlayEnabled(QS_SPLIT_SHADE_LAYOUT_PKG)
            && mThemeUtils.isOverlayEnabled(QS_SPLIT_SHADE_CUTOUT_PKG);
    }

    private void updateSplitShadeState(boolean enable) {
        mThemeUtils.setOverlayEnabled(
                QS_SPLIT_SHADE_LAYOUT_CTG,
                enable ? QS_SPLIT_SHADE_LAYOUT_PKG : QS_SPLIT_SHADE_LAYOUT_TARGET,
                QS_SPLIT_SHADE_LAYOUT_TARGET);

        mThemeUtils.setOverlayEnabled(
                QS_SPLIT_SHADE_CUTOUT_CTG,
                enable ? QS_SPLIT_SHADE_CUTOUT_PKG : QS_SPLIT_SHADE_CUTOUT_TARGET,
                QS_SPLIT_SHADE_CUTOUT_TARGET);
    }

    public static void reset(Context mContext) {
        ContentResolver resolver = mContext.getContentResolver();
        ThemeUtils themeUtils = new ThemeUtils(mContext);
        themeUtils.setOverlayEnabled(QS_SPLIT_SHADE_LAYOUT_CTG, QS_SPLIT_SHADE_LAYOUT_TARGET, QS_SPLIT_SHADE_LAYOUT_TARGET);
        themeUtils.setOverlayEnabled(QS_SPLIT_SHADE_CUTOUT_CTG, QS_SPLIT_SHADE_CUTOUT_TARGET, QS_SPLIT_SHADE_CUTOUT_TARGET);
    }

    private void updateQuickPulldownSummary(int value) {
        String summary = "";
        switch (value) {
            case PULLDOWN_DIR_NONE:
                summary = getResources().getString(
                    R.string.status_bar_quick_pull_down_off);
                break;
            case PULLDOWN_DIR_RIGHT:
            case PULLDOWN_DIR_LEFT:
            case PULLDOWN_DIR_BOTH:
                summary = getResources().getString(
                    R.string.status_bar_quick_pull_down_summary,
                    getResources().getString(
                        value == PULLDOWN_DIR_RIGHT
                            ? R.string.status_bar_quick_pull_down_right
                            : value == PULLDOWN_DIR_LEFT
                                ? R.string.status_bar_quick_pull_down_left
                                : R.string.status_bar_quick_pull_down_both
                    )
                );
                break;
        }
        mQuickPulldown.setSummary(summary);
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.STARFIELD;
    }
    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {
                @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(
                        Context context, boolean enabled) {
                    final SearchIndexableResource sir = new SearchIndexableResource(context);
                    sir.xmlResId = R.xml.category_misc;
                    return Arrays.asList(sir);
                }

                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    final List<String> keys = super.getNonIndexableKeys(context);
                    final Resources res = context.getResources();
                    boolean autoBrightnessAvailable = res.getBoolean(
                            com.android.internal.R.bool.config_automatic_brightness_available);
                    if (!autoBrightnessAvailable) {
                        keys.add(KEY_SHOW_AUTO_BRIGHTNESS);
                    }
                    return keys;
                }
            };
}
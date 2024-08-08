/*
 * Copyright (C) 2017-2024 crDroid Android Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.star.field.fragments;

import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.os.UserHandle;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.SwitchPreferenceCompat;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.R;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settingslib.search.SearchIndexable;

import java.util.List;

import lineageos.providers.LineageSettings;

import com.android.settings.custom.preference.CustomSeekBarPreference;

@SearchIndexable
public class NetworkTrafficSettings extends SettingsPreferenceFragment
        implements Preference.OnPreferenceChangeListener  {

    private static final String TAG = "NetworkTrafficSettings";

    private CustomSeekBarPreference mNetTrafficAutohideThreshold;
    private CustomSeekBarPreference mNetTrafficRefreshInterval;
    private ListPreference mNetTrafficLocation;
    private ListPreference mNetTrafficMode;
    private ListPreference mNetTrafficUnits;
    private SwitchPreferenceCompat mNetTrafficAutohide;
    private SwitchPreferenceCompat mNetTrafficHideArrow;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.network_traffic_settings);
        final ContentResolver resolver = getActivity().getContentResolver();

        mNetTrafficAutohideThreshold = (CustomSeekBarPreference)
                findPreference(LineageSettings.Secure.NETWORK_TRAFFIC_AUTOHIDE_THRESHOLD);
        mNetTrafficRefreshInterval = (CustomSeekBarPreference)
                findPreference(LineageSettings.Secure.NETWORK_TRAFFIC_REFRESH_INTERVAL);
        mNetTrafficLocation = (ListPreference)
                findPreference(LineageSettings.Secure.NETWORK_TRAFFIC_LOCATION);
        mNetTrafficLocation.setOnPreferenceChangeListener(this);
        mNetTrafficMode = (ListPreference)
                findPreference(LineageSettings.Secure.NETWORK_TRAFFIC_MODE);
        mNetTrafficAutohide = (SwitchPreferenceCompat)
                findPreference(LineageSettings.Secure.NETWORK_TRAFFIC_AUTOHIDE);
        mNetTrafficUnits = (ListPreference)
                findPreference(LineageSettings.Secure.NETWORK_TRAFFIC_UNITS);
        mNetTrafficHideArrow = (SwitchPreferenceCompat)
                findPreference(LineageSettings.Secure.NETWORK_TRAFFIC_HIDEARROW);

        int location = LineageSettings.Secure.getIntForUser(resolver,
                LineageSettings.Secure.NETWORK_TRAFFIC_LOCATION, 0, UserHandle.USER_CURRENT);
        updateEnabledStates(location);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mNetTrafficLocation) {
            int location = Integer.valueOf((String) newValue);
            updateEnabledStates(location);
            return true;
        }
        return false;
    }

    private void updateEnabledStates(int location) {
        final boolean enabled = location != 0;
        mNetTrafficMode.setEnabled(enabled);
        mNetTrafficAutohide.setEnabled(enabled);
        mNetTrafficAutohideThreshold.setEnabled(enabled);
        mNetTrafficHideArrow.setEnabled(enabled);
        mNetTrafficRefreshInterval.setEnabled(enabled);
        mNetTrafficUnits.setEnabled(enabled);
    }

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.STARFIELD;
    }

    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
        new BaseSearchIndexProvider(R.xml.network_traffic_settings) {

            @Override
            public List<String> getNonIndexableKeys(Context context) {
                List<String> keys = super.getNonIndexableKeys(context);
                return keys;
            }
        };
}

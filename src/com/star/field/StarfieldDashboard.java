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

package com.star.field;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.view.MotionEvent; 

import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settingslib.widget.LayoutPreference;
import com.google.android.material.card.MaterialCardView;
import com.android.internal.logging.nano.MetricsProto;

public class StarfieldDashboard extends SettingsPreferenceFragment implements View.OnClickListener {

    private LayoutPreference mPreference;
    private MaterialCardView lsclock;
    private LinearLayout themes, fonts;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.starfield_dashboard);

        mPreference = findPreference("starfield_header");
        lsclock = mPreference.findViewById(R.id.wallpaper);
        themes = mPreference.findViewById(R.id.theme);
        fonts = mPreference.findViewById(R.id.fonts);

    // Set touch listener to prevent the entire header from being clickable
    View headerView = mPreference.findViewById(R.id.header_root);
    if (headerView != null) {
        headerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true; // Intercept touch events
            }
        });
    }

        lsclock.setOnClickListener(this);
        themes.setOnClickListener(this);
        fonts.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == lsclock) {
            openGoogleWallpaperApp();
        } else if (v == themes) {
            startActivity("ThemeActivity");
        } else if (v == fonts) {
            startActivity("FontPickerPreviewActivity");
        }
    }

    private void startActivity(String activity) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setClassName("com.android.settings", "com.android.settings.Settings$" + activity);
        getContext().startActivity(intent);
    }

	private void openGoogleWallpaperApp() {
		Intent intent = new Intent();
		// Trying known activity names
		String[] possibleActivities = {
			"com.google.android.apps.wallpaper.picker.CategoryPickerActivity",
			"com.google.android.apps.wallpaper.category.CategorySelectorActivity"
		};

		boolean found = false;
		for (String activity : possibleActivities) {
			intent.setClassName("com.google.android.apps.wallpaper", activity);
			if (intent.resolveActivity(getContext().getPackageManager()) != null) {
				getContext().startActivity(intent);
				found = true;
				break;
			}
		}

		if (!found) {
			// Handle the case where the Google Wallpaper app is not installed or no activity is found
			Toast.makeText(getContext(), "Google Wallpaper app is not installed", Toast.LENGTH_SHORT).show();
		}
	}


    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.STARFIELD;
    }

    @Override
    public RecyclerView onCreateRecyclerView(LayoutInflater inflater, ViewGroup container, Bundle icicle) {
        RecyclerView rcv = super.onCreateRecyclerView(inflater, container, icicle);
        GridLayoutManager layoutG = new GridLayoutManager(getActivity(), 2);
        layoutG.setSpanSizeLookup(new SpanSizeLookupG());
        rcv.setLayoutManager(layoutG);
        return rcv;
    }

    class SpanSizeLookupG extends GridLayoutManager.SpanSizeLookup {
        @Override
        public int getSpanSize(int position) {
            if (position == 0 || position == 1 || position == 6) {
                return 2;
            } else {
                return 1;
            }
        }
    }
}

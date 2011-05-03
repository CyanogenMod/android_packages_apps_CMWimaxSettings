/*
 * Copyright (C) 2010 The CyanogenMod Project
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

package com.android.settings.wimax;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wimax.WimaxHelper;
import android.net.wimax.WimaxManagerConstants;
import android.os.Bundle;
import android.os.SystemProperties;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.view.Menu;
import android.widget.Toast;

import java.util.WeakHashMap;

/**
 * Settings screen for Wimax. This will be launched from the main system settings.
 */
public class WimaxSettings extends PreferenceActivity {

    private static final String TAG = "WimaxSettings";

    //============================
    // Preference/activity member variables
    //============================

    private static final String KEY_WIMAX_ENABLED = "wimax_enabled";

    private static final String KEY_WIMAX_SCAN = "wimax_scan";

    private CheckBoxPreference mWimaxEnabled;
    private WimaxEnabler mWimaxEnabler;

    private static final String KEY_MAC_ADDRESS = "mac_address";
    private static final String KEY_SW_VERSION = "sw_version";
    private static final String KEY_IP_ADDRESS = "ip_address";
    private static final String KEY_GATEWAY = "gateway";
    private static final String KEY_SIG_STR_SIMPLE = "signal_strength_simple";
    //private static final String KEY_SIG_STR_DB = "signal_strength_db";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onCreatePreferences();

        IntentFilter filter = new IntentFilter(WimaxManagerConstants.RSSI_CHANGED_ACTION);
        this.registerReceiver(mIntentReceiver, filter);
        //IntentFilter filter2 = new IntentFilter(WimaxConstants.NETWORK_STATE_CHANGED_ACTION);
        //this.registerReceiver(mIntentReceiver, filter2);

        refreshAll();
    }

    private void onCreatePreferences() {
        addPreferencesFromResource(R.xml.wimax_settings);

        final PreferenceScreen preferenceScreen = getPreferenceScreen();

        mWimaxEnabled = (CheckBoxPreference) preferenceScreen.findPreference(KEY_WIMAX_ENABLED);
        mWimaxEnabler = new WimaxEnabler(this, mWimaxEnabled);

        registerForContextMenu(getListView());
    }

    @Override
    protected void onResume() {
        IntentFilter filter = new IntentFilter(WimaxManagerConstants.RSSI_CHANGED_ACTION);
        this.registerReceiver(mIntentReceiver, filter);
        //IntentFilter filter2 = new IntentFilter(WimaxConstants.NETWORK_STATE_CHANGED_ACTION);
        //this.registerReceiver(mIntentReceiver, filter2);

        refreshAll();
        mWimaxEnabler.resume();
        super.onResume();
    }

    @Override
    protected void onPause() {
        this.unregisterReceiver(mIntentReceiver);
        mWimaxEnabler.pause();
        super.onPause();
    }

    //============================
    // Preference callbacks
    //============================

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (KEY_WIMAX_SCAN.equals(preference.getKey())) {
            WimaxHelper.wimaxRescan(this);
        }
        super.onPreferenceTreeClick(preferenceScreen, preference);
        return false;
    }

    //============================
    // Wimax callbacks
    //============================

    public void onError(int messageResId) {
        Toast.makeText(this, messageResId, Toast.LENGTH_LONG).show();
    }

    private void refreshAll() {
        refreshDeviceInfo();
        refreshIPInfo();
    }

    private void refreshDeviceInfo() {
        //Preference wimaxSignalStrengthRSSIPref = findPreference(KEY_SIG_STR_SIMPLE);
        //wimaxSignalStrengthRSSIPref.setSummary(getString(R.string.status_unavailable));

        //Preference wimaxSignalStrengthDBPref = findPreference(KEY_SIG_STR_DB);
        //wimaxSignalStrengthDBPref.setSummary("Level: " + getString(R.string.status_unavailable));

        Preference wimaxMacAddressPref = findPreference(KEY_MAC_ADDRESS);
        String macAddress = SystemProperties.get("persist.wimax.0.MAC", getString(R.string.status_unavailable));
        wimaxMacAddressPref.setSummary(macAddress);

        Preference wimaxSwVersionPref = findPreference(KEY_SW_VERSION);
        String swVersion = SystemProperties.get("persist.wimax.fw.version", getString(R.string.status_unavailable));
        wimaxSwVersionPref.setSummary(swVersion);
    }

    private void refreshIPInfo() {
        Preference wimaxIpAddressPref = findPreference(KEY_IP_ADDRESS);
        String ipAddress = SystemProperties.get("dhcp.wimax0.ipaddress", getString(R.string.status_unavailable));
        wimaxIpAddressPref.setSummary(ipAddress);

        Preference wimaxGatewayPref = findPreference(KEY_GATEWAY);
        String gateway = SystemProperties.get("dhcp.wimax0.gateway", getString(R.string.status_unavailable));
        wimaxGatewayPref.setSummary(gateway);
    }

    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(WimaxManagerConstants.NETWORK_STATE_CHANGED_ACTION) ||
                    action.equals(WimaxManagerConstants.RSSI_CHANGED_ACTION)) {
                updateWimax(intent);
            }
        }
    };

    private final void updateWimax(Intent intent) {
        final String action = intent.getAction();
        if (action.equals(WimaxManagerConstants.RSSI_CHANGED_ACTION)) {
            int rssi = intent.getIntExtra(WimaxManagerConstants.EXTRA_NEW_RSSI_LEVEL, -200);
            Preference wimaxSignalStrengthRSSIPref = findPreference(KEY_SIG_STR_SIMPLE);
            if (rssi == 0) {
                wimaxSignalStrengthRSSIPref.setSummary("Level: " + getString(R.string.wimax_signal_0));
            } else if (rssi == 1) {
                wimaxSignalStrengthRSSIPref.setSummary("Level: " + getString(R.string.wimax_signal_1));
            } else if (rssi == 2) {
                wimaxSignalStrengthRSSIPref.setSummary("Level: " + getString(R.string.wimax_signal_2));
            } else if (rssi == 3) {
                wimaxSignalStrengthRSSIPref.setSummary("Level: " + getString(R.string.wimax_signal_3));
            } else {
                wimaxSignalStrengthRSSIPref.setSummary("Level: " + getString(R.string.status_unavailable));
            }
        }
        if (action.equals(WimaxManagerConstants.NETWORK_STATE_CHANGED_ACTION)) {
            //final NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra(WimaxConstants.EXTRA_NETWORK_INFO);
        }
    }
}

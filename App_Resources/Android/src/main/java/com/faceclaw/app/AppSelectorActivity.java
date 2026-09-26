package com.faceclaw.app;

import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.graphics.Insets;
import android.view.View;
import android.view.WindowInsets;

public class AppSelectorActivity extends AppCompatActivity {

    private static final String PREF_NAME = "settings";
    private static final String KEY_ALLOWED = "allowed_apps";

    private ListView listViewApps;
    private SharedPreferences prefs;

    private AppListAdapter appAdapter;
    private EditText editTextSearch;
    private Button buttonBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
    androidx.appcompat.app.AppCompatDelegate
            .setDefaultNightMode(
                    androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
            );

    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_app_selector);
    
            
        View root = findViewById(android.R.id.content);
        root.setOnApplyWindowInsetsListener((v, insets) -> {

            Insets systemBars =
                    insets.getInsets(WindowInsets.Type.systemBars());

            v.setPadding(
                    0,
                    systemBars.top,
                    0,
                    systemBars.bottom
            );

            return insets;
        });

        listViewApps = findViewById(R.id.listViewApps);

        prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        buttonBack = findViewById(R.id.buttonBack);
        buttonBack.setOnClickListener(v -> finish());

        Switch switchSelectAll = findViewById(R.id.switchSelectAll);

        switchSelectAll.setOnCheckedChangeListener((buttonView, isChecked) -> {

            Set<String> allowedSet = new HashSet<>();

            if (isChecked) {

                for (int i = 0; i < appAdapter.getCount(); i++) {

                    ApplicationInfo appInfo =
                            (ApplicationInfo) appAdapter.getItem(i);

                    if (!appInfo.packageName.startsWith("___")) {
                        allowedSet.add(appInfo.packageName);
                    }
                }

            } else {

                // Tout désactiver.
                allowedSet.clear();
            }

            prefs.edit()
                    .putStringSet(KEY_ALLOWED, allowedSet)
                    .apply();

            appAdapter.notifyDataSetChanged();
        });

        editTextSearch = findViewById(R.id.editSearch);

        editTextSearch.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(
                    CharSequence s,
                    int start,
                    int count,
                    int after) {
            }

            @Override
            public void onTextChanged(
                    CharSequence s,
                    int start,
                    int before,
                    int count) {

                if (appAdapter != null) {
                    appAdapter.filter(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        loadInstalledApps();
    }

    private void loadInstalledApps() {

        PackageManager pm = getPackageManager();

        List<ApplicationInfo> allApps =
                pm.getInstalledApplications(0);

        List<ApplicationInfo> userApps =
                new ArrayList<>();

        List<ApplicationInfo> systemApps =
                new ArrayList<>();

        for (ApplicationInfo appInfo : allApps) {

            if ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                userApps.add(appInfo);
            } else {
                systemApps.add(appInfo);
            }
        }

        userApps.sort((a, b) ->
                a.loadLabel(pm)
                        .toString()
                        .compareToIgnoreCase(
                                b.loadLabel(pm).toString()
                        )
        );

        ApplicationInfo separator =
                new ApplicationInfo();

        separator.packageName =
                "_________________ SYSTEM _________________";

        separator.name =
                "_________________ SYSTEM _________________";

        userApps.add(separator);

        systemApps.sort((a, b) ->
                a.loadLabel(pm)
                        .toString()
                        .compareToIgnoreCase(
                                b.loadLabel(pm).toString()
                        )
        );

        userApps.addAll(systemApps);

        appAdapter =
                new AppListAdapter(this, userApps);

        listViewApps.setAdapter(appAdapter);
    }
}


package com.faceclaw.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AppListAdapter extends BaseAdapter {

    private static final String PREF_NAME = "settings";

    private static final String KEY_ALLOWED = "allowed_apps";

    private static final String KEY_BEEP_PREFIX = "app_beep_";
    private static final String KEYWORD_PREFIX = "app_keyword_";

    private static final String DEFAULT_BEEP = "myalarm";

    private static final String[] BEEP_NAMES = {
            "None",
            "coin",
            "powerup",
            "oneup",
            "laser",
            "pew",
            "zap",
            "warp",
            "explosion",
            "success",
            "notify",
            "chime",
            "doorbell",
            "error",
            "deny",
            "tick",
            "doublebeep",
            "sonar",
            "alarm",
            "myalarm",
            "siren",
            "wail",
            "heartbeat",
            "sadtrombone",
            "r2d2",
            "questcomplete",
            "nokia",
            "imperial",
            "mario",
            "scale"
    };

    private final Context context;
    private final List<ApplicationInfo> apps;
    private final List<ApplicationInfo> originalApps;
    private final SharedPreferences prefs;
    private final PackageManager pm;

    public AppListAdapter(Context context, List<ApplicationInfo> apps) {
        this.context = context;
        this.apps = apps;
        this.originalApps = new ArrayList<>(apps);
        this.pm = context.getPackageManager();
        this.prefs = context.getSharedPreferences(
                PREF_NAME,
                Context.MODE_PRIVATE
        );
    }

    @Override
    public int getCount() {
        return apps.size();
    }

    @Override
    public ApplicationInfo getItem(int position) {
        return apps.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    static class ViewHolder {

        ImageView icon;
        TextView textAppName;
        TextView textPackageName;
        Switch switchApp;
        Spinner spinnerBeep;
        EditText editKeyword;
    }

    @Override
    public View getView(
            int position,
            View convertView,
            ViewGroup parent) {

        ViewHolder holder;

        if (convertView == null) {

            convertView = LayoutInflater.from(context)
                    .inflate(
                            R.layout.item_app_switch,
                            parent,
                            false
                    );

            holder = new ViewHolder();

            holder.icon =
                    convertView.findViewById(R.id.imgIcon);

            holder.textAppName =
                    convertView.findViewById(R.id.textAppName);

            holder.textPackageName =
                    convertView.findViewById(R.id.textPackageName);

            holder.switchApp =
                    convertView.findViewById(R.id.switchApp);

            holder.spinnerBeep =
                    convertView.findViewById(R.id.spinnerBeep);

            holder.editKeyword =
                    convertView.findViewById(R.id.editKeyword);

            convertView.setTag(holder);

        } else {

            holder = (ViewHolder) convertView.getTag();
        }

        ApplicationInfo info = apps.get(position);

        String packageName = info.packageName;

        /*
         * SYSTEM separator
         */
        if ("_________________ SYSTEM _________________"
                .equals(packageName)) {

            holder.icon.setVisibility(View.GONE);
            holder.switchApp.setVisibility(View.GONE);
            holder.spinnerBeep.setVisibility(View.GONE);
            holder.editKeyword.setVisibility(View.GONE);

            holder.textAppName.setGravity(
                    android.view.Gravity.CENTER
            );

            holder.textAppName.setText(packageName);
            holder.textPackageName.setText("");

            return convertView;
        }

        /*
         * Application row
         */

        holder.icon.setVisibility(View.VISIBLE);
        holder.switchApp.setVisibility(View.VISIBLE);
        holder.spinnerBeep.setVisibility(View.VISIBLE);
        holder.editKeyword.setVisibility(View.VISIBLE);

        holder.textAppName.setGravity(
                android.view.Gravity.START
        );

        String appName =
                info.loadLabel(pm).toString();

        holder.icon.setImageDrawable(
                info.loadIcon(pm)
        );

        holder.textAppName.setText(appName);
        holder.textPackageName.setText(packageName);

        /*
         * SWITCH
         */

        Set<String> allowed =
                prefs.getStringSet(
                        KEY_ALLOWED,
                        new HashSet<>()
                );

        holder.switchApp.setOnCheckedChangeListener(null);

        holder.switchApp.setChecked(
                allowed.contains(packageName)
        );

        holder.switchApp.setOnCheckedChangeListener(
                (buttonView, isChecked) -> {

                    Set<String> newSet =
                            new HashSet<>(
                                    prefs.getStringSet(
                                            KEY_ALLOWED,
                                            new HashSet<>()
                                    )
                            );

                    if (isChecked) {
                        newSet.add(packageName);
                    } else {
                        newSet.remove(packageName);
                    }

                    prefs.edit()
                            .putStringSet(
                                    KEY_ALLOWED,
                                    newSet
                            )
                            .apply();
                }
        );

        /*
         * BEEP SPINNER
         */

        ArrayAdapter<String> beepAdapter =
        new ArrayAdapter<String>(
                context,
                R.layout.app_spinner_item,
                BEEP_NAMES
        ) {

            @Override
            public View getDropDownView(
                    int position,
                    View convertView,
                    ViewGroup parent) {

                View view =
                        super.getDropDownView(
                                position,
                                convertView,
                                parent
                        );

                return view;
            }
        };

beepAdapter.setDropDownViewResource(
        R.layout.app_spinner_dropdown
);


        beepAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        holder.spinnerBeep.setAdapter(beepAdapter);

        String savedBeep =
                prefs.getString(
                        KEY_BEEP_PREFIX + packageName,
                        DEFAULT_BEEP
                );

        int beepPosition = 0;

        for (int i = 0; i < BEEP_NAMES.length; i++) {

            if (BEEP_NAMES[i].equals(savedBeep)) {
                beepPosition = i;
                break;
            }
        }

        holder.spinnerBeep.setOnItemSelectedListener(null);

        holder.spinnerBeep.setSelection(
                beepPosition,
                false
        );

        holder.spinnerBeep.setOnItemSelectedListener(
                new android.widget.AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(
                            android.widget.AdapterView<?> parent,
                            View view,
                            int position,
                            long id) {

                        String beep =
                                BEEP_NAMES[position];

                        prefs.edit()
                                .putString(
                                        KEY_BEEP_PREFIX + packageName,
                                        beep
                                )
                                .apply();
                    }

                    @Override
                    public void onNothingSelected(
                            android.widget.AdapterView<?> parent) {
                    }
                }
        );

        /*
         * KEYWORD
         */

        TextWatcher oldWatcher =
                (TextWatcher) holder.editKeyword.getTag();

        if (oldWatcher != null) {
            holder.editKeyword.removeTextChangedListener(
                    oldWatcher
            );
        }

        String savedKeyword =
                prefs.getString(
                        KEYWORD_PREFIX + packageName,
                        ""
                );

        holder.editKeyword.setText(savedKeyword);

        TextWatcher newWatcher =
                new TextWatcher() {

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

                        prefs.edit()
                                .putString(
                                        KEYWORD_PREFIX + packageName,
                                        s.toString()
                                )
                                .apply();
                    }

                    @Override
                    public void afterTextChanged(
                            Editable s) {
                    }
                };

        holder.editKeyword.setTag(newWatcher);

        holder.editKeyword.addTextChangedListener(
                newWatcher
        );

        return convertView;
    }

    public void filter(String text) {

        apps.clear();

        if (text == null || text.isEmpty()) {

            apps.addAll(originalApps);

        } else {

            String search =
                    text.toLowerCase();

            for (ApplicationInfo app : originalApps) {

                if ("_________________ SYSTEM _________________"
                        .equals(app.packageName)) {
                    continue;
                }

                String name =
                        app.loadLabel(pm)
                                .toString()
                                .toLowerCase();

                String packageName =
                        app.packageName.toLowerCase();

                if (name.contains(search)
                        || packageName.contains(search)) {

                    apps.add(app);
                }
            }
        }

        notifyDataSetChanged();
    }
}

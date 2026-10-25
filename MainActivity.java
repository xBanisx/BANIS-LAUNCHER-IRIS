package com.banis.launcher;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.widget.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends Activity {
    private final int BG = Color.rgb(8, 9, 11);
    private final int PANEL = Color.rgb(22, 23, 26);
    private final int GOLD = Color.rgb(224, 174, 75);
    private final int WHITE = Color.rgb(245, 242, 235);
    private LinearLayout appGrid;
    private EditText search;
    private TextView clock, date, weather;
    private final ArrayList<AppItem> allApps = new ArrayList<>();
    private static class AppItem {
        String label, pkg; android.graphics.drawable.Drawable icon;
        AppItem(String l, String p, android.graphics.drawable.Drawable i) { label=l; pkg=p; icon=i; }
    }

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        buildUi();
        loadApps();
        renderApps("");
        updateClock();
    }

    private GradientDrawable shape(int color, int stroke, int radius) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color); d.setCornerRadius(radius);
        if (stroke != 0) d.setStroke(2, stroke);
        return d;
    }
    private TextView text(String s, int size, int color, boolean bold) {
        TextView t = new TextView(this); t.setText(s); t.setTextSize(size); t.setTextColor(color);
        t.setGravity(Gravity.CENTER_VERTICAL);
        if (bold) t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return t;
    }
    private LinearLayout column() { LinearLayout l = new LinearLayout(this); l.setOrientation(LinearLayout.VERTICAL); return l; }
    private LinearLayout row() { LinearLayout l = new LinearLayout(this); l.setOrientation(LinearLayout.HORIZONTAL); l.setGravity(Gravity.CENTER_VERTICAL); return l; }

    private void buildUi() {
        ScrollView scroll = new ScrollView(this); scroll.setFillViewport(true); scroll.setBackgroundColor(BG);
        LinearLayout root = column(); root.setPadding(28, 18, 28, 20); root.setBackgroundColor(BG);
        scroll.addView(root);
        setContentView(scroll);

        LinearLayout top = row();
        TextView logo = text("♛  BANIS", 30, GOLD, true);
        top.addView(logo, new LinearLayout.LayoutParams(0, 58, 1));
        LinearLayout info = column(); info.setGravity(Gravity.RIGHT);
        clock = text("--:--", 23, WHITE, true); clock.setGravity(Gravity.RIGHT);
        date = text("", 13, GOLD, false); date.setGravity(Gravity.RIGHT);
        info.addView(clock); info.addView(date);
        top.addView(info);
        root.addView(top);

        TextView tagline = text("SMART TV  •  CREATE WITHOUT LIMITS", 12, GOLD, false);
        tagline.setPadding(3, 0, 0, 12); root.addView(tagline);

        LinearLayout hero = column();
        hero.setPadding(24, 18, 24, 18); hero.setBackground(shape(Color.rgb(17,18,19), GOLD, 20));
        TextView welcome = text("مرحباً بك في BANIS", 27, GOLD, true);
        hero.addView(welcome);
        TextView subtitle = text("تطبيقاتك المفضلة، في مكان واحد", 16, WHITE, false);
        hero.addView(subtitle);
        root.addView(hero, new LinearLayout.LayoutParams(-1, 112));

        LinearLayout shortcuts = row(); shortcuts.setPadding(0, 15, 0, 8);
        Button searchBtn = button("⌕  بحث");
        searchBtn.setOnClickListener(v -> { search.requestFocus(); });
        Button weatherBtn = button("☀  الطقس");
        weatherBtn.setOnClickListener(v -> openUrl("https://wttr.in/Algeria"));
        Button settingsBtn = button("⚙  الإعدادات");
        settingsBtn.setOnClickListener(v -> openSettings());
        shortcuts.addView(searchBtn, new LinearLayout.LayoutParams(0, 52, 1));
        shortcuts.addView(weatherBtn, new LinearLayout.LayoutParams(0, 52, 1));
        shortcuts.addView(settingsBtn, new LinearLayout.LayoutParams(0, 52, 1));
        root.addView(shortcuts);

        search = new EditText(this);
        search.setSingleLine(true);
        search.setTextColor(WHITE); search.setHintTextColor(Color.GRAY);
        search.setHint("ابحث عن تطبيق...");
        search.setTextSize(17); search.setPadding(16, 4, 16, 4);
        search.setBackground(shape(PANEL, Color.rgb(70,65,52), 14));
        root.addView(search, new LinearLayout.LayoutParams(-1, 50));

        TextView section = text("★  التطبيقات المثبتة", 20, GOLD, true);
        section.setPadding(2, 18, 0, 8); root.addView(section);
        appGrid = column(); root.addView(appGrid);

        TextView footer = text("BANIS  •  اختر التطبيق باستخدام الأسهم ثم OK", 12, Color.GRAY, false);
        footer.setGravity(Gravity.CENTER); footer.setPadding(0, 18, 0, 4); root.addView(footer);

        search.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s,int st,int c,int a) {}
            public void onTextChanged(CharSequence s,int start,int before,int count) { renderApps(s.toString()); }
            public void afterTextChanged(Editable e) {}
        });
    }

    private Button button(String label) {
        Button b = new Button(this); b.setText(label); b.setTextColor(GOLD); b.setTextSize(15);
        b.setAllCaps(false); b.setFocusable(true); b.setBackground(shape(PANEL, Color.rgb(80,66,40), 14));
        return b;
    }

    private void loadApps() {
        allApps.clear();
        PackageManager pm = getPackageManager();
        Intent query = new Intent(Intent.ACTION_MAIN, null);
        query.addCategory(Intent.CATEGORY_LAUNCHER);
        List<android.content.pm.ResolveInfo> list = pm.queryIntentActivities(query, 0);
        for (android.content.pm.ResolveInfo ri : list) {
            String pkg = ri.activityInfo.packageName;
            if (pkg.equals(getPackageName())) continue;
            String label = ri.loadLabel(pm).toString();
            allApps.add(new AppItem(label, pkg, ri.loadIcon(pm)));
        }
        Collections.sort(allApps, (a,b) -> a.label.toLowerCase(Locale.getDefault()).compareTo(b.label.toLowerCase(Locale.getDefault())));
    }

    private void renderApps(String filter) {
        if (appGrid == null) return;
        appGrid.removeAllViews();
        ArrayList<AppItem> shown = new ArrayList<>();
        String f = filter == null ? "" : filter.trim().toLowerCase(Locale.getDefault());
        for (AppItem a : allApps) if (a.label.toLowerCase(Locale.getDefault()).contains(f) || a.pkg.toLowerCase(Locale.getDefault()).contains(f)) shown.add(a);
        if (shown.isEmpty()) {
            TextView none = text("لا توجد تطبيقات مطابقة", 16, WHITE, false);
            none.setPadding(8,12,8,12); appGrid.addView(none); return;
        }
        int perRow = 4;
        for (int i=0; i<shown.size(); i+=perRow) {
            LinearLayout line = row(); line.setGravity(Gravity.CENTER_VERTICAL);
            for (int j=i; j<Math.min(i+perRow, shown.size()); j++) {
                final AppItem item = shown.get(j);
                LinearLayout tile = column(); tile.setGravity(Gravity.CENTER); tile.setPadding(5, 8, 5, 8);
                tile.setBackground(shape(PANEL, Color.rgb(42,42,42), 16));
                ImageView icon = new ImageView(this); icon.setImageDrawable(item.icon);
                LinearLayout.LayoutParams ip = new LinearLayout.LayoutParams(52,52); ip.bottomMargin=4; tile.addView(icon, ip);
                TextView label = text(item.label, 13, WHITE, true); label.setGravity(Gravity.CENTER);
                label.setMaxLines(2); tile.addView(label);
                tile.setFocusable(true); tile.setClickable(true);
                tile.setOnFocusChangeListener((v, has) -> v.setBackground(shape(has ? Color.rgb(49,39,20) : PANEL, has ? GOLD : Color.rgb(42,42,42), 16)));
                tile.setOnClickListener(v -> launch(item.pkg));
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, 112, 1);
                lp.setMargins(5, 4, 5, 4); line.addView(tile, lp);
            }
            appGrid.addView(line);
        }
    }

    private void launch(String pkg) {
        try {
            Intent i = getPackageManager().getLaunchIntentForPackage(pkg);
            if (i != null) startActivity(i);
            else Toast.makeText(this, "تعذر فتح التطبيق", Toast.LENGTH_SHORT).show();
        } catch (Exception e) { Toast.makeText(this, "خطأ في فتح التطبيق", Toast.LENGTH_SHORT).show(); }
    }
    private void openSettings() {
        try { startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS)); }
        catch (Exception e) { Toast.makeText(this, "الإعدادات غير متاحة", Toast.LENGTH_SHORT).show(); }
    }
    private void openUrl(String url) {
        try { startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url))); }
        catch (Exception e) { Toast.makeText(this, "لا يوجد متصفح متاح", Toast.LENGTH_SHORT).show(); }
    }
    private void updateClock() {
        Date now = new Date();
        clock.setText(new SimpleDateFormat("HH:mm", Locale.getDefault()).format(now));
        date.setText(new SimpleDateFormat("EEEE، d MMMM yyyy", new Locale("ar")).format(now));
        clock.postDelayed(() -> updateClock(), 30000);
    }

    @Override protected void onResume() { super.onResume(); if (appGrid != null) { loadApps(); renderApps(search == null ? "" : search.getText().toString()); } }
    @Override public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) { openSettings(); return true; }
        if (keyCode == KeyEvent.KEYCODE_SEARCH) { if (search != null) search.requestFocus(); return true; }
        return super.onKeyDown(keyCode, event);
    }
}

package org.cimsbioko.activity;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import com.google.android.material.navigation.NavigationView;
import org.cimsbioko.R;
import org.cimsbioko.campaign.CampaignUpdateService;
import org.cimsbioko.search.IndexingService;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static org.cimsbioko.campaign.CampaignUpdateService.CAMPAIGN_UPDATE_AVAILABLE;
import static org.cimsbioko.search.Utils.isSearchEnabled;
import static org.cimsbioko.syncadpt.Constants.ACCOUNT_TYPE;
import static org.cimsbioko.utilities.CampaignUtils.updateCampaign;
import static org.cimsbioko.utilities.ConfigUtils.getAppFullName;

public class FieldWorkerLoginActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private NavigationView navView;
    private BroadcastReceiver broadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fieldworker_login_activity);
        Toolbar toolbar = findViewById(R.id.fieldworker_login_toolbar);
        DrawerLayout drawerLayout = findViewById(R.id.fieldworker_login_drawer_layout);
        navView = findViewById(R.id.fieldworker_login_navigation_view);
        navView.setNavigationItemSelectedListener(this);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }
        setTitle(getAppFullName(this));

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, 0, 0) {

            boolean tryHide = true;

            @Override
            public void onDrawerOpened(View drawerView) {
                tryHide = true;
                super.onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                tryHide = true;
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                if (tryHide) {
                    InputMethodManager inputMgr = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    View focused = getCurrentFocus();
                    if (focused != null) {
                        inputMgr.hideSoftInputFromWindow(focused.getWindowToken(), 0);
                        tryHide = false;
                    }
                }
                super.onDrawerSlide(drawerView, slideOffset);
            }
        };
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        Account[] accounts = AccountManager.get(this).getAccountsByType(ACCOUNT_TYPE);
        if (navView.getHeaderCount() > 0) {
            boolean attached = accounts.length > 0;
            View headerView = navView.getHeaderView(0);
            TextView textView = headerView.findViewById(R.id.nav_header_text);
            textView.setText(attached ? accounts[0].name : getString(R.string.app_name));
            textView.setVisibility(attached ? VISIBLE : GONE);
        }

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (CAMPAIGN_UPDATE_AVAILABLE.equals(intent.getAction())) {
                    new AlertDialog.Builder(FieldWorkerLoginActivity.this)
                            .setTitle(R.string.campaign_update_title)
                            .setMessage(R.string.campaign_update_msg)
                            .setNegativeButton(R.string.no_btn, (dialog, which) -> {})
                            .setPositiveButton(R.string.yes_btn, (dialog, which) -> updateCampaign())
                            .show();
                }
            }
        };
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this)
                .unregisterReceiver(broadcastReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        navView.getMenu().findItem(R.id.rebuild_search_indices).setVisible(isSearchEnabled(this));
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
        lbm.registerReceiver(broadcastReceiver, new IntentFilter(CAMPAIGN_UPDATE_AVAILABLE));
        startService(new Intent(this, CampaignUpdateService.class));
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.manage_forms:
                startActivity(new Intent(this, ManageFormsActivity.class));
                return true;
            case R.id.send_forms:
                startActivity(new Intent(Intent.ACTION_EDIT));
                return true;
            case R.id.download_data:
                startActivity(new Intent(this, SyncDbActivity.class));
                return true;
            case R.id.rebuild_search_indices:
                IndexingService.queueFullReindex(this);
                return true;
            case R.id.configure_settings:
                startActivity(new Intent(this, PreferenceActivity.class));
                return true;
            default:
                return false;
        }
    }
}
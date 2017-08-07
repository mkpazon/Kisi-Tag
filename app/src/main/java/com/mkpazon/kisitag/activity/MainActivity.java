package com.mkpazon.kisitag.activity;

import android.content.SharedPreferences;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.mkpazon.kisitag.Constants;
import com.mkpazon.kisitag.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;

import static android.nfc.NdefRecord.createMime;

public class MainActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener,
        NfcAdapter.CreateNdefMessageCallback,
        NfcAdapter.OnNdefPushCompleteCallback {

    @BindView(R.id.textView_toSendNext)
    TextView mTvToSendNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setupDrawer(toolbar);

        NfcAdapter mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mNfcAdapter == null) {
            Toast.makeText(this, "NFC is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        // Register callback
        mNfcAdapter.setNdefPushMessageCallback(this, this);
        mNfcAdapter.setOnNdefPushCompleteCallback(this, this);

        resetPayloadText();
    }

    private void setupDrawer(Toolbar toolbar) {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public NdefMessage createNdefMessage(NfcEvent nfcEvent) {
        Timber.d(".createNdefMesasage");
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String payload = preferences.getString(Constants.PREFERENCE_PAYLOAD, Constants.PAYLOAD_UNLOCK);

        return new NdefMessage(
                new NdefRecord[]{
                        createMime("application/vnd.com.example.android.beam", payload.getBytes())
                });
    }

    @Override
    public void onNdefPushComplete(NfcEvent nfcEvent) {
        Timber.d(".onNdefPushComplete");

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String payload = preferences.getString(Constants.PREFERENCE_PAYLOAD, Constants.PAYLOAD_UNLOCK);
        switchPayload();

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                resetPayloadText();
            }
        });
    }

    private void switchPayload() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String payload = preferences.getString(Constants.PREFERENCE_PAYLOAD, Constants.PAYLOAD_UNLOCK);
        SharedPreferences.Editor prefEditor = preferences.edit();
        if (Constants.PAYLOAD_UNLOCK.equals(payload)) {
            prefEditor.putString(Constants.PREFERENCE_PAYLOAD, Constants.PAYLOAD_NOTHING);
        } else {
            prefEditor.putString(Constants.PREFERENCE_PAYLOAD, Constants.PAYLOAD_UNLOCK);
        }
        prefEditor.apply();
    }

    private void resetPayloadText() {
        Timber.d(".resetPayloadText");
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String payload = preferences.getString(Constants.PREFERENCE_PAYLOAD, Constants.PAYLOAD_UNLOCK);
        Timber.i("set text to " + payload);
        mTvToSendNext.setText(payload);
    }

    @OnClick(R.id.textView_toSendNext)
    public void onClickPayload() {
        switchPayload();
        resetPayloadText();
    }
}

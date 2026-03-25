package com.gttcgf.nanoscan;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.gttcgf.nanoscan.databinding.ActivityMainBinding;
import com.gttcgf.nanoscan.viewmodel.MainViewModel;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "MainActivity";

    private ActivityMainBinding binding;
    private MainActivityDeviceListAdapter deviceListAdapter;
    private MainViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_NanoScan);
        super.onCreate(savedInstanceState);
        Log.e(TAG, "主界面-onCreate called");

        Window window = getWindow();
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        window.setStatusBarColor(Color.TRANSPARENT);
        EdgeToEdge.enable(this);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        initComponent();
        observeViewModel();

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG, "主界面-onResume called!");
        viewModel.refresh();
    }

    private void initComponent() {
        binding.etSearch.setEnabled(false);
        binding.ibShutdown.setOnClickListener(this);
        binding.ibAddDevice.setOnClickListener(this);
        binding.ibAccount.setOnClickListener(this);

        binding.pbNews.setVisibility(View.INVISIBLE);
        binding.tvNesEmpty.setVisibility(View.VISIBLE);
        binding.rvDevicesList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        deviceListAdapter = new MainActivityDeviceListAdapter(new ArrayList<>());
        binding.rvDevicesList.setAdapter(deviceListAdapter);
        deviceListAdapter.setOnItemClickListener(position -> viewModel.onDeviceClicked(position));

        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                viewModel.filter(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        enableAllComponent(false);
    }

    private void observeViewModel() {
        viewModel.getUiState().observe(this, state -> {
            deviceListAdapter.updateDeviceList(state.getDevices());
            binding.pbDevicesList.setVisibility(state.isLoading() ? View.VISIBLE : View.INVISIBLE);
            binding.tvDevicesListEmpty.setVisibility(state.isEmpty() ? View.VISIBLE : View.INVISIBLE);
            enableAllComponent(state.isInteractionsEnabled());
        });

        viewModel.getEvents().observe(this, event -> {
            MainViewModel.MainAction action = event != null ? event.getContentIfNotHandled() : null;
            if (action == null) {
                return;
            }
            switch (action.getType()) {
                case OPEN_USER_AGREEMENT:
                    startActivity(new Intent(this, UserAgreementActivity.class));
                    finish();
                    break;
                case OPEN_LOGIN:
                    startActivity(new Intent(this, LoginActivity.class));
                    finish();
                    break;
                case OPEN_DEVICE_DETAILS:
                    Intent detailIntent = new Intent(this, DeviceDetailsActivity.class);
                    detailIntent.putExtra("deviceItem", action.getDeviceItem());
                    startActivity(detailIntent);
                    break;
                case SHOW_TOAST:
                    if (action.getMessage() != null && !action.getMessage().isEmpty()) {
                        Toast.makeText(this, action.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        });
    }

    private void enableAllComponent(boolean enable) {
        binding.ibShutdown.setEnabled(enable);
        binding.ibAccount.setEnabled(enable);
        binding.ibAddDevice.setEnabled(enable);
        binding.etSearch.setEnabled(enable);
        binding.rvDevicesList.setEnabled(enable);
        deviceListAdapter.setClickable(enable);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.ib_shutdown) {
            finish();
        } else if (view.getId() == R.id.ib_add_device) {
            startActivity(new Intent(this, DeviceListActivity.class));
        } else if (view.getId() == R.id.ib_account) {
            startActivity(new Intent(this, UserProfileActivity.class));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.e(TAG, "主界面-onPause called!");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e(TAG, "主界面-onStop called!");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "主界面-onDestroy called!");
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.e(TAG, "主界面-onConfigurationChanged called。newConfig:" + newConfig.orientation);
    }

    public static class StoreCalibration {
        public static String device;
        public static byte[] storrefCoeff;
        public static byte[] storerefMatrix;
    }
}

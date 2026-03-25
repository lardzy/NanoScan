package com.gttcgf.nanoscan;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.gttcgf.nanoscan.databinding.ActivityDeviceDetailsBinding;
import com.gttcgf.nanoscan.tools.DeviceUiUtils;
import com.gttcgf.nanoscan.ui.state.DeviceDetailsUiState;
import com.gttcgf.nanoscan.viewmodel.DeviceDetailsViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DeviceDetailsActivity extends AppCompatActivity implements View.OnClickListener {
    private ActivityDeviceDetailsBinding binding;
    private DeviceDetailsViewModel viewModel;
    private RecentSpectralDataListAdapter spectralDataListAdapter;
    private Animation fadeIn;
    private Animation fadeOut;
    private List<DeviceDetailMenuItems> menuItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityDeviceDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(DeviceDetailsViewModel.class);
        DeviceItem deviceItem = (DeviceItem) getIntent().getSerializableExtra("deviceItem");
        if (deviceItem == null) {
            finish();
            return;
        }

        fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out);

        initData();
        initComponent();
        observeViewModel();
        viewModel.initialize(deviceItem);

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        boolean warmUp = viewModel.getUiState().getValue() != null && viewModel.getUiState().getValue().isWarmUp();
        viewModel.refresh(warmUp);
    }

    private void initData() {
        menuItems = new ArrayList<>();
        menuItems.add(new DeviceDetailMenuItems("设备详情"));
        menuItems.add(new DeviceDetailMenuItems("删除本地参比"));
        menuItems.add(new DeviceDetailMenuItems("删除当前设备"));
        menuItems.add(new DeviceDetailMenuItems("检索光谱"));
        menuItems.add(new DeviceDetailMenuItems("连接设备"));
    }

    private void initComponent() {
        binding.rvRecentSpectralDataList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        spectralDataListAdapter = new RecentSpectralDataListAdapter(this);
        binding.rvRecentSpectralDataList.setAdapter(spectralDataListAdapter);

        binding.scan.setOnClickListener(this);
        binding.deviceConnectionLayout.setOnClickListener(this);
        binding.imageButtonBack.setOnClickListener(this);
        binding.imageButtonMenu.setOnClickListener(this);
        binding.scan.setEnabled(false);

        binding.notPreheatToggle.setOnCheckedChangeListener((compoundButton, checked) -> {
            int colorFrom = checked ? android.graphics.Color.WHITE : android.graphics.Color.parseColor("#FFEACA");
            int colorTo = checked ? android.graphics.Color.parseColor("#FFEACA") : android.graphics.Color.WHITE;
            ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
            colorAnimation.setDuration(300);
            colorAnimation.addUpdateListener(animator -> binding.notPreheatToggle.setBackgroundTintList(android.content.res.ColorStateList.valueOf((int) animator.getAnimatedValue())));
            colorAnimation.start();
            binding.notPreheatToggle.setTextColor(checked ? android.graphics.Color.parseColor("#8CBEFF") : android.graphics.Color.GRAY);
            viewModel.updateWarmUp(checked);
        });

        spectralDataListAdapter.setOnItemClickListener(new RecentSpectralDataListAdapter.OnItemClickListener() {
            @Override
            public void OnItemClick(int position, List<String> dataList) {
                if (position >= 0 && position < dataList.size()) {
                    viewModel.openSpectrumPreview(dataList.get(position));
                }
            }

            @Override
            public void OnItemLongClick(int position, List<String> dataList) {
                if (position >= 0 && position < dataList.size()) {
                    showDeleteSpectrumDialog(position, dataList);
                }
            }
        });
    }

    private void observeViewModel() {
        viewModel.getUiState().observe(this, state -> {
            binding.tvDeviceMac.setText(state.getDeviceItem().getDeviceMac());
            binding.tvDeviceName.setText(state.getDeviceItem().getDeviceName());
            binding.lightUsageDuration.setText(state.getTotalLampTime());
            binding.spectralReferenceUpdateDateValue.setText(state.getReferenceUpdateDate());
            binding.numberOfSpectraCollectedValue.setText(String.valueOf(state.getSpectraCount()));
            if (state.getBattery() > 0) {
                binding.batteryLevel.setText(getString(R.string.battery_level, state.getBattery() + "%"));
            } else {
                binding.batteryLevel.setText(getString(R.string.not_available));
            }
            binding.batteryImage.setImageResource(DeviceUiUtils.getBatteryIconRes(state.getBattery()));
            spectralDataListAdapter.updateDataList();
            spectralDataListAdapter.notifyDataSetChanged();
            binding.rvRecentSpectralDataList.setVisibility(state.hasSpectra() ? View.VISIBLE : View.INVISIBLE);
            binding.recentSpectralDataListEmpty.setVisibility(state.hasSpectra() ? View.INVISIBLE : View.VISIBLE);
            updateLoadingState(state.isLoading());
            if (binding.notPreheatToggle.isChecked() != state.isWarmUp()) {
                binding.notPreheatToggle.setChecked(state.isWarmUp());
            }
        });

        viewModel.getEvents().observe(this, event -> {
            DeviceDetailsViewModel.DeviceDetailsAction action = event != null ? event.getContentIfNotHandled() : null;
            if (action == null) {
                return;
            }
            switch (action.getType()) {
                case SHOW_TOAST:
                    Toast.makeText(this, action.getMessage(), Toast.LENGTH_SHORT).show();
                    break;
                case OPEN_SCAN:
                    Intent scanIntent = new Intent(this, ScanViewActivity.class);
                    scanIntent.putExtra("deviceItem", action.getDeviceItem());
                    scanIntent.putExtra("warmUp", action.isWarmUp());
                    scanIntent.putExtra("mainFlag", true);
                    startActivity(scanIntent);
                    break;
                case OPEN_SPECTRUM_PREVIEW:
                    Bundle bundle = new Bundle();
                    bundle.putString("userPhoneNumber", viewModel.getUserPhoneNumber());
                    bundle.putString("fileName", action.getFileName());
                    SpectralPreviewDialogFragment previewDialogFragment = SpectralPreviewDialogFragment.newInstance(bundle);
                    previewDialogFragment.show(getSupportFragmentManager(), "光谱预览");
                    break;
                case FINISH:
                    finish();
                    break;
            }
        });
    }

    private void updateLoadingState(boolean loading) {
        binding.deviceConnectionLayout.setClickable(!loading);
        if (loading) {
            binding.progressBar.setVisibility(View.VISIBLE);
            binding.progressBar.startAnimation(fadeIn);
            binding.connectBtn.setVisibility(View.GONE);
            binding.connectText.setVisibility(View.GONE);
            binding.scan.setEnabled(false);
            return;
        }
        binding.progressBar.startAnimation(fadeOut);
        binding.progressBar.setVisibility(View.GONE);
        binding.connectBtn.setVisibility(View.VISIBLE);
        binding.connectText.setVisibility(View.VISIBLE);
        binding.connectBtn.startAnimation(fadeIn);
        binding.connectText.startAnimation(fadeIn);
        binding.scan.setEnabled(true);
    }

    private void showDeleteSpectrumDialog(int position, List<String> dataList) {
        String fileName = dataList.get(position);
        String dateTime = Objects.requireNonNull(com.gttcgf.nanoscan.tools.SpectralDataUtils.userSpectralFileMap.get(fileName)).getDateTime();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("预测结果");
        builder.setMessage(dateTime + "\n是否删除本条结果？");
        builder.setPositiveButton("删除", (dialogInterface, i) -> viewModel.deleteSpectrum(fileName));
        builder.setNegativeButton("取消", (dialogInterface, i) -> dialogInterface.dismiss());
        AlertDialog alertDialog = builder.create();
        Objects.requireNonNull(alertDialog.getWindow()).setBackgroundDrawableResource(R.drawable.rounded_rectangle);
        alertDialog.show();
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.scan) {
            binding.scan.setEnabled(false);
            viewModel.openScan();
        } else if (view.getId() == R.id.device_connection_layout) {
            boolean warmUp = viewModel.getUiState().getValue() != null && viewModel.getUiState().getValue().isWarmUp();
            viewModel.refresh(warmUp);
        } else if (view.getId() == R.id.imageButton_back) {
            finish();
        } else if (view.getId() == R.id.imageButton_menu) {
            binding.imageButtonMenu.setEnabled(false);
            DeviceDetailsMenuDialogFragment menuDialogFragment = getDeviceDetailsMenuDialogFragment();
            menuDialogFragment.show(getSupportFragmentManager(), "DeviceDetailsMenuDialogFragment");
        }
    }

    private @NonNull DeviceDetailsMenuDialogFragment getDeviceDetailsMenuDialogFragment() {
        DeviceItem deviceItem = viewModel.getUiState().getValue() != null ? viewModel.getUiState().getValue().getDeviceItem() : null;
        DeviceDetailsMenuDialogFragment menuDialogFragment = new DeviceDetailsMenuDialogFragment(menuItems, deviceItem);
        menuDialogFragment.setOnMenuCloseListener(() -> binding.imageButtonMenu.setEnabled(true));
        menuDialogFragment.setActivityOnItemClickListener(position -> {
            switch (position) {
                case 0:
                    modifyDeviceInformation();
                    break;
                case 1:
                    viewModel.deleteReference();
                    break;
                case 2:
                    viewModel.deleteCurrentDevice();
                    break;
                case 3:
                    break;
                case 4:
                    viewModel.openScan();
                    break;
            }
        });
        return menuDialogFragment;
    }

    private void modifyDeviceInformation() {
        DeviceDetailsUiState state = viewModel.getUiState().getValue();
        if (state == null) {
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("修改设备信息");
        View view = getLayoutInflater().inflate(R.layout.modify_device_information_dialog_layout, null);
        TextView deviceTypeInput = view.findViewById(R.id.device_type_input);
        TextView deviceInfoInput = view.findViewById(R.id.device_info_input);
        EditText deviceNameInput = view.findViewById(R.id.device_name_input);
        deviceNameInput.setText(state.getDeviceItem().getDeviceName());
        deviceTypeInput.setText(state.getDeviceItem().getDeviceType());
        deviceInfoInput.setText(state.getDeviceItem().getDeviceMac());
        builder.setView(view);
        builder.setPositiveButton("确认", (dialog, which) -> {
            String newName = deviceNameInput.getText().toString();
            if (newName.isEmpty() || newName.length() > 20) {
                Toast.makeText(this, "设备名称格式有误!", Toast.LENGTH_SHORT).show();
                return;
            }
            viewModel.renameDevice(newName);
        });
        builder.setNeutralButton("删除设备", (dialog, which) -> viewModel.deleteCurrentDevice());
        builder.setNegativeButton("取消", (dialog, which) -> {
        });
        AlertDialog dialog = builder.create();
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(R.drawable.rounded_rectangle);
        dialog.show();
    }
}

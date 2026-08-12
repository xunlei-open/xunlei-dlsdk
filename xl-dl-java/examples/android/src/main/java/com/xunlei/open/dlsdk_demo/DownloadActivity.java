package com.xunlei.open.dlsdk_demo;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.xunlei.open.dlsdk.XLDownloadAPI;
import com.xunlei.open.dlsdk_demo.databinding.ActivityDownloadBinding;

public class DownloadActivity extends AppCompatActivity implements TaskAdapter.OnTaskActionListener {
    private DownloadViewModel viewModel;
    private TaskAdapter taskAdapter;
    private ActivityDownloadBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDownloadBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(DownloadViewModel.class);

        initViews();
        observeViewModel();
    }

    private void initViews() {
        setSupportActionBar(binding.toolbar);

        taskAdapter = new TaskAdapter(this, this);
        binding.taskList.setAdapter(taskAdapter);
        binding.taskList.setLayoutManager(new LinearLayoutManager(this));
        binding.taskList.addItemDecoration(
                new DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        );

        binding.addTaskButton.setOnClickListener(v -> showAddTaskDialog());
    }

    private void observeViewModel() {
        binding.switchToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            viewModel.setTokenSwitch(isChecked);
        });
        viewModel.getTokenSwitch().observe(this, enabled -> binding.switchToggle.setChecked(enabled));
        viewModel.getTasks().observe(this, tasks -> taskAdapter.updateTasks(tasks));

        viewModel.getDownloadingCount().observe(this, count ->
                binding.downloadingCount.setText(String.valueOf("下载中: " + count)));

        viewModel.getCompletedCount().observe(this, count ->
                binding.completedCount.setText(String.valueOf("已下载: " + count)));

        viewModel.getTotalCount().observe(this, count ->
                binding.totalCount.setText(String.valueOf("总数: " + count)));

        viewModel.getOperationResult().observe(this, result -> {
            if (result != XLDownloadAPI.ERROR_SUCCESS) {
                String message;
                switch (result) {
                    case XLDownloadAPI.ERROR_TASK_NOT_EXIST:
                        message = "任务不存在";
                        break;
                    case XLDownloadAPI.ERROR_TASK_ALREADY_STOPPED:
                        message = "任务已暂停";
                        break;
                    case XLDownloadAPI.ERROR_TASK_ALREADY_RUNNING:
                        message = "任务正在下载中";
                        break;
                    case XLDownloadAPI.ERROR_TASK_ALREADY_EXIST:
                        message = "任务已存在";
                        break;
                    case XLDownloadAPI.ERROR_TOO_MUCH_TASK:
                        message = "下载任务数量已达上限";
                        break;
                    case XLDownloadAPI.ERROR_PARAM_ERROR:
                        message = "参数错误";
                        break;
                    case XLDownloadAPI.ERROR_SDK_NOT_INIT:
                        message = "SDK未初始化";
                        break;
                    case XLDownloadAPI.ERROR_AUTH_TOKEN_VERIFY_FAILED:
                        message = "认证失败";
                        break;
                    case XLDownloadAPI.ERROR_DISK_FULL:
                        message = "存储空间不足";
                        break;
                    case XLDownloadAPI.ERROR_FILE_EXISTED:
                        message = "文件已存在";
                        break;
                    case XLDownloadAPI.ERROR_FILE_NOT_EXIST:
                        message = "文件不存在";
                        break;
                    case XLDownloadAPI.ERROR_INSUFFICIENT_DISK_SPACE:
                        message = "存储空间不足";
                        break;
                    case XLDownloadAPI.ERROR_OPEN_FILE_ERR:
                        message = "文件打开失败";
                        break;
                    case XLDownloadAPI.ERROR_TIME_OUT:
                        message = "连接超时";
                        break;
                    case XLDownloadAPI.ERROR_TASK_FINISH:
                        message = "任务已完成";
                        break;
                    case XLDownloadAPI.ERROR_TASK_NOT_RUNNING:
                        message = "任务未在运行";
                        break;
                    case XLDownloadAPI.ERROR_TASK_NOT_IDLE:
                        message = "任务正忙";
                        break;
                    default:
                        message = "操作失败：" + result;
                }
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "操作成功", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAddTaskDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_task, null);
        EditText urlInput = dialogView.findViewById(R.id.url_input);

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this)
                .setTitle("添加下载任务")
                .setView(dialogView)
                .setPositiveButton("下载", (dialog, which) -> {
                    String url = urlInput.getText().toString().trim();
                    if (!TextUtils.isEmpty(url)) {
//                        if (Patterns.WEB_URL.matcher(url).matches()) {
                            viewModel.addDownloadTask(url);
//                        } else {
//                            Toast.makeText(this, "请输入有效的URL", Toast.LENGTH_SHORT).show();
//                        }
                    }
                })
                .setNegativeButton("取消", null);

        builder.show();
    }

    @Override
    public void onTaskPause(long taskId) {
        viewModel.pauseTask(taskId);
    }

    @Override
    public void onTaskResume(long taskId) {
        viewModel.resumeTask(taskId);
    }

    @Override
    public void onTaskDelete(long taskId) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.delete_task)
                .setMessage(R.string.delete_task_confirmation)
                .setPositiveButton(R.string.delete, (dialog, which) ->
                        viewModel.deleteTask(taskId))
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

}
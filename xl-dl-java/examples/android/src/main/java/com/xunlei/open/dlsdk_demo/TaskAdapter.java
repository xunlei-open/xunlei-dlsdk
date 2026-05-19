package com.xunlei.open.dlsdk_demo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;
import com.xunlei.open.dlsdk.XLDownloadAPI;
import java.util.ArrayList;
import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {
    private final List<DownloadTask> tasks = new ArrayList<>();
    private final Context context;
    private final OnTaskActionListener actionListener;

    public interface OnTaskActionListener {
        void onTaskPause(long taskId);
        void onTaskResume(long taskId);
        void onTaskDelete(long taskId);
    }

    public TaskAdapter(Context context, OnTaskActionListener listener) {
        this.context = context;
        this.actionListener = listener;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_download_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        holder.bind(tasks.get(position));
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    public void updateTasks(List<DownloadTask> newTasks) {
        tasks.clear();
        tasks.addAll(newTasks);
        notifyDataSetChanged();
    }

    class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView fileName;
        Button status;
        ProgressBar progressBar;
        TextView speed;
        TextView sizeInfo;
        View itemView;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;
            fileName = itemView.findViewById(R.id.file_name);
            status = itemView.findViewById(R.id.status);
            progressBar = itemView.findViewById(R.id.progress_bar);
            speed = itemView.findViewById(R.id.speed);
            sizeInfo = itemView.findViewById(R.id.size_info);
            status.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DownloadTask task = (DownloadTask) v.getTag();
                    if (task.getStatus() == XLDownloadAPI.TASK_STATUS_STOPED) {
                        actionListener.onTaskResume(task.getTaskId() );
                    } else if (task.getStatus() == XLDownloadAPI.TASK_STATUS_STARTED) {
                        actionListener.onTaskPause(task.getTaskId() );
                    }
                }
            });

            // 长按菜单
            itemView.setOnLongClickListener(v -> {
                showPopupMenu(v, getAdapterPosition());
                return true;
            });
        }

        void bind(DownloadTask task) {
            fileName.setText(task.getFileName());
            progressBar.setProgress(task.getProgress() );
            speed.setText(formatSpeed(task.getSpeed()));
            status.setText(getStatusText(task.getStatus()));
            sizeInfo.setText(formatSizeInfo(task.getDownloadedSize(), task.getTotalSize()));
            status.setTag(task);

            switch (task.getStatus()) {
                case XLDownloadAPI.TASK_STATUS_STOPED:
                case XLDownloadAPI.TASK_STATUS_STARTED:
                    status.setEnabled(true);
                    break;
                default:
                    status.setEnabled(false);
            }
        }

        private void showPopupMenu(View view, int position) {
            PopupMenu popup = new PopupMenu(context, view);
            DownloadTask task = tasks.get(position);
            popup.getMenu().add(Menu.NONE, 1, Menu.NONE, "删除");

            popup.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case 1: // 删除
                        actionListener.onTaskDelete(task.getTaskId() );
                        return true;
                }
                return false;
            });

            popup.show();
        }
    }

    private String formatSpeed(long bytesPerSecond) {
        if (bytesPerSecond < 1024) {
            return bytesPerSecond + "B/s";
        } else if (bytesPerSecond < 1024 * 1024) {
            return String.format("%.1fKB/s", bytesPerSecond / 1024.0f);
        } else {
            return String.format("%.1fMB/s", bytesPerSecond / (1024.0f * 1024.0f));
        }
    }

    private String formatSizeInfo(long downloaded, long total) {
        return formatSize(downloaded) + "/" + formatSize(total);
    }

    private String formatSize(long bytes) {
        if (bytes < 1024) {
            return bytes + "B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.1fKB", bytes / 1024.0f);
        } else if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.1fMB", bytes / (1024.0f * 1024.0f));
        } else {
            return String.format("%.1fGB", bytes / (1024.0f * 1024.0f * 1024.0f));
        }
    }

    private String getStatusText(int status) {
        switch (status) {
            case XLDownloadAPI.TASK_STATUS_STARTED:
                return "下载中";
            case XLDownloadAPI.TASK_STATUS_STOPED:
                return "已暂停";
            case XLDownloadAPI.TASK_STATUS_SUCCEEDED:
                return "已完成";
            case XLDownloadAPI.TASK_STATUS_FAILED:
                return "下载失败";
            case XLDownloadAPI.TASK_STATUS_START_PENDING:
                return "准备中";
            case XLDownloadAPI.TASK_STATUS_STOP_PENDING:
                return "正在暂停";
            case XLDownloadAPI.TASK_STATUS_START_WAITING:
                return "等待中";

            default:
                return "未知状态";
        }
    }

}

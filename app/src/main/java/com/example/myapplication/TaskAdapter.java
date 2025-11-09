package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.ViewHolder> {

    private List<Task> taskList;

    // --- INTERFACE BARU UNTUK LISTENER ---
    public interface OnTaskLongClickListener {
        void onTaskLongClick(Task task);
    }

    private OnTaskLongClickListener longClickListener;
    // --- BATAS INTERFACE BARU ---

    // --- CONSTRUCTOR DIUBAH ---
    public TaskAdapter(List<Task> taskList, OnTaskLongClickListener longClickListener) {
        this.taskList = taskList;
        this.longClickListener = longClickListener;
    }
    // --- BATAS CONSTRUCTOR ---

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Task task = taskList.get(position);

        holder.tvTaskName.setText(task.getTaskName());
        holder.tvTaskDate.setText(task.getDate());
        holder.tvTaskTime.setText(task.getTimeRange());

        // --- TAMBAHKAN LONG CLICK LISTENER ---
        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onTaskLongClick(task);
                return true; // event sudah di-handle
            }
            return false;
        });
        // --- BATAS TAMBAHAN ---
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public void updateData(List<Task> newTaskList) {
        this.taskList.clear();
        this.taskList.addAll(newTaskList);
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTaskName, tvTaskDate, tvTaskTime;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTaskName = itemView.findViewById(R.id.tvTaskName);
            tvTaskDate = itemView.findViewById(R.id.tvTaskDate);
            tvTaskTime = itemView.findViewById(R.id.tvTaskTime);
        }
    }
}
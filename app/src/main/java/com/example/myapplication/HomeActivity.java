package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;
import android.util.Log;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.app.AlertDialog; // Pastikan ini di-import

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

// --- IMPLEMENTASI INTERFACE BARU ---
public class HomeActivity extends AppCompatActivity implements TaskAdapter.OnTaskLongClickListener {
// --- BATAS IMPLEMENTASI ---

    private static final String TAG = "HomeActivity";

    TextView tvWelcome, tvCurrentDateDisplay;
    Button btnToday, btnTomorrow;
    Button btnAdjustDiet, btnAdjustSleep, btnAdjustHydration;
    FloatingActionButton fabAddTask;
    DatabaseHelper db;
    String userEmail;
    AIAnalyticsHelper aiHelper;

    RecyclerView rvCalendarAgenda;
    TaskAdapter taskAdapter;
    List<Task> taskList;

    private String currentlyDisplayedDate;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private SimpleDateFormat friendlySdf = new SimpleDateFormat("MMM d", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "=== onCreate started ===");
        setContentView(R.layout.activity_home);

        try {
            db = new DatabaseHelper(this);
            userEmail = getIntent().getStringExtra("USER_EMAIL");

            if (userEmail == null || userEmail.isEmpty()) {
                Log.e(TAG, "ERROR: Email is null or empty!");
                Toast.makeText(this, "Error: Email tidak ditemukan.", Toast.LENGTH_LONG).show();
                startActivity(new Intent(HomeActivity.this, MainActivity.class));
                finish();
                return;
            }

            // Inisialisasi Views
            tvWelcome = findViewById(R.id.tvWelcome);
            btnAdjustDiet = findViewById(R.id.btnAdjustDiet);
            btnAdjustSleep = findViewById(R.id.btnAdjustSleep);
            btnAdjustHydration = findViewById(R.id.btnAdjustHydration);
            fabAddTask = findViewById(R.id.fabAddTask);
            rvCalendarAgenda = findViewById(R.id.rvCalendarAgenda);
            tvCurrentDateDisplay = findViewById(R.id.tvCurrentDateDisplay);
            btnToday = findViewById(R.id.btnToday);
            btnTomorrow = findViewById(R.id.btnTomorrow);

            // Set Welcome Text
            if (tvWelcome != null) {
                UserProfile profile = db.getUserProfile(userEmail);
                if (profile != null && profile.getName() != null && !profile.getName().isEmpty()) {
                    tvWelcome.setText("Welcome, " + profile.getName() + "!");
                } else {
                    tvWelcome.setText("Welcome, " + userEmail + "!");
                }
            }

            // --- SETUP RECYCLERVIEW (DIUBAH) ---
            taskList = new ArrayList<>();
            // 'this' (HomeActivity) sekarang adalah listener-nya
            taskAdapter = new TaskAdapter(taskList, this);
            rvCalendarAgenda.setLayoutManager(new LinearLayoutManager(this));
            rvCalendarAgenda.setAdapter(taskAdapter);
            // --- BATAS PERUBAHAN ---

            // Inisialisasi AI Helper
            aiHelper = new AIAnalyticsHelper(this, db, userEmail);

            // Setup Button Listeners
            setupButtonListeners();

            // Tampilkan tanggal hari ini
            updateDateUI(0);

            Log.d(TAG, "onCreate completed successfully");

        } catch (Exception e) {
            Log.e(TAG, "FATAL ERROR in onCreate: " + e.getMessage());
            e.printStackTrace();
            Toast.makeText(this, "Fatal error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    // --- FUNGSI BARU SAAT ITEM DI-TEKAN LAMA ---
    @Override
    public void onTaskLongClick(Task task) {
        showDeleteConfirmationDialog(task);
    }
    // --- BATAS FUNGSI BARU ---

    /**
     * Fungsi BARU untuk menampilkan dialog konfirmasi hapus
     */
    private void showDeleteConfirmationDialog(Task task) {
        new AlertDialog.Builder(this)
                .setTitle("Hapus Tugas")
                .setMessage("Apakah Anda yakin ingin menghapus tugas '" + task.getTaskName() + "'?")
                .setPositiveButton("Ya, Hapus", (dialog, which) -> {
                    // Panggil fungsi delete di DB
                    boolean deleted = db.deleteTaskById(task.getId());
                    if (deleted) {
                        Toast.makeText(this, "Tugas dihapus!", Toast.LENGTH_SHORT).show();
                        loadTasks(); // Refresh list setelah dihapus
                    } else {
                        Toast.makeText(this, "Gagal menghapus tugas.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    // Sisa file (updateDateUI, setupButtonListeners, loadTasks, onResume,
    // showConfirmationDialog) biarkan sama seperti kode lengkap sebelumnya.
    // ...
    // (Salin-tempel sisa fungsi dari jawaban saya sebelumnya)
    // ...

    private void updateDateUI(int daysToAdd) {
        Calendar c = Calendar.getInstance();
        if (daysToAdd > 0) {
            c.add(Calendar.DAY_OF_YEAR, daysToAdd);
        }
        currentlyDisplayedDate = sdf.format(c.getTime());
        String friendlyDayName = (daysToAdd == 0) ? "Today" : "Tomorrow";
        String friendlyDate = friendlyDayName + ", " + friendlySdf.format(c.getTime());
        tvCurrentDateDisplay.setText(friendlyDate);
        Log.d(TAG, "Date updated to: " + currentlyDisplayedDate);
        loadTasks();
    }

    private void setupButtonListeners() {
        btnToday.setOnClickListener(v -> updateDateUI(0));
        btnTomorrow.setOnClickListener(v -> updateDateUI(1));

        btnAdjustDiet.setOnClickListener(v -> {
            if (aiHelper == null) {
                Toast.makeText(this, "AI Helper tidak tersedia", Toast.LENGTH_SHORT).show();
                return;
            }
            showConfirmationDialog("Adjust Diet", () -> {
                Toast.makeText(this, "AI sedang menganalisis " + currentlyDisplayedDate + "...", Toast.LENGTH_SHORT).show();
                aiHelper.adjustDiet(new AIResponseListener() {
                    @Override
                    public void onSuccess() {
                        loadTasks();
                        Toast.makeText(HomeActivity.this, "Jadwal diet telah ditambahkan!", Toast.LENGTH_LONG).show();
                    }
                    @Override
                    public void onError(String message) {
                        Toast.makeText(HomeActivity.this, "Error: " + message, Toast.LENGTH_LONG).show();
                    }
                }, currentlyDisplayedDate);
            });
        });

        btnAdjustSleep.setOnClickListener(v -> {
            if (aiHelper == null) {
                Toast.makeText(this, "AI Helper tidak tersedia", Toast.LENGTH_SHORT).show();
                return;
            }
            showConfirmationDialog("Adjust Sleep", () -> {
                Toast.makeText(this, "AI sedang menganalisis " + currentlyDisplayedDate + "...", Toast.LENGTH_SHORT).show();
                aiHelper.adjustSleep(new AIResponseListener() {
                    @Override
                    public void onSuccess() {
                        loadTasks();
                        Toast.makeText(HomeActivity.this, "Jadwal tidur telah ditambahkan!", Toast.LENGTH_LONG).show();
                    }
                    @Override
                    public void onError(String message) {
                        Toast.makeText(HomeActivity.this, "Error: " + message, Toast.LENGTH_LONG).show();
                    }
                }, currentlyDisplayedDate);
            });
        });

        btnAdjustHydration.setOnClickListener(v -> {
            if (aiHelper == null) {
                Toast.makeText(this, "AI Helper tidak tersedia", Toast.LENGTH_SHORT).show();
                return;
            }
            showConfirmationDialog("Adjust Hydration", () -> {
                Toast.makeText(this, "AI sedang menganalisis " + currentlyDisplayedDate + "...", Toast.LENGTH_SHORT).show();
                aiHelper.adjustHydration(new AIResponseListener() {
                    @Override
                    public void onSuccess() {
                        loadTasks();
                        Toast.makeText(HomeActivity.this, "Pengingat hidrasi telah ditambahkan!", Toast.LENGTH_LONG).show();
                    }
                    @Override
                    public void onError(String message) {
                        Toast.makeText(HomeActivity.this, "Error: " + message, Toast.LENGTH_LONG).show();
                    }
                }, currentlyDisplayedDate);

            });
        });

        fabAddTask.setOnClickListener(v -> {
            Log.d(TAG, "FAB Add Task clicked");
            Intent intent = new Intent(HomeActivity.this, AddTaskActivity.class);
            intent.putExtra("USER_EMAIL", userEmail);
            startActivity(intent);
        });
    }

    private void loadTasks() {
        try {
            if (userEmail != null && !userEmail.isEmpty() && currentlyDisplayedDate != null) {
                Log.d(TAG, "Loading tasks for user: " + userEmail + " on date: " + currentlyDisplayedDate);
                List<Task> tasks = db.getTasksForUserByDate(userEmail, currentlyDisplayedDate);
                Log.d(TAG, "Tasks loaded: " + tasks.size());

                if (taskAdapter != null) {
                    taskAdapter.updateData(tasks);
                } else {
                    Log.e(TAG, "TaskAdapter is null, cannot update data");
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading tasks: " + e.getMessage());
            e.printStackTrace();
            Toast.makeText(this, "Error loading tasks", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume called");
        loadTasks();
    }

    private void showConfirmationDialog(String title, Runnable onConfirm) {
        try {
            new AlertDialog.Builder(this)
                    .setTitle(title)
                    .setMessage("Apakah Anda yakin ingin AI menganalisis dan menambahkan jadwal baru untuk tanggal " + currentlyDisplayedDate + "?")
                    .setPositiveButton("Ya, Jalankan", (dialog, which) -> onConfirm.run())
                    .setNegativeButton("Batal", null)
                    .show();
        } catch (Exception e) {
            Log.e(TAG, "Error showing dialog: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
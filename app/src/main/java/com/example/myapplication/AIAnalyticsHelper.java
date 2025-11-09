package com.example.myapplication;

import android.content.Context;
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AIAnalyticsHelper {

    private DatabaseHelper db;
    private String userEmail;
    private ApiService apiService;
    private Gson gson;

    public AIAnalyticsHelper(Context context, DatabaseHelper db, String userEmail) {
        this.db = db;
        this.userEmail = userEmail;
        // Ambil ApiService dari RetrofitClient versi Gemini
        this.apiService = RetrofitClient.getApiService();
        this.gson = new Gson();
    }

    /**
     * Meminta AI untuk membuat jadwal makan (Sarapan, Makan Siang, Makan Malam)
     * yang disesuaikan dengan profil dan jadwal sibuk pengguna.
     */
    public void adjustDiet(AIResponseListener listener) {
        UserProfile profile = db.getUserProfile(userEmail);
        List<Task> busySchedule = db.getTasksForUser(userEmail);

        if (profile == null) {
            listener.onError("User profile not found.");
            return;
        }

        String profileJson = gson.toJson(profile);
        String scheduleJson = gson.toJson(busySchedule);
        String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        String systemPrompt = "Anda adalah asisten kesehatan. Tugas Anda adalah membuat jadwal makan " +
                "berdasarkan profil dan jadwal sibuk pengguna. " +
                "Anda HARUS membalas HANYA dengan JSON Array yang berisi objek Task. " +
                "Setiap objek Task harus memiliki key: 'taskName', 'date', 'startTime', dan 'endTime'. " +
                "Contoh: [{'taskName': 'Sarapan', 'date': '2025-11-02', 'startTime': '07:00', 'endTime': '07:30'}]";

        String userPrompt = "Tolong buatkan jadwal 'Sarapan', 'Makan Siang', dan 'Makan Malam' untuk saya hari ini, " +
                "tanggal " + todayDate + ". " +
                "Ini adalah profil saya: " + profileJson + ". " +
                "Ini adalah jadwal sibuk saya (JANGAN menimpa jadwal ini): " + scheduleJson + ". " +
                "Pastikan jadwal makan tidak bentrok dengan jadwal sibuk saya.";

        callApi(systemPrompt, userPrompt, "Diet", listener);
    }

    /**
     * Meminta AI untuk membuat satu jadwal tidur ideal (8 jam)
     * yang disesuaikan dengan profil dan jadwal sibuk pengguna.
     */
    public void adjustSleep(AIResponseListener listener) {
        UserProfile profile = db.getUserProfile(userEmail);
        List<Task> busySchedule = db.getTasksForUser(userEmail);

        if (profile == null) {
            listener.onError("User profile not found.");
            return;
        }

        String profileJson = gson.toJson(profile);
        String scheduleJson = gson.toJson(busySchedule);
        String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        String systemPrompt = "Anda adalah asisten kesehatan. Tugas Anda adalah membuat SATU jadwal tidur ideal " +
                "berdasarkan profil usia dan jadwal sibuk pengguna. " +
                "Anda HARUS membalas HANYA dengan JSON Array yang berisi SATU objek Task. " +
                "Objek Task harus memiliki key: 'taskName', 'date', 'startTime', dan 'endTime'. " +
                "Contoh: [{'taskName': 'Waktu Tidur', 'date': '2025-11-02', 'startTime': '22:00', 'endTime': '06:00'}]";

        String userPrompt = "Tolong buatkan jadwal tidur 8 jam untuk saya malam ini, " +
                "tanggal " + todayDate + ". " +
                "Cari slot waktu 8 jam yang paling ideal antara jam 21:00 dan 07:00 keesokan harinya. " +
                "Ini adalah profil saya: " + profileJson + ". " +
                "Ini adalah jadwal sibuk saya (JANGAN menimpa jadwal ini): " + scheduleJson + ". " +
                "Pastikan jadwal tidur tidak bentrok.";

        callApi(systemPrompt, userPrompt, "Sleep", listener);
    }

    /**
     * Meminta AI untuk membuat beberapa jadwal pengingat minum
     * yang disesuaikan dengan jadwal sibuk pengguna.
     */
    public void adjustHydration(AIResponseListener listener) {
        UserProfile profile = db.getUserProfile(userEmail);
        List<Task> busySchedule = db.getTasksForUser(userEmail);

        if (profile == null) {
            listener.onError("User profile not found.");
            return;
        }

        String profileJson = gson.toJson(profile);
        String scheduleJson = gson.toJson(busySchedule);
        String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        String systemPrompt = "Anda adalah asisten kesehatan. Tugas Anda adalah membuat BEBERAPA jadwal pengingat minum " +
                "sepanjang hari. " +
                "Anda HARUS membalas HANYA dengan JSON Array yang berisi objek-objek Task. " +
                "Setiap objek Task harus memiliki key: 'taskName', 'date', 'startTime', dan 'endTime'. " +
                "Contoh: [{'taskName': 'Minum Air', 'date': '2025-11-02', 'startTime': '08:00', 'endTime': '08:05'}]";

        String userPrompt = "Tolong buatkan 5 jadwal pengingat minum (durasi 5 menit) untuk saya hari ini, " +
                "tanggal " + todayDate + ", yang tersebar antara jam 08:00 dan 17:00. " +
                "Ini adalah profil saya: " + profileJson + ". " +
                "Ini adalah jadwal sibuk saya (JANGAN menimpa jadwal ini): " + scheduleJson + ". " +
                "Pastikan pengingat minum tidak bentrok.";

        callApi(systemPrompt, userPrompt, "Hydration", listener);
    }


    /**
     * Fungsi inti yang memanggil API Gemini.
     * Menggunakan struktur "multi-turn chat" untuk memberi instruksi sistem.
     */
    private void callApi(String systemPrompt, String userPrompt, String logTag, AIResponseListener listener) {

        // --- Struktur Request untuk GEMINI ---
        List<Content> contents = new ArrayList<>();

        // 1. Masukkan instruksi sistem sebagai giliran "user"
        List<Part> systemParts = new ArrayList<>();
        systemParts.add(new Part(systemPrompt));
        contents.add(new Content(systemParts, "user"));

        // 2. Masukkan balasan "model" palsu untuk 'memaksa' AI mematuhi instruksi
        List<Part> modelParts = new ArrayList<>();
        modelParts.add(new Part("OK. Saya adalah asisten kesehatan dan akan membalas HANYA dengan JSON Array."));
        contents.add(new Content(modelParts, "model"));

        // 3. Masukkan prompt user yang sebenarnya sebagai giliran "user"
        List<Part> userParts = new ArrayList<>();
        userParts.add(new Part(userPrompt));
        contents.add(new Content(userParts, "user"));

        // Buat request body Gemini
        GeminiRequest request = new GeminiRequest(contents);
        // --- Batas Perubahan ---

        // Panggil ApiService (versi Gemini)
        apiService.getChatCompletion(request, RetrofitClient.API_KEY).enqueue(new Callback<GeminiResponse>() {
            @Override
            public void onResponse(Call<GeminiResponse> call, Response<GeminiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {

                    // Ambil teks jawaban dari helper di GeminiResponse
                    String aiResponseContent = response.body().getResponseText();

                    if (aiResponseContent == null) {
                        Log.w("AIAnalyticsHelper", "AI response content is null (" + logTag + ")");
                        listener.onError("AI tidak memberikan balasan.");
                        return;
                    }

                    // Membersihkan string JSON jika ada "```json" dan "```"
                    if (aiResponseContent.startsWith("```json\n")) {
                        aiResponseContent = aiResponseContent.substring(7, aiResponseContent.length() - 3).trim();
                    } else if (aiResponseContent.startsWith("```")) {
                        aiResponseContent = aiResponseContent.substring(3, aiResponseContent.length() - 3).trim();
                    }


                    try {
                        // Coba parsing JSON yang dibalas AI
                        Type taskListType = new TypeToken<ArrayList<Task>>(){}.getType();
                        List<Task> aiTasks = gson.fromJson(aiResponseContent, taskListType);

                        if (aiTasks == null || aiTasks.isEmpty()) {
                            listener.onError("AI tidak memberikan jadwal (null/empty).");
                            return;
                        }

                        // Simpan setiap tugas dari AI ke database
                        for (Task task : aiTasks) {
                            if (task.getTaskName() == null || task.getDate() == null || task.getStartTime() == null || task.getEndTime() == null) {
                                // Lewati task yang datanya tidak lengkap
                                Log.w("AIAnalyticsHelper", "AI task skipped due to null fields: " + gson.toJson(task));
                                continue;
                            }

                            db.insertTask(userEmail,
                                    "AI: " + task.getTaskName(), // Tambah prefix "AI:"
                                    task.getDate(),
                                    task.getStartTime(),
                                    task.getEndTime());
                        }
                        listener.onSuccess(); // Berhasil!

                    } catch (Exception e) {
                        Log.e("AIAnalyticsHelper", "Error parsing AI JSON (" + logTag + "): " + aiResponseContent, e);
                        listener.onError("AI memberikan balasan, tapi formatnya salah.");
                    }
                } else {
                    // Error dari server (misal: API key salah, 4xx, 5xx)
                    Log.e("AIAnalyticsHelper", "API Response Error (" + logTag + "): Code: " + response.code() + ", Message: " + response.message());
                    listener.onError("Gagal mendapat balasan dari server AI: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<GeminiResponse> call, Throwable t) {
                // Error jaringan (tidak ada internet, timeout)
                Log.e("AIAnalyticsHelper", "Network Error (" + logTag + ")", t);
                listener.onError("Error Jaringan: " + t.getMessage());
            }
        });
    }
}
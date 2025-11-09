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
        this.apiService = RetrofitClient.getApiService();
        this.gson = new Gson();
    }

    /**
     * BARU: Menerima targetDate
     */
    public void adjustDiet(AIResponseListener listener, String targetDate) {
        UserProfile profile = db.getUserProfile(userEmail);
        List<Task> busySchedule = db.getTasksForUser(userEmail);

        if (profile == null) {
            listener.onError("User profile not found.");
            return;
        }

        String profileJson = gson.toJson(profile);
        String scheduleJson = gson.toJson(busySchedule);

        // --- PROMPT DIPERTEGAS & DIUBAH ---
        String systemPrompt = "Anda adalah API JSON. JANGAN beri penjelasan. JANGAN gunakan markdown. " +
                "Tugas Anda adalah membuat **jadwal makan ideal** berdasarkan profil pengguna. " +
                "**ANDA HARUS MEMATUHI preferensi diet ('dietPref')** yang ada di dalam data profil. " +
                "Jika 'dietPref' bilang tidak sarapan, JANGAN BUAT jadwal 'Sarapan'. " +
                "Anda HARUS membalas HANYA dengan JSON Array yang berisi objek Task. " +
                "Contoh: [{'taskName': 'Makan Siang', 'date': '2025-11-02', 'startTime': '12:00', 'endTime': '12:30'}]";

        // --- USER PROMPT JUGA DIUBAH ---
        String userPrompt = "Buatkan **jadwal makan ideal** untuk saya, " +
                "pada tanggal " + targetDate + ". " +
                "Profil saya: " + profileJson + ". " +
                "Sangat penting: **Patuhi 'dietPref' (preferensi diet) dalam profil saya.** " +
                "Jadwal sibuk saya (JANGAN bentrok): " + scheduleJson;

        callApi(systemPrompt, userPrompt, "Diet", listener);
    }

    /**
     * BARU: Menerima targetDate
     */
    public void adjustSleep(AIResponseListener listener, String targetDate) {
        UserProfile profile = db.getUserProfile(userEmail);
        List<Task> busySchedule = db.getTasksForUser(userEmail);

        if (profile == null) {
            listener.onError("User profile not found.");
            return;
        }

        String profileJson = gson.toJson(profile);
        String scheduleJson = gson.toJson(busySchedule);

        String systemPrompt = "Anda adalah API JSON. JANGAN beri penjelasan. JANGAN gunakan markdown. " +
                "Tugas Anda adalah membuat SATU jadwal tidur ideal 8 jam. " +
                "Anda HARUS membalas HANYA dengan JSON Array yang berisi SATU objek Task. " +
                "Contoh: [{'taskName': 'Waktu Tidur', 'date': '2025-11-02', 'startTime': '22:00', 'endTime': '06:00'}]";

        String userPrompt = "Buatkan jadwal tidur 8 jam untuk saya, " +
                "pada tanggal " + targetDate + ", antara jam 21:00 dan 07:00. " + // <-- DIUBAH
                "Profil saya: " + profileJson + ". " +
                "Jadwal sibuk saya (JANGAN bentrok): " + scheduleJson;

        callApi(systemPrompt, userPrompt, "Sleep", listener);
    }

    /**
     * BARU: Menerima targetDate
     */
    public void adjustHydration(AIResponseListener listener, String targetDate) {
        UserProfile profile = db.getUserProfile(userEmail);
        List<Task> busySchedule = db.getTasksForUser(userEmail);

        if (profile == null) {
            listener.onError("User profile not found.");
            return;
        }

        String profileJson = gson.toJson(profile);
        String scheduleJson = gson.toJson(busySchedule);

        String systemPrompt = "Anda adalah API JSON. JANGAN beri penjelasan. JANGAN gunakan markdown. " +
                "Tugas Anda adalah membuat 5 jadwal pengingat minum. " +
                "Anda HARUS membalas HANYA dengan JSON Array yang berisi objek-objek Task. " +
                "Contoh: [{'taskName': 'Minum Air', 'date': '2025-11-02', 'startTime': '08:00', 'endTime': '08:05'}]";

        String userPrompt = "Buatkan 5 jadwal pengingat minum (durasi 5 menit) untuk saya, " +
                "pada tanggal " + targetDate + ", tersebar antara jam 08:00 dan 17:00. " + // <-- DIUBAH
                "Profil saya: " + profileJson + ". " +
                "Jadwal sibuk saya (JANGAN bentrok): " + scheduleJson;

        callApi(systemPrompt, userPrompt, "Hydration", listener);
    }


    /**
     * Fungsi inti (Tidak berubah)
     */
    private void callApi(String systemPrompt, String userPrompt, String logTag, AIResponseListener listener) {

        List<Message> messages = new ArrayList<>();
        messages.add(new Message("system", systemPrompt));
        messages.add(new Message("user", userPrompt));

        ApiRequest request = new ApiRequest("llama-3.1-8b-instant", messages);

        apiService.getChatCompletion(request, RetrofitClient.API_KEY).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().choices != null && !response.body().choices.isEmpty()) {

                    String aiResponseContent = response.body().choices.get(0).message.content;

                    if (aiResponseContent == null) {
                        Log.w("AIAnalyticsHelper", "AI response content is null (" + logTag + ")");
                        listener.onError("AI tidak memberikan balasan.");
                        return;
                    }

                    Log.d("AIAnalyticsHelper", "Raw AI Response (" + logTag + "): " + aiResponseContent);

                    String jsonOnlyString = null;
                    int startIndex = aiResponseContent.indexOf("[");
                    int endIndex = aiResponseContent.lastIndexOf("]");

                    if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
                        jsonOnlyString = aiResponseContent.substring(startIndex, endIndex + 1);
                    }

                    if (jsonOnlyString == null) {
                        Log.e("AIAnalyticsHelper", "AI response did not contain a JSON array: " + aiResponseContent);
                        listener.onError("AI tidak memberikan balasan JSON yang valid.");
                        return;
                    }

                    try {
                        Type taskListType = new TypeToken<ArrayList<Task>>(){}.getType();
                        List<Task> aiTasks = gson.fromJson(jsonOnlyString, taskListType);

                        if (aiTasks == null || aiTasks.isEmpty()) {
                            listener.onError("AI tidak memberikan jadwal (null/empty).");
                            return;
                        }

                        for (Task task : aiTasks) {
                            if (task.getTaskName() == null || task.getDate() == null || task.getStartTime() == null || task.getEndTime() == null) {
                                Log.w("AIAnalyticsHelper", "AI task skipped due to null fields: " + gson.toJson(task));
                                continue;
                            }

                            db.insertTask(userEmail,
                                    "AI: " + task.getTaskName(),
                                    task.getDate(),
                                    task.getStartTime(),
                                    task.getEndTime());
                        }
                        listener.onSuccess();

                    } catch (Exception e) {
                        Log.e("AIAnalyticsHelper", "Error parsing AI JSON (" + logTag + "): " + jsonOnlyString, e);
                        listener.onError("AI memberikan balasan, tapi formatnya salah (setelah diekstrak).");
                    }
                } else {
                    Log.e("AIAnalyticsHelper", "API Response Error (" + logTag + "): Code: " + response.code() + ", Message: " + response.message());
                    listener.onError("Gagal mendapat balasan dari server AI: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Log.e("AIAnalyticsHelper", "Network Error (" + logTag + ")", t);
                listener.onError("Error Jaringan: " + t.getMessage());
            }
        });
    }
}
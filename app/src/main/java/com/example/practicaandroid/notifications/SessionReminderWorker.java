package com.example.practicaandroid.notifications;
import android.app.PendingIntent;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

import com.example.practicaandroid.MainActivity;
import com.example.practicaandroid.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SessionReminderWorker extends Worker {

    private static final String CHANNEL_ID = "session_notifications";

    public SessionReminderWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        long sessionId = getInputData().getLong("id", -1L);
        String sessionTitle = getInputData().getString("nombre");
        long sessionTimestamp = getInputData().getLong("fecha", -1L);

        if (sessionId == -1L) {
            return Result.failure();
        }

        Context context = getApplicationContext();
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        CharSequence name = context.getString(R.string.not_channel_name);;
        String description = context.getString(R.string.not_channel_description);
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
        channel.setDescription(description);
        notificationManager.createNotificationChannel(channel);

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        String formattedTime = sdf.format(new Date(sessionTimestamp));

        String contentText = context.getString(
                R.string.notification_content_format,
                sessionTitle,
                formattedTime
        );

        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(sessionTitle)
                .setContentText(contentText)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        notificationManager.notify(Long.valueOf(sessionId).hashCode(), builder.build());

        return Result.success();
    }
}

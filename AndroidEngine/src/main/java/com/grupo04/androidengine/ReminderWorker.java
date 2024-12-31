package com.grupo04.androidengine;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.Manifest;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class ReminderWorker extends Worker {
    public ReminderWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        String packageName = getInputData().getString("package_name");
        String notifications_channel_id = getInputData().getString("channel_id");
        String key = getInputData().getString("key");
        String title = getInputData().getString("title");
        String message = getInputData().getString("message");
        int icon = getInputData().getInt("icon", 0);
        int priority = getInputData().getInt("priority", NotificationCompat.PRIORITY_DEFAULT);
        int visibility = getInputData().getInt("visibility", NotificationCompat.VISIBILITY_PUBLIC);

        if (notifications_channel_id == null || notifications_channel_id.isEmpty()) {
            notifications_channel_id = "default";
        }

        assert packageName != null;
        Intent intent = getApplicationContext().getPackageManager().getLaunchIntentForPackage(packageName);
        assert intent != null;
        intent.setData(Uri.parse(key));

        PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_IMMUTABLE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), notifications_channel_id)
                .setSmallIcon(icon)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(priority)
                .setVisibility(visibility)
                .setContentIntent(contentIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(getApplicationContext());
        // Dependiendo de la API (>= 33), hay que pedir permisos
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return Result.failure();
        }
        notificationManagerCompat.notify(1, builder.build());

        return Result.success();
    }
}

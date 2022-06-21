package app.cloudit.cloudit;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;

import java.io.File;

import app.cloudit.cloudit.ui.FileDetailsActivity;
import app.cloudit.cloudit.ui.MainActivity;

public class DownloadService extends Service {

    NotificationManager notificationManager;

    @Override
    public void onCreate() {
        super.onCreate();
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, final int startId) {

        final String fileName = intent.getStringExtra("fileName");
        final String fileId = intent.getStringExtra("fileId");
        final String mimeType = intent.getStringExtra("mimeType");

        startForeground(startId, setNotification(0,fileName));

        final StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(fileId);
        final File file = new File(getExternalFilesDir(null), fileName);
        storageReference.getFile(file).addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull FileDownloadTask.TaskSnapshot taskSnapshot) {
                int perc = (int) (taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());

                if (notificationManager != null) {
                    notificationManager.notify(startId, setNotification(perc,fileName));
                }
            }
        }).addOnCompleteListener(new OnCompleteListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<FileDownloadTask.TaskSnapshot> task) {
                if (task.isSuccessful()) {

                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK| Intent.FLAG_ACTIVITY_NEW_TASK);
                    PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);
                 Notification notification =   new Notification.Builder(getApplicationContext())
                            .setContentTitle(getString(R.string.download_complete))
                            .setAutoCancel(true)
                         .setChannelId("CLOUDIT")
                         .setSmallIcon(R.drawable.ic_baseline_file)
                            .setContentIntent(pendingIntent)
                            .setContentText(fileName).build();

                    notificationManager.notify((int)System.currentTimeMillis(), notification);
                    stopSelf(startId);

                } else {
                    Log.d("DOWNLOAD:",task.getException().getMessage());
                    Notification notification = new Notification.Builder(getApplicationContext())
                            .setContentTitle(getString(R.string.download_failed))
                            .setAutoCancel(true)
                            .setSmallIcon(R.drawable.ic_baseline_file)
                            .setChannelId("CLOUDIT")
                            .setContentText(fileName).build();

                    notificationManager.notify((int)System.currentTimeMillis(), notification);
                    stopSelf(startId);

                }
            }
        });


        return super.onStartCommand(intent, flags, startId);
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    private Notification setNotification(int perc , String filename) {
        return new Notification.Builder(getApplicationContext())
                .setContentTitle(getString(R.string.download_title))
                .setAutoCancel(false)
                .setOngoing(true)
                .setChannelId("CLOUDIT")
                .setSmallIcon(R.drawable.ic_baseline_file)
                .setProgress(100, perc, false)
                .setContentText(filename).build();
    }

    private void createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("CLOUDIT", name, importance);
            channel.setDescription(description);

            notificationManager.createNotificationChannel(channel);
        }
    }

}
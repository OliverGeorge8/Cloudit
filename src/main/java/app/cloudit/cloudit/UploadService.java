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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Date;

import app.cloudit.cloudit.models.File;
import app.cloudit.cloudit.ui.FileDetailsActivity;

import static app.cloudit.cloudit.Utils.getMimeType;

public class UploadService extends Service {


    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private NotificationManager notificationManager;

    @Override
    public void onCreate() {
        super.onCreate();
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel();


    }

    @Override
    public int onStartCommand(Intent intent, int flags, final int startId) {

        final DocumentReference documentReference = FirebaseFirestore.getInstance().collection("Files").document();
        String uri = intent.getStringExtra("uri");
        final Uri fileUri = Uri.parse(uri);

        final String mimeType = getMimeType(fileUri, getApplicationContext());


        final Notification notification = new Notification.Builder(getApplicationContext())
                .setContentTitle(getString(R.string.upload_notification_title))
                .setAutoCancel(false)
                .setChannelId("CLOUDIT")
                .setOngoing(true)
                .setSmallIcon(R.drawable.ic_baseline_file)
                .setProgress(100, 0, false)
                .setContentText(getString(R.string.upload_content_text)).build();

        startForeground(startId, notification);

        StorageMetadata metadata = new StorageMetadata.Builder().setContentType(mimeType).setCustomMetadata("isPrivate", "false").setCustomMetadata("uid",mAuth.getCurrentUser().getUid()).build();
        final StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(documentReference.getId());
        storageReference.putFile(fileUri, metadata).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {

                int progress = (int) ((100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount());


                Log.d("UPL", String.valueOf(progress));
                if (notificationManager != null) {
                    updateProgressNotification(startId, progress);
                }
            }
        }).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()) {
                    storageReference.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful()) {
                                Uri uri = task.getResult();
                                final File file = new File();

                                if (uri != null) {
                                    file.setUrl(uri.toString());
                                    file.setPrivate(true);
                                    file.setUploaderUID(mAuth.getCurrentUser().getUid());
                                    file.setFileSizeAndName(getApplicationContext(), fileUri);
                                    file.setMimeType(mimeType);

                                    documentReference.set(file, SetOptions.merge()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {

                                                Intent intent = new Intent(getApplicationContext(), FileDetailsActivity.class);
                                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                file.setCreatedAt(new Date());
                                                intent.putExtra("file", file);

                                                PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);
                                                Notification notificationDone = new Notification.Builder(getApplicationContext())
                                                        .setContentTitle(getString(R.string.upload_Complete))
                                                        .setContentText(getString(R.string.upload_complete_success))
                                                        .setSmallIcon(R.drawable.ic_baseline_file)
                                                        .setContentIntent(pendingIntent)
                                                        .setChannelId("CLOUDIT")
                                                        .setAutoCancel(true)
                                                        .build();
                                                notificationManager.notify((int)System.currentTimeMillis(), notificationDone);
                                                stopSelf(startId);
                                            } else {
                                                Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                                setFaildNotification(file.getName());
                                                stopSelf(startId);
                                            }
                                        }
                                    });
                                } else {
                                    Toast.makeText(getApplicationContext(), "Failed", Toast.LENGTH_LONG).show();
                                    setFaildNotification(file.getName());
                                    stopSelf(startId);
                                }

                            }else {
                                Log.d("SECU",task.getException().getMessage());
                            }
                        }
                    });
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


    private void updateProgressNotification(int notId, int perc) {
        Log.d("UPLOADSERVICE:", String.valueOf(notId));
        Notification notification = new Notification.Builder(getApplicationContext())
                .setContentTitle(getString(R.string.upload_notification_title))
                .setAutoCancel(false)
                .setOngoing(true)
                .setSmallIcon(R.drawable.ic_baseline_file)
                .setProgress(100, perc, false)
                .setChannelId("CLOUDIT")
                .setContentText(getString(R.string.upload_content_text)).build();

        notificationManager.notify(notId, notification);
    }

    private void setFaildNotification( String filename) {
        Notification notification = new Notification.Builder(getApplicationContext())
                .setContentTitle(getString(R.string.upload_failed))
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.ic_baseline_file)
                .setOngoing(true)
                .setChannelId("CLOUDIT")
                .setContentText(filename).build();

        if (notificationManager != null) notificationManager.notify((int)System.currentTimeMillis(), notification);

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

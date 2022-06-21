package app.cloudit.cloudit.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.dynamiclinks.ShortDynamicLink;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;

import java.text.SimpleDateFormat;
import java.util.Locale;

import app.cloudit.cloudit.DownloadService;
import app.cloudit.cloudit.R;
import app.cloudit.cloudit.databinding.ActivityFileDetailsBinding;
import app.cloudit.cloudit.models.File;
import app.cloudit.cloudit.models.MyTask;
import app.cloudit.cloudit.models.OnComplete;

import static app.cloudit.cloudit.Utils.createLink;

public class FileDetailsActivity extends AppCompatActivity {

    File file ; SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm dd-MM-yyyy", Locale.ENGLISH);
    FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    FirebaseStorage storage = FirebaseStorage.getInstance();
    ActivityFileDetailsBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_file_details);

        file = getIntent().getParcelableExtra("file");
getSupportActionBar().setTitle(file.getName());
getSupportActionBar().setDisplayHomeAsUpEnabled(true);

binding.createdText.setText(simpleDateFormat.format(file.getCreatedAt()));



        int drawableId = 0 ;
        if (file.getMimeType().contains("image/")) {
            Glide.with(getApplicationContext()).load(file.getUrl()).into(binding.fileImage);
        } else if (file.getMimeType().contains("audio/")) {
            drawableId = R.drawable.ic_baseline_music;
        } else if (file.getMimeType().contains("video/")) {
            drawableId = R.drawable.ic_baseline_video;
        } else {
            drawableId = R.drawable.ic_baseline_file;
        }

        if (drawableId != 0){
            binding.fileImage.setImageResource(drawableId);
        }


        binding.fileName.setText(file.getName());
        binding.fileType.setText(file.getMimeType());
        binding.fileSize.setText(file.getFileSize());

        if (file.isPrivate()) {
            binding.privateSwitch.setChecked(true);
        }

        binding.privateSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {

                firestore.collection("Files").document(file.getDocumentId()).update("isPrivate", isChecked).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            storage.getReference().child(file.getDocumentId()).updateMetadata(new StorageMetadata.Builder().setCustomMetadata("isPrivate", Boolean.toString(isChecked)).setContentType(file.getMimeType()).build()).addOnCompleteListener(new OnCompleteListener<StorageMetadata>() {
                                @Override
                                public void onComplete(@NonNull Task<StorageMetadata> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(getApplicationContext(),R.string.file_updated,Toast.LENGTH_LONG).show();
                                    } else {
                                        Toast.makeText(getApplicationContext(),task.getException().getMessage(),Toast.LENGTH_LONG).show();

                                    }
                                }
                            });
                        } else {
                            Toast.makeText(getApplicationContext(),task.getException().getMessage(),Toast.LENGTH_LONG).show();

                        }
                    }
                });

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_download:{

                java.io.File dirFile = new java.io.File(getExternalFilesDir(null),file.getName());
                if (!dirFile.exists()){
                    Intent serviceIntent = new Intent(getApplicationContext(), DownloadService.class);
                    serviceIntent.putExtra("fileName",file.getName());
                    serviceIntent.putExtra("mimeType",file.getMimeType());
                    serviceIntent.putExtra("fileId",file.getDocumentId());
                    startService(serviceIntent);
                }else {
                    Toast.makeText(getApplicationContext(),"File Already Downloaded",Toast.LENGTH_LONG).show();
                }
              return true ;
            }
            case R.id.action_share:{
                final Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.setType("text/plain");
                if (file.getDynamicLink() != null) {
                    sendIntent.putExtra(Intent.EXTRA_TEXT, file.getDynamicLink());
                    final Intent shareIntent = Intent.createChooser(sendIntent, "Share File Link");
                    startActivity(shareIntent);
                } else {
                    createLink(file, new OnComplete<String>() {
                        @Override
                        public void onComplete(MyTask<String> task) {

                            if (task.getSuccessFull()) {
                                file.setDynamicLink(task.getValue());
                                sendIntent.putExtra(Intent.EXTRA_TEXT, task.getValue());
                                final Intent shareIntent = Intent.createChooser(sendIntent, "Share File Link");
                                startActivity(shareIntent);

                            } else {

                                Toast.makeText(getApplicationContext(), task.getE().getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
return  true;
            }
            case android.R.id.home: {
                onBackPressed();
                return true;

            }
            default:{
                return super.onOptionsItemSelected(item);
            }
        }

    }
}

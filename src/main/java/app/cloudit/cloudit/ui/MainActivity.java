package app.cloudit.cloudit.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Date;

import app.cloudit.cloudit.FilesAdapter;
import app.cloudit.cloudit.R;
import app.cloudit.cloudit.UploadService;
import app.cloudit.cloudit.databinding.ActivityMainBinding;
import app.cloudit.cloudit.databinding.UserDialogBinding;
import app.cloudit.cloudit.models.File;

public class MainActivity extends AppCompatActivity implements FilesAdapter.OnClick {

    public static int REQUEST_IMAGE_GET = 1;
    public static int PERMISSION_REQUEST = 10;
    ActivityMainBinding binding;
    private FilesAdapter mAdapter;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    ConnectivityManager connectivityManager ;
    ConnectivityManager.NetworkCallback callback ;
    Snackbar snackbar ;
        boolean isConnected ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding =  DataBindingUtil.setContentView(this,R.layout.activity_main);
    connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        if(!isConnected()){
            snackbar  = Snackbar.make(binding.fab,"No Internet",Snackbar.LENGTH_INDEFINITE);
            snackbar.show();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            callback =  new ConnectivityManager.NetworkCallback() {
               @Override
               public void onLost(@NonNull Network network) {
                   super.onLost(network);
                   isConnected = false ;
                   if (snackbar == null){
                     snackbar   = Snackbar.make(binding.fab,"No Internet",Snackbar.LENGTH_INDEFINITE);
                   }
                snackbar.show();
               }

               @Override
               public void onAvailable(@NonNull Network network) {
                   super.onAvailable(network);

                   isConnected = true ;
                   if (snackbar != null) snackbar.dismiss();
               }
           };
        }

        mAdapter = new FilesAdapter(this);
        binding.filesRecView.setAdapter(mAdapter);
        binding.filesRecView.setHasFixedSize(true);

        FirebaseFirestore.getInstance().collection("Files").whereEqualTo("uploaderUID", mAuth.getCurrentUser().getUid()).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {


                if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                    if (binding.noFilesGroup.getVisibility() == View.VISIBLE){
                        binding.noFilesGroup.setVisibility(View.GONE);
                    }
                    for (DocumentChange dc : queryDocumentSnapshots.getDocumentChanges()) {
                        switch (dc.getType()) {
                            case ADDED:
                                File file = dc.getDocument().toObject(File.class);
                                file.setDocumentId(dc.getDocument().getId());
                                if (file.getCreatedAt() == null) {
                                    file.setCreatedAt(new Date());
                                }
                                mAdapter.addFile(file);
                                break;
                            case MODIFIED:
                                File fileMod = dc.getDocument().toObject(File.class);
                                fileMod.setDocumentId(dc.getDocument().getId());
                                mAdapter.addModifiedFile(fileMod);
                                break;
                            case REMOVED:
                                mAdapter.removeFile(dc.getDocument().getId());
                            default:
                                break;
                        }
                    }

                } else {
                    mAdapter.clear();
                    binding.noFilesGroup.setVisibility(View.VISIBLE);

                }

            }
        });

        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isConnected){
                    getPermissionOrSelectFile();
                }

            }
        });
    }


    public void selectFile() {

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_IMAGE_GET);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            connectivityManager.registerNetworkCallback(
                 new    NetworkRequest.Builder()
                            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET).build(),
                    callback);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            connectivityManager.unregisterNetworkCallback(callback);
        }
    }

    public void getPermissionOrSelectFile() {

        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST);
        } else {
            selectFile();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0) {
            selectFile();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_GET && resultCode == RESULT_OK){
            Uri fullFileUri = data.getData();
            Intent serviceIntent = new Intent(getApplicationContext(), UploadService.class);
            serviceIntent.putExtra("uri", fullFileUri.toString());
            startService(serviceIntent);
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.user,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        showUserDialog();
        return true ;
    }

    @Override
    public void onClick(View v, File file) {
        Intent intent = new Intent(MainActivity.this, FileDetailsActivity.class);
        intent.putExtra("file", file);
        startActivity(intent);
    }

    @Override
    public boolean onLongClick(View v, File file) {

        FileBottomSheet fileBottomSheet = new FileBottomSheet(file);
        fileBottomSheet.show(getSupportFragmentManager(), "");
        return true;
    }

    public boolean isConnected() {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
        }

        public void showUserDialog(){
            UserDialogBinding userDialogBinding = DataBindingUtil.inflate(getLayoutInflater(),R.layout.user_dialog,null,false);
            userDialogBinding.signoutButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mAuth.signOut();
                    Intent intent = new Intent(MainActivity.this,StarterActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            });

            Glide.with(getApplicationContext()).load(mAuth.getCurrentUser().getPhotoUrl()).apply(RequestOptions.circleCropTransform()).into(userDialogBinding.userImage);
            userDialogBinding.userEmail.setText(mAuth.getCurrentUser().getEmail());
            userDialogBinding.userName.setText(mAuth.getCurrentUser().getDisplayName());
         new MaterialAlertDialogBuilder(this).setCancelable(true).setView(userDialogBinding.getRoot()).create().show();
        }
    }


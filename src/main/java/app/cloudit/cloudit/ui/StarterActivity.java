package app.cloudit.cloudit.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import app.cloudit.cloudit.R;
import app.cloudit.cloudit.databinding.ActivityStarterBinding;
import app.cloudit.cloudit.models.File;

public class StarterActivity extends AppCompatActivity {


    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseDynamicLinks dynamicLinks = FirebaseDynamicLinks.getInstance();
    FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    ActivityStarterBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_starter);

        if (mAuth.getCurrentUser() == null) {


            getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new AuthFragment()).commit();

        } else {

            dynamicLinks.getDynamicLink(getIntent()).addOnCompleteListener(new OnCompleteListener<PendingDynamicLinkData>() {
                @Override
                public void onComplete(@NonNull Task<PendingDynamicLinkData> task) {

                    if (task.isSuccessful()) {
                        Uri deepLink ;
                        PendingDynamicLinkData pendingDynamicLinkData = task.getResult();
                        if (pendingDynamicLinkData != null) {
                            deepLink = pendingDynamicLinkData.getLink();

                            final String docId = deepLink.getQueryParameter("docId");

                            if (docId != null) {

                                firestore.collection("Files").document(docId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                    @Override
                                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                                        if (documentSnapshot != null && documentSnapshot.exists()) {

                                            File file = documentSnapshot.toObject(File.class);
                                            file.setDocumentId(documentSnapshot.getId());

                                            if (file.isPrivate() && !file.getUploaderUID().equals(mAuth.getCurrentUser().getUid())){
                                                goToMain();
                                                return;
                                            }
                                            Intent intent = new Intent(getApplicationContext(), FileDetailsActivity.class);
                                            intent.putExtra("file", file);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                            startActivity(intent);

                                        }
                                    }
                                });
                            }else {
                                goToMain();
                            }


                        } else {
                            goToMain();
                        }


                    } else {
                        Toast.makeText(getApplicationContext(),task.getException().getMessage(),Toast.LENGTH_LONG).show();
                        goToMain();

                    }
                }
            });


        }
    }

    private void goToMain() {
        Intent mainActivityIntent = new Intent(this, MainActivity.class);
        mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(mainActivityIntent);
    }
}

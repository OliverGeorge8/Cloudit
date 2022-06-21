package app.cloudit.cloudit.ui;


import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

import app.cloudit.cloudit.R;
import app.cloudit.cloudit.databinding.FragmentAuthBinding;

import static android.app.Activity.RESULT_OK;

public class AuthFragment extends Fragment {

    private FragmentAuthBinding binding ;
    private Intent signInIntent ;
    private static int rcSignIn = 100 ;

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();


    public AuthFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initGoogleSignIn();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_auth, container, false);

        binding.gBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                binding.gBtn.setVisibility(View.GONE);
                binding.progressBar.setVisibility(View.VISIBLE);

             startActivityForResult(signInIntent,rcSignIn);
            }
        });

        return binding.getRoot();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == rcSignIn){
            handleAuthIntent(requestCode,resultCode,data);
        }
    }

    private void  initGoogleSignIn() {

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(getActivity(), gso);
        signInIntent = mGoogleSignInClient.getSignInIntent();

    }


    private void handleToken(String token) {
        AuthCredential credential = GoogleAuthProvider.getCredential(token, null);

        mAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful()){

                    Intent intent = new Intent(getActivity(),MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);

                }else {
                    Toast.makeText(getContext(),task.getException().getMessage(),Toast.LENGTH_LONG).show();
                    binding.gBtn.setVisibility(View.VISIBLE);
                    binding.progressBar.setVisibility(View.GONE);
                }


            }

        });

    }

    public   void handleAuthIntent(int requestCode , int resultCode, Intent data) {

        if (requestCode == rcSignIn && resultCode == RESULT_OK) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            GoogleSignInAccount account ;
            try {
                account = task.getResult(ApiException.class);
                if (account != null && account.getIdToken() != null) {

                    handleToken(account.getIdToken());

                } else {
                    binding.gBtn.setVisibility(View.VISIBLE);
                    binding.progressBar.setVisibility(View.GONE);

                }
            } catch (ApiException e) {
               Toast.makeText(getContext(),e.getMessage(),Toast.LENGTH_LONG).show();
                binding.gBtn.setVisibility(View.VISIBLE);
                binding.progressBar.setVisibility(View.GONE);
            }

        }
    }
}

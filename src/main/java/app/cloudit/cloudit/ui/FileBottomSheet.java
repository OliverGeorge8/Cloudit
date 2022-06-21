package app.cloudit.cloudit.ui;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import app.cloudit.cloudit.R;
import app.cloudit.cloudit.databinding.BottomSheetBinding;
import app.cloudit.cloudit.models.File;
import app.cloudit.cloudit.models.MyTask;
import app.cloudit.cloudit.models.OnComplete;

import static app.cloudit.cloudit.Utils.createLink;

public class FileBottomSheet extends BottomSheetDialogFragment {

    private File file;
    private ClipboardManager clipboardManager;

    public FileBottomSheet(File mFile) {

        this.file = mFile;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        clipboardManager = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);

    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        BottomSheetDialog bottomSheetDialogFragment = new BottomSheetDialog(getActivity());
        final BottomSheetBinding binding = DataBindingUtil.inflate(LayoutInflater.from(getActivity()), R.layout.bottom_sheet, null, false);


        binding.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               binding.progressBar.setVisibility(View.VISIBLE);
                FirebaseStorage.getInstance().getReference().child(file.getDocumentId()).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            FirebaseFirestore.getInstance().collection("Files").document(file.getDocumentId()).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    binding.progressBar.setVisibility(View.GONE);
                                    if (task.isSuccessful()) {
                                        dismiss();
                                    } else {
                                        Toast.makeText(getContext(), task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                        } else {
                            Toast.makeText(getContext(), task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });

        binding.copyLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (file.getDynamicLink() != null) {

                    ClipData data = ClipData.newPlainText("Link", file.getDynamicLink());
                    clipboardManager.setPrimaryClip(data);
                    Toast.makeText(getContext(), R.string.link_copied, Toast.LENGTH_LONG).show();
                } else {
                    binding.progressBar.setVisibility(View.VISIBLE);
                    createLink(file, new OnComplete<String>() {
                        @Override
                        public void onComplete(MyTask<String> task) {
                            binding.progressBar.setVisibility(View.GONE);
                            if (task.getSuccessFull()) {
                                file.setDynamicLink(task.getValue());
                                clipboardManager.setPrimaryClip(ClipData.newPlainText("link", task.getValue()));
                                Toast.makeText(getContext(), R.string.link_copied, Toast.LENGTH_LONG).show();
                            }else{
                                Toast.makeText(getContext(), task.getE().getMessage(), Toast.LENGTH_LONG).show();

                            }
                        }
                    });
                    }

            }
        });
        binding.share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.setType("text/plain");
                if (file.getDynamicLink() != null) {
                    sendIntent.putExtra(Intent.EXTRA_TEXT, file.getDynamicLink());
                    final Intent shareIntent = Intent.createChooser(sendIntent, "Share File Link");
                    startActivity(shareIntent);
                } else {
                    binding.progressBar.setVisibility(View.VISIBLE);
                    createLink(file, new OnComplete<String>() {
                        @Override
                        public void onComplete(MyTask<String> task) {
                            binding.progressBar.setVisibility(View.GONE);
                            if (task.getSuccessFull()) {
                                file.setDynamicLink(task.getValue());
                                sendIntent.putExtra(Intent.EXTRA_TEXT, task.getValue());
                                final Intent shareIntent = Intent.createChooser(sendIntent, "Share File Link");
                                startActivity(shareIntent);

                            } else {

                                Toast.makeText(getContext(), task.getE().getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }


            }
        });

        binding.fileName.setText(file.getName());


        binding.detail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getActivity(), FileDetailsActivity.class);
                intent.putExtra("file", file);
                startActivity(intent);
            }
        });


        bottomSheetDialogFragment.setCancelable(true);
        bottomSheetDialogFragment.setDismissWithAnimation(true);
        bottomSheetDialogFragment.setContentView(binding.getRoot());
        bottomSheetDialogFragment.setTitle(file.getName());
        return bottomSheetDialogFragment;

    }


}

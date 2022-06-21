package app.cloudit.cloudit;

import android.content.ClipData;
import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.ShortDynamicLink;
import com.google.firebase.firestore.FirebaseFirestore;

import app.cloudit.cloudit.models.File;
import app.cloudit.cloudit.models.MyTask;
import app.cloudit.cloudit.models.OnComplete;


public class Utils {


    public static String imageUrl = "https://firebasestorage.googleapis.com/v0/b/mini-project-college.appspot.com/o/statics%2Fpicture.png?alt=media&token=7354b910-d8ef-452c-a956-d9704585d230";
    public  static  String videoUrl = "https://firebasestorage.googleapis.com/v0/b/mini-project-college.appspot.com/o/statics%2Fplay.png?alt=media&token=4aba1a58-cb22-48f8-af93-d8c614f11f9a";
    public  static  String audioUrl = "https://firebasestorage.googleapis.com/v0/b/mini-project-college.appspot.com/o/statics%2Fheadphones.png?alt=media&token=69abf35a-e701-4a71-9019-7af2df6f5494";
    public  static  String fileUrl  = "https://firebasestorage.googleapis.com/v0/b/mini-project-college.appspot.com/o/statics%2Ffolder.png?alt=media&token=c870c1dd-e4b1-4505-ab6b-3f26a644614d";

    public  static String getMimeType(Uri uri , Context context) {
        String mimeType ;
        if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            ContentResolver cr = context.getContentResolver();
            mimeType = cr.getType(uri);
        } else {
            String fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri
                    .toString());
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                    fileExtension.toLowerCase());
        }
        return   mimeType;
    }

    public  static void createLink(final File file , final OnComplete<String> onComplete){
        final MyTask<String> myTask = new MyTask<>();
        DynamicLink.SocialMetaTagParameters.Builder socialMetaTagParameters = new DynamicLink.SocialMetaTagParameters.Builder().setDescription("File").setTitle(file.getName());

        if (file.getMimeType().contains("image/")) {
            socialMetaTagParameters.setImageUrl(Uri.parse(imageUrl));
        } else if (file.getMimeType().contains("audio/")) {
            socialMetaTagParameters.setImageUrl(Uri.parse(audioUrl));
        } else if (file.getMimeType().contains("video/")) {
            socialMetaTagParameters.setImageUrl(Uri.parse(videoUrl));
        } else {
            socialMetaTagParameters.setImageUrl(Uri.parse(fileUrl));
        }
        FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLink(Uri.parse("https://www.cloudit.com/?docId=" + file.getDocumentId()))
                .setDomainUriPrefix("https://cloudit.page.link")
                .setSocialMetaTagParameters(socialMetaTagParameters.build())
                .setAndroidParameters(new DynamicLink.AndroidParameters.Builder().build())
                .buildShortDynamicLink(ShortDynamicLink.Suffix.SHORT).addOnCompleteListener(new OnCompleteListener<ShortDynamicLink>() {
            @Override
            public void onComplete(@NonNull Task<ShortDynamicLink> task) {
                if (task.isSuccessful()) {
                    final String link = task.getResult().getShortLink().toString();
                    FirebaseFirestore.getInstance().collection("Files")
                            .document(file.getDocumentId()).update("dynamicLink", link).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()) {
                                myTask.setSuccessFull(true);
                                myTask.setValue(link);
                            }else {
                               myTask.setSuccessFull(false);
                               myTask.setE(task.getException());
                            }
                            onComplete.onComplete(myTask);
                        }
                    });
                } else {
                    myTask.setSuccessFull(false);
                    myTask.setE(task.getException());
                    onComplete.onComplete(myTask);
                }
            }
        });
    }
}

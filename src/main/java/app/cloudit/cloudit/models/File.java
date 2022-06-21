package app.cloudit.cloudit.models;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.OpenableColumns;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class File implements Parcelable {

    private  String url ;
    private String name ;
    private String mimeType ;
    private  boolean isPrivate ;
    private  String uploaderUID ;

    @ServerTimestamp
    private Date createdAt ;
    private String fileSize ;
   private String dynamicLink ;
   @Exclude
   private String DocumentId ;



    public File(){

    }

    public  void setMimeType(Uri uri , Context context) {
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
        this.mimeType =  mimeType;
    }

    public void setFileSizeAndName(Context context , Uri fileUri){


        Cursor returnCursor =
                context.getContentResolver().query(fileUri, null, null, null, null);


        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
        returnCursor.moveToFirst();
        long bytes = returnCursor.getLong(sizeIndex);
        Log.d("FILE:",String.valueOf(bytes));
        this.name = returnCursor.getString(nameIndex);

        int kBytes = (int) bytes / 1024 ;
        if (kBytes < 1024){
           this.fileSize =   kBytes + " KB" ;
           return;
        }

        int mBytes =  kBytes / 1024 ;

        if (mBytes < 1024){
            this.fileSize =  mBytes + " MB" ;
        }
        else {
            this.fileSize =  "UNKNOWN" ;
        }
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public void setPrivate(boolean aPrivate) {
        isPrivate = aPrivate;
    }

    public String getUploaderUID() {
        return uploaderUID;
    }

    public void setUploaderUID(String uploaderUID) {
        this.uploaderUID = uploaderUID;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getFileSize() {
        return fileSize;
    }

    public void setFileSize(String fileSize) {
        this.fileSize = fileSize;
    }

    public String getDynamicLink() {
        return dynamicLink;
    }

    public void setDynamicLink(String dynamicLink) {
        this.dynamicLink = dynamicLink;
    }

    public String getDocumentId() {
        return DocumentId;
    }

    public void setDocumentId(String documentId) {
        DocumentId = documentId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.url);
        dest.writeString(this.name);
        dest.writeString(this.mimeType);
        dest.writeByte(this.isPrivate ? (byte) 1 : (byte) 0);
        dest.writeString(this.uploaderUID);
        dest.writeLong(this.createdAt != null ? this.createdAt.getTime() : -1);
        dest.writeString(this.fileSize);
        dest.writeString(this.dynamicLink);
        dest.writeString(this.DocumentId);
    }

    protected File(Parcel in) {
        this.url = in.readString();
        this.name = in.readString();
        this.mimeType = in.readString();
        this.isPrivate = in.readByte() != 0;
        this.uploaderUID = in.readString();
        long tmpCreatedAt = in.readLong();
        this.createdAt = tmpCreatedAt == -1 ? null : new Date(tmpCreatedAt);
        this.fileSize = in.readString();
        this.dynamicLink = in.readString();
        this.DocumentId = in.readString();
    }

    public static final Parcelable.Creator<File> CREATOR = new Parcelable.Creator<File>() {
        @Override
        public File createFromParcel(Parcel source) {
            return new File(source);
        }

        @Override
        public File[] newArray(int size) {
            return new File[size];
        }
    };
}

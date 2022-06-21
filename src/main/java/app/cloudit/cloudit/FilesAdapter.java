package app.cloudit.cloudit;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import app.cloudit.cloudit.databinding.FileViewholderBinding;
import app.cloudit.cloudit.models.File;

public class FilesAdapter extends RecyclerView.Adapter<FilesAdapter.FileViewHolder> {

    OnClick clickListener ;
    List<File> allFiles = new ArrayList<>();

    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm dd-MM-yyyy",Locale.ENGLISH);

    public FilesAdapter(OnClick clkListener ){
        this.clickListener = clkListener ;
    }


    public void addFile(File file){
        allFiles.add(file);
        notifyDataSetChanged();
    }

    public void addModifiedFile(File file){
        for (int i = 0 ; i < allFiles.size(); i++){

            if (allFiles.get(i).getDocumentId().equals(file.getDocumentId())){
                allFiles.set(i,file);
            }
        }
        notifyDataSetChanged();
    }

    public void clear(){
        allFiles.clear();
        notifyDataSetChanged();
    }

    public void removeFile(String docId){
        for (int i = 0 ; i < allFiles.size(); i++){

            if (allFiles.get(i).getDocumentId().equals(docId)){
                allFiles.remove(i);
            }
        }
        notifyDataSetChanged();
    }
    @NonNull
    @Override
    public FileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater  = LayoutInflater.from(parent.getContext());
        FileViewholderBinding  binding = DataBindingUtil.inflate(layoutInflater,R.layout.file_viewholder,parent,false);
        return new FileViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull FileViewHolder holder, int position) {

        File file = allFiles.get(position);
        holder.Bind(file);

    }

    @Override
    public int getItemCount() {
        return allFiles.size();
    }

    public class FileViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        FileViewholderBinding binding ;
        Context context ;
        public FileViewHolder(@NonNull FileViewholderBinding mBinding) {
            super(mBinding.getRoot());
            this.binding = mBinding ;
            context = binding.getRoot().getContext();
            binding.getRoot().setOnClickListener(this);
            binding.getRoot().setOnLongClickListener(this);

        }

        public void Bind(final File file ){


            binding.fileName.setText(file.getName());
            binding.fileDate.setText(simpleDateFormat.format(file.getCreatedAt()));


            binding.fileAction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clickListener.onLongClick(binding.getRoot(),file);
                }
            });

            int drawableId  ;
            if (file.getMimeType().contains("image/")) {
Glide.with(context).load(file.getUrl()).into(binding.fileImageView);
return;
            } else if (file.getMimeType().contains("audio/")) {
                drawableId = R.drawable.ic_baseline_music;
            } else if (file.getMimeType().contains("video/")) {
                drawableId = R.drawable.ic_baseline_video;
            } else {
                drawableId = R.drawable.ic_baseline_file;
            }
            binding.fileImageView.setImageResource(drawableId);

        }

        @Override
        public void onClick(View v) {
            clickListener.onClick(v, allFiles.get(getAdapterPosition()));
        }

        @Override
        public boolean onLongClick(View v) {

            return clickListener.onLongClick(v,allFiles.get(getAdapterPosition()));
        }
    }

  public  interface OnClick {
      void  onClick(View v , File file );
        boolean onLongClick(View v , File file );
    }
}

package app.cloudit.cloudit.models;

public interface OnComplete<T> {
    void onComplete(MyTask<T> task);
}
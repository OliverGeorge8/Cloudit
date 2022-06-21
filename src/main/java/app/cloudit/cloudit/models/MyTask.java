package app.cloudit.cloudit.models;

public class MyTask<T> {

  private   T value ;
  private   Exception e ;
  private   Boolean isSuccessFull ;

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public Exception getE() {
        return e;
    }

    public void setE(Exception e) {
        this.e = e;
    }

    public Boolean getSuccessFull() {
        return isSuccessFull;
    }

    public void setSuccessFull(Boolean successFull) {
        isSuccessFull = successFull;
    }
}
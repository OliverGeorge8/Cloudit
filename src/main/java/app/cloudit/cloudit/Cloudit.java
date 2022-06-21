package app.cloudit.cloudit;

import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;

public class Cloudit extends MultiDexApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        MultiDex.install(this);
    }
}

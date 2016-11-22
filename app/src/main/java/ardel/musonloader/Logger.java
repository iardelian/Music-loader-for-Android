package ardel.musonloader;

import android.util.Log;

public class Logger implements Constants {
    private static Logger instance = new Logger();

    public static Logger getInstance() {
        if (instance == null) {
            instance = new Logger();
        }
        return instance;

    }

    private Logger() {}

    public void log(String from, String s) {
        if (LOG) Log.e(LOG_TAG, from + " *** " + s);
    }
}
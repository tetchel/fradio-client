package ca.fradio;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Listener extends Service {

    private static final String TAG = "ListenerService";

    private static final int PORT = 16987;

    private ServerSocket listenerSocket;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("LocalService", "Received start id " + startId + ": " + intent);

        try {
            listenerSocket = new ServerSocket(PORT, 0, null);
            while (!Thread.currentThread().isInterrupted() && !listenerSocket.isClosed()) {
                Log.d(TAG, "Ready to accept");
                Socket acceptance = listenerSocket.accept();

                String request = Utility.readAllFromInputStream(acceptance.getInputStream());

                Log.d(TAG, request);
            }
        } catch (IOException e) {
            // Nothing at all will work
            e.printStackTrace();
            stopSelf();
        }



        return START_NOT_STICKY;
    }

}

package ca.fradio;

import android.app.Service;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Listener extends Service {


    // Singleton instance to be called to get access to the Application Context from static code
    private static Listener INSTANCE;

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
        INSTANCE = this;

        try {
            listenerSocket = new ServerSocket(PORT, 0, null);
            ListenerThread listenerThread = new ListenerThread(listenerSocket);
            listenerThread.start();
        } catch (IOException e) {
            e.printStackTrace();
            stopSelf();
        }

        return START_NOT_STICKY;
    }


    /**
     * @return If this service is running. Only one instance of this service can run at a time.
     */
    public static boolean isRunning() {
        Listener instance = instance();
        if(instance == null) {
            return false;
        }

        ActivityManager manager = (ActivityManager) instance.getApplicationContext()
                .getSystemService(Context.ACTIVITY_SERVICE);

        for (ActivityManager.RunningServiceInfo runningService : manager
                .getRunningServices(Integer.MAX_VALUE)) {

            if (Listener.class.getName().equals(runningService.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Access the singleton instance of this class for getting the context.
     *
     * @return The singleton instance.
     */
    public static Listener instance() {
        return INSTANCE;
    }

}


class ListenerThread extends Thread {

    private static final String TAG = "ListenerService";
    private static final int PORT = 16987;

    private ServerSocket listenerSocket;

    public ListenerThread(ServerSocket listenerSock) {
        listenerSocket = listenerSock;
    }

    public void run(){
        listenForBroadcasts();
    }

    private void listenForBroadcasts() {
        try {
            while (!Thread.currentThread().isInterrupted() && !listenerSocket.isClosed()) {
                Log.d(TAG, "Ready to accept");
                Socket acceptance = listenerSocket.accept();

                String request = Utility.readAllFromInputStream(acceptance.getInputStream());

                Log.d(TAG, request);
            }
        } catch (IOException e){
            e.printStackTrace();
        }

    }

}

package ca.fradio.net;

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

public class StreamerStateListener extends Service {

    private static final String TAG = "ListenerService";

    // Singleton instance to be called to get access to the Application Context from static code
    private static StreamerStateListener INSTANCE;

    private static final int PORT = 16987;

    private ServerSocket listenerSocket;
    private ListenerThread listenerThread;

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
            //listenerSocket = new ServerSocket(PORT, 0, null);
            listenerSocket = new ServerSocket(PORT);
            listenerThread = new ListenerThread(listenerSocket);
            listenerThread.start();
        } catch (IOException e) {
            e.printStackTrace();
            stopSelf();
        }

        return START_NOT_STICKY;
    }

    @Override
        public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Enter onDestroy");

        listenerThread.interrupt();
        try {
            if (listenerSocket != null && !listenerSocket.isClosed()) {
                listenerSocket.close();
                Log.d(TAG, "Closed ServerSocket");
            } else {
                Log.w(TAG, "ServerSocket was not open!");
            }
        } catch (IOException e) {
            Log.e(TAG, "Exception closing ServerSocket", e);
        }
    }

    /**
     * @return If this service is running. Only one instance of this service can run at a time.
     */
    public static boolean isRunning() {
        StreamerStateListener instance = instance();
        if(instance == null) {
            return false;
        }

        ActivityManager manager = (ActivityManager) instance.getApplicationContext()
                .getSystemService(Context.ACTIVITY_SERVICE);

        for (ActivityManager.RunningServiceInfo runningService : manager
                .getRunningServices(Integer.MAX_VALUE)) {

            if (StreamerStateListener.class.getName().equals(runningService.service.getClassName())) {
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
    public static StreamerStateListener instance() {
        return INSTANCE;
    }

}


class ListenerThread extends Thread {

    private static final String TAG = "ListenerThread";

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
                Log.d(TAG, "Accepted something");

                String request = Requester.readAllFromInputStream(acceptance.getInputStream());

                Log.d(TAG, request);
            }
        } catch (IOException e){
            e.printStackTrace();
        }

        try {
            if (listenerSocket != null && !listenerSocket.isClosed()) {
                listenerSocket.close();
                Log.d(TAG, "Closed ServerSocket");
            } else {
                Log.w(TAG, "ServerSocket was not open!");
            }
        } catch (IOException e) {
            Log.e(TAG, "Exception closing ServerSocket", e);
        }

    }

}

package com.zeromaid.gme_android;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.widget.Toast;

public class VGMPlayerService extends Service {
    private VGMPlayer c_objPlayer = new VGMPlayer();
    private static final int NOTIFY_ID = R.layout.main;

    private final IVGMPlayerService.Stub c_objBinder = new IVGMPlayerService.Stub() {
	public void loadData( byte[] a_bytDataIn, String strNameIn )
		    throws RemoteException {
	    // TODO Auto-generated method stub

	    Notification objNotification = new Notification( R.drawable.icon,
			strNameIn, System.currentTimeMillis() );

	    /*
	     * ((NotificationManager)getSystemService(
	     * Context.NOTIFICATION_SERVICE )) .notify( NOTIFY_ID,
	     * objNotification );
	     */

	    c_objPlayer.loadData( a_bytDataIn, strNameIn );
	    c_objPlayer.startTrack( 1, 200 );

	    /*
	     * Toast.makeText( null, "Playing " + strPathIn + "...",
	     * Toast.LENGTH_LONG ).show();
	     */
	}

	public boolean isPlaying() throws RemoteException {
	    // TODO Auto-generated method stub
	    return false;
	}

	public void next() throws RemoteException {
	    // TODO Auto-generated method stub
	}

	public void pause() throws RemoteException {
	    // TODO Auto-generated method stub
	}

	public void prev() throws RemoteException {
	    // TODO Auto-generated method stub
	}

	public void startTrack( int intTrackIn ) throws RemoteException {
	    // TODO Auto-generated method stub
	}

	public void stop() throws RemoteException {
	    // TODO Auto-generated method stub
	}
    };

    @Override
    public IBinder onBind( Intent iteIntendIn ) {
	return c_objBinder;
    }

    @Override
    public void onCreate() {
	// TODO Auto-generated method stub
	super.onCreate();

	Toast.makeText( this, "Service created ...", Toast.LENGTH_LONG ).show();
    }

    @Override
    public void onDestroy() {
	super.onDestroy();
	Toast.makeText( this, "Service destroyed ...", Toast.LENGTH_LONG )
		    .show();
    }

}

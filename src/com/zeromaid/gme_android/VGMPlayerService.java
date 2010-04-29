package com.zeromaid.gme_android;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

public class VGMPlayerService extends Service {
    private VGMPlayer c_objPlayer = new VGMPlayer();
    private static final int NOTIFY_ID = R.layout.main;
    private static final int DEFAULT_PLAY_LEN = 200;

    private final IVGMPlayerService.Stub c_objBinder = new IVGMPlayerService.Stub() {
	/**
	 * 
	 * @param a_bytDataIn
	 *            The binary data for the file to play. Set to null to play
	 *            the currently loaded file.
	 */
	public void start( byte[] a_bytDataIn, String strPathIn, int intTrackIn )
		    throws RemoteException {
	    startMusic( a_bytDataIn, strPathIn, intTrackIn );
	}

	public boolean isPlaying() throws RemoteException {
	    return c_objPlayer.isPlaying();
	}

	public void next() throws RemoteException {
	    int intTrackNext = c_objPlayer.getCurrentTrack() + 1;
	    if( c_objPlayer.getTrackCount() <= intTrackNext ) {
		intTrackNext = 0;
	    }
	    this.start( null, null, intTrackNext );
	}

	public void pause() throws RemoteException {
	    c_objPlayer.pause();
	}

	public void prev() throws RemoteException {
	    int intTrackPrev = c_objPlayer.getCurrentTrack() - 1;
	    if( 0 >= intTrackPrev ) {
		intTrackPrev = 0;
	    }
	    this.start( null, null, intTrackPrev );
	}

	public void stop() throws RemoteException {
	    stopMusic();
	}

	public String getTitle() throws RemoteException {
	    return c_strLastPath
			.substring( c_strLastPath.lastIndexOf( '/' ) + 1 );
	}

	public int getTrack() throws RemoteException {
	    return c_objPlayer.getCurrentTrack();
	}

	public int getPlayTime() throws RemoteException {
	    return c_objPlayer.getCurrentTime();
	}
    };

    private NotificationManager c_objNotificationManager;
    private String c_strLastPath;

    @Override
    public IBinder onBind( Intent iteIntendIn ) {
	return c_objBinder;
    }

    @Override
    public void onCreate() {
	super.onCreate();
	c_objNotificationManager = (NotificationManager)getSystemService( Context.NOTIFICATION_SERVICE );
    }

    @Override
    public void onDestroy() {
	super.onDestroy();
    }

    public void startMusic( byte[] a_bytDataIn, String strPathIn, int intTrackIn ) {
	// Find the last played track if there is one.
	int intTrackResume;
	try {
	    intTrackResume = c_objPlayer.getCurrentTrack();
	} catch( Exception ex ) {
	    // There was no previous track!
	    intTrackResume = 0;
	}
	if( null != a_bytDataIn ) {
	    // If data was provided then load it.
	    c_objPlayer.loadData( a_bytDataIn, strPathIn );
	}
	if( 0 > intTrackIn ) {
	    // If the track specified is -1 then try to play the current
	    // track but start at the first track if there is no current
	    // track.
	    intTrackIn = intTrackResume;
	}

	// Display the notification icon in the tray.
	if( null != strPathIn ) {
	    c_strLastPath = strPathIn;
	}
	String strFilename = c_strLastPath.substring( c_strLastPath
		    .lastIndexOf( '/' ) + 1 );
	Notification objNotification = new Notification( R.drawable.icon,
		    strPathIn, System.currentTimeMillis() );
	Intent itePlayMusic = new Intent( this, PlayMusic.class );
	PendingIntent pitPlayMusic = PendingIntent.getActivity( this, 0,
		    itePlayMusic, PendingIntent.FLAG_CANCEL_CURRENT );
	objNotification.setLatestEventInfo( this, this
		    .getString( R.string.app_name ), "Now playing "
		    + strFilename + "...", pitPlayMusic );
	objNotification.flags |= Notification.FLAG_ONGOING_EVENT;
	c_objNotificationManager.notify( NOTIFY_ID, objNotification );

	// Start playing.
	c_objPlayer.stop();
	c_objPlayer.startTrack( intTrackIn, DEFAULT_PLAY_LEN );
    }

    public void stopMusic() {
	c_objNotificationManager.cancel( NOTIFY_ID );
	c_objPlayer.stop();
    }
}

package com.zeromaid.gme_android;

import java.io.InputStream;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

public class VGMPlayerService extends Service {
    private VGMPlayer c_objPlayer = new VGMPlayer();
    private static final int NOTIFY_ID = R.layout.main;
    private static final String LOG_TAG = "VGMPlayerService";
    private static final int DEFAULT_PLAY_LEN = 200;

    private NotificationManager c_objNotificationManager;
    private HeadphonePlugReceiver c_objHeadphoneReceiver;

    private final IVGMPlayerService.Stub c_objBinder = new IVGMPlayerService.Stub() {
	public boolean isPlaying() throws RemoteException {
	    return c_objPlayer.isPlaying();
	}

	public void next() throws RemoteException {
	    int intTrackNext = c_objPlayer.getCurrentTrack() + 1;
	    if( c_objPlayer.getTrackCount() <= intTrackNext ) {
		intTrackNext = 0;
	    }
	    setTrack( intTrackNext );
	}

	public void pause() throws RemoteException {
	    c_objPlayer.pause();
	}

	public void prev() throws RemoteException {
	    int intTrackPrev = c_objPlayer.getCurrentTrack() - 1;
	    if( 0 >= intTrackPrev ) {
		intTrackPrev = c_objPlayer.getTrackCount() - 1;
	    }
	    setTrack( intTrackPrev );
	}

	public void stop() throws RemoteException {
	    stopMusic();
	}

	public String getTitle() throws RemoteException {
	    if( null != c_objPlayer ) {
		return c_objPlayer.getCurrentName();
	    } else {
		return null;
	    }
	}

	public int getTrack() throws RemoteException {
	    return c_objPlayer.getCurrentTrack();
	}

	public int getPlayTime() throws RemoteException {
	    return c_objPlayer.getCurrentTime();
	}

	public boolean load( String strPathIn ) throws RemoteException {
	    return loadMusic( strPathIn );
	}

	public void play() throws RemoteException {
	    startMusic();
	}
    };

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

    public boolean loadMusic( String strPathIn ) {
	try {
	    InputStream stmInput = this.getContentResolver().openInputStream(
			Uri.parse( strPathIn ) );
	    byte[] a_bytData = DataReader.loadData( stmInput );

	    c_objPlayer.loadData( a_bytData, strPathIn );
	} catch( Exception ex ) {
	    Log.e( LOG_TAG, "Unable to load selected music file." );
	    return false;
	}

	return true;
    }

    public void setTrack( int intTrackIn ) {
	// Don't bother if we're being asked to play the same track we're
	// already playing.
	if( c_objPlayer.isPlaying()
		    && c_objPlayer.getCurrentTrack() == intTrackIn ) {
	    return;
	}

	// boolean bolWasPlaying = c_objPlayer.isPlaying();
	c_objPlayer.stop();
	c_objPlayer.setCurrentTrack( intTrackIn );

	// Play if we were playing before.
	/*
	 * if( bolWasPlaying ) { c_objPlayer.play(); }
	 */
    }

    public void startMusic() {
	// Display the notification icon in the tray.
	String strFilename = c_objPlayer.getCurrentName();
	String strNowPlaying = String.format( "%s: %s...", this
		    .getString( R.string.now_playing ), strFilename );
	Notification objNotification = new Notification( R.drawable.icon,
		    c_objPlayer.getCurrentName(), System.currentTimeMillis() );
	Intent itePlayMusic = new Intent( this, PlayMusic.class );
	PendingIntent pitPlayMusic = PendingIntent.getActivity( this, 0,
		    itePlayMusic, PendingIntent.FLAG_CANCEL_CURRENT );
	objNotification.setLatestEventInfo( this, this
		    .getString( R.string.app_name ), strNowPlaying,
		    pitPlayMusic );
	objNotification.flags |= Notification.FLAG_ONGOING_EVENT;
	c_objNotificationManager.notify( NOTIFY_ID, objNotification );

	// Register a receiver to listen for the headphones being unplugged.
	c_objHeadphoneReceiver = new HeadphonePlugReceiver();
	IntentFilter objFilter = new IntentFilter( Intent.ACTION_HEADSET_PLUG );
	registerReceiver( c_objHeadphoneReceiver, objFilter );

	// Start playing.
	c_objPlayer.stop();
	c_objPlayer
		    .startTrack( c_objPlayer.getCurrentTrack(),
				DEFAULT_PLAY_LEN );
    }

    public void stopMusic() {
	try {
	    c_objNotificationManager.cancel( NOTIFY_ID );
	    unregisterReceiver( c_objHeadphoneReceiver );
	} catch( IllegalArgumentException ex ) {
	    // We tried to cancel or unregister something that didn't exist,
	    // probably.
	    Log.e( LOG_TAG, "Attempted to cancel non-existent handler." );
	}
	c_objPlayer.stop();
    }
}

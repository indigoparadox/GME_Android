package com.zeromaid.gme_android;

import java.io.InputStream;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.os.RemoteException;

public class VGMPlayerService extends Service {
    private VGMPlayer c_objPlayer = new VGMPlayer();
    private static final int NOTIFY_ID = R.layout.main;
    private static final int DEFAULT_PLAY_LEN = 200;

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
		intTrackPrev = 0;
	    }
	    setTrack( intTrackPrev );
	}

	public void stop() throws RemoteException {
	    stopMusic();
	}

	public String getTitle() throws RemoteException {
	    return c_objPlayer.getCurrentName();
	}

	public int getTrack() throws RemoteException {
	    return c_objPlayer.getCurrentTrack();
	}

	public int getPlayTime() throws RemoteException {
	    return c_objPlayer.getCurrentTime();
	}

	public void load( String strPathIn ) throws RemoteException {
	    loadMusic( strPathIn );
	}

	public void play() throws RemoteException {
	    startMusic();
	}
    };

    private NotificationManager c_objNotificationManager;

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

    public void loadMusic( String strPathIn ) {
	try {
	    InputStream stmInput = this.getContentResolver().openInputStream(
			Uri.parse( strPathIn ) );
	    byte[] a_bytData = DataReader.loadData( stmInput );

	    c_objPlayer.loadData( a_bytData, strPathIn );
	} catch( Exception ex ) {
	    // TODO: What should we do here?
	}
    }

    public void setTrack( int intTrackIn ) {
	// Don't bother if we're being asked to play the same track we're
	// already playing.
	if( c_objPlayer.isPlaying()
		    && c_objPlayer.getCurrentTrack() == intTrackIn ) {
	    return;
	}

	boolean bolWasPlaying = c_objPlayer.isPlaying();
	c_objPlayer.stop();
	c_objPlayer.startTrack( intTrackIn, DEFAULT_PLAY_LEN );

	// Pause if we weren't playing before.
	if( !bolWasPlaying ) {
	    c_objPlayer.pause();
	}
    }

    public void startMusic() {
	// Display the notification icon in the tray.
	String strFilename = c_objPlayer.getCurrentName();
	Notification objNotification = new Notification( R.drawable.icon,
		    c_objPlayer.getCurrentName(), System.currentTimeMillis() );
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
	c_objPlayer
		    .startTrack( c_objPlayer.getCurrentTrack(),
				DEFAULT_PLAY_LEN );
    }

    public void stopMusic() {
	c_objNotificationManager.cancel( NOTIFY_ID );
	c_objPlayer.stop();
    }
}

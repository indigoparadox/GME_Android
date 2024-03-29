package com.zeromaid.gme_android;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class PlayMusic extends Activity implements OnClickListener {
    private final static String LOG_TAG = "PlayMusic";
    private IVGMPlayerService c_objPlayerI = null;

    private ServiceConnection c_conService = new ServiceConnection() {
	public void onServiceDisconnected( ComponentName className ) {
	    c_objPlayerI = null;
	}

	public void onServiceConnected( ComponentName name, IBinder service ) {
	    // TODO: Update song display.
	    c_objPlayerI = IVGMPlayerService.Stub
			.asInterface( (IBinder)service );

	    // Figure out if music was loaded from a load box.
	    String strMusicPath;
	    try {
		strMusicPath = (String)getIntent().getExtras()
			    .get( "musicPath" );
	    } catch( Exception ex ) {
		// No music file was specified, probably.
		strMusicPath = null;
	    }

	    try {
		if( null != strMusicPath ) {
		    // Figure out of there's a music player service
		    // and it's playing.
		    if( null != c_objPlayerI && c_objPlayerI.isPlaying() ) {
			c_objPlayerI.stop();
		    }

		    // Start a new music player with the selected music.
		    c_objPlayerI.load( strMusicPath );
		}
	    } catch( RemoteException ex ) {
		Log.e( LOG_TAG, ex.getMessage() );
	    }

	    try {
		((TextView)findViewById( R.id.txtPlayFileName ))
			    .setText( c_objPlayerI.getTitle() );
		setTrackDisplay( c_objPlayerI.getTrack() );
	    } catch( NullPointerException ex ) {
		// Nothing to do here.
	    } catch( RemoteException ex ) {
		// Nothing to do here.
	    }
	}
    };

    public void onClick( View v ) {
	try {
	    switch( v.getId() ) {
		case R.id.btnLoad:
		    // Switch to the LoadMusic activity.
		    Intent iteLoad = new Intent( this, LoadMusic.class );
		    startActivity( iteLoad );
		    break;

		case R.id.btnPlay:
		    if( null != c_objPlayerI && null != c_objPlayerI.getTitle() ) {
			// Send the play signal to the player service.
			Intent iteStartService = new Intent( this,
				    VGMPlayerService.class );
			this.startService( iteStartService );
			c_objPlayerI.play();
		    }
		    break;

		case R.id.btnStop:
		    if( null != c_objPlayerI && null != c_objPlayerI.getTitle() ) {
			c_objPlayerI.stop();
			Intent iteStopService = new Intent( this,
				    VGMPlayerService.class );
			this.stopService( iteStopService );
		    }
		    break;

		case R.id.btnNext:
		    if( null != c_objPlayerI && null != c_objPlayerI.getTitle() ) {
			c_objPlayerI.next();
			this.setTrackDisplay( c_objPlayerI.getTrack() );
		    }
		    break;

		case R.id.btnPrev:
		    if( null != c_objPlayerI && null != c_objPlayerI.getTitle() ) {
			c_objPlayerI.prev();
			this.setTrackDisplay( c_objPlayerI.getTrack() );
		    }
		    break;
	    }
	} catch( RemoteException ex ) {
	    Log.e( LOG_TAG, ex.getMessage() );
	}
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate( Bundle savedInstanceState ) {
	super.onCreate( savedInstanceState );
	setContentView( R.layout.main );

	// Setup the window event listeners.
	((Button)findViewById( R.id.btnLoad )).setOnClickListener( this );
	((Button)findViewById( R.id.btnPlay )).setOnClickListener( this );
	((Button)findViewById( R.id.btnStop )).setOnClickListener( this );
	((Button)findViewById( R.id.btnPrev )).setOnClickListener( this );
	((Button)findViewById( R.id.btnNext )).setOnClickListener( this );

	// Call the player service to play music.
	Intent iteService = new Intent( this, VGMPlayerService.class );
	this.bindService( iteService, c_conService, Context.BIND_AUTO_CREATE );

	((TextView)findViewById( R.id.txtPlayFileName )).setText( "" );
	((TextView)findViewById( R.id.txtPlayTrack ))
		    .setText( "Nothing Loaded" );
    }

    @Override
    protected void onPause() {
	super.onPause();
	try {
	    // If nothing is playing then stop the player service.
	    if( !c_objPlayerI.isPlaying() ) {
		Intent iteStopService = new Intent( this,
			    VGMPlayerService.class );
		this.stopService( iteStopService );
	    }
	} catch( RemoteException ex ) {
	    Log.e( LOG_TAG, ex.getMessage() );
	}
    }

    /**
     * Set the track number displayed in the main interface.
     * 
     * @param intTrackIn
     */
    protected void setTrackDisplay( int intTrackIn ) {
	// Apparently humans don't like zero-indexing.
	intTrackIn++;

	try {
	    ((TextView)findViewById( R.id.txtPlayTrack )).setText( "Track "
			+ intTrackIn );
	} catch( Exception ex ) {
	    ((TextView)findViewById( R.id.txtPlayTrack )).setText( "Track 0" );
	}
    }
}
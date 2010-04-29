package com.zeromaid.gme_android;

import java.io.InputStream;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class PlayMusic extends Activity implements OnClickListener {
    private final static String PATH_CHIPTUNES = "/sdcard/Chiptunes";
    private IVGMPlayerService c_objPlayerI = null;

    private String c_strMusicPath;

    private ServiceConnection c_conService = new ServiceConnection() {
	public void onServiceDisconnected( ComponentName className ) {
	    c_objPlayerI = null;
	}

	public void onServiceConnected( ComponentName name, IBinder service ) {
	    // TODO Auto-generated method stub
	    // TODO: Update song display.
	    c_objPlayerI = IVGMPlayerService.Stub
			.asInterface( (IBinder)service );
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
		    // TODO: Send the play signal to the player service.
		    Intent iteStartService = new Intent( this,
				VGMPlayerService.class );
		    this.startService( iteStartService );
		    this.loadCurrentFile();
		    break;

		case R.id.btnStop:
		    c_objPlayerI.stop();
		    Intent iteStopService = new Intent( this,
				VGMPlayerService.class );
		    this.stopService( iteStopService );
		    break;

		case R.id.btnNext:
		    c_objPlayerI.next();
		    break;

		case R.id.btnPrev:
		    c_objPlayerI.prev();
		    break;
	    }
	} catch( RemoteException ex ) {
	    ex.printStackTrace();
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

	// Figure out if music was loaded from a load box.
	try {
	    c_strMusicPath = (String)this.getIntent().getExtras().get(
			"musicPath" );
	} catch( Exception ex ) {
	    // No music file was specified, probably.
	    c_strMusicPath = null;
	}

	// TODO: Call the player service to play this music.
	this.bindService( new Intent( this, VGMPlayerService.class ),
		    c_conService, Context.BIND_AUTO_CREATE );

	if( null != c_strMusicPath ) {
	    try {
		// TODO: Figure out of there's a music player service and it's
		// playing.

		if( null != c_objPlayerI && c_objPlayerI.isPlaying() ) {
		    Toast.makeText( this, "KILL", 200 );
		    c_objPlayerI.stop();
		}
	    } catch( RemoteException ex ) {
		// TODO: Handle this?
	    }

	    // TODO: Start a new music player with the selected music.

	} else {
	    // TODO: If the music player service is already playing something,
	    // populate the status and controls with that.

	}
    }

    private boolean loadCurrentFile() {
	try {
	    ((TextView)findViewById( R.id.txtNowPlaying ))
			.setText( c_strMusicPath );

	    InputStream stmInput = this.getContentResolver().openInputStream(
			Uri.parse( c_strMusicPath ) );
	    byte[] a_bytData = DataReader.loadData( stmInput );

	    c_objPlayerI.start( a_bytData, c_strMusicPath, -1 );
	} catch( Exception ex ) {
	    // this.getString( R.string.error_play )
	    Toast.makeText( this, ex.getMessage(), Toast.LENGTH_LONG ).show();
	    return false;
	}

	return true;
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

	}
    }
}
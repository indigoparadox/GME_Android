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
    private IVGMPlayerService c_objPlayerI;

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

		    if( this.loadCurrentFile() ) {
			c_objPlayerI.startTrack( 1 );
		    }
		    break;

	    }
	} catch( RemoteException e ) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
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

	// Connect to the music player service.
	this.bindService( new Intent( this, VGMPlayerService.class ),
		    c_conService, Context.BIND_AUTO_CREATE );

	// Figure out if there's music to load.
	try {
	    c_strMusicPath = (String)this.getIntent().getExtras().get(
			"musicPath" );
	} catch( Exception ex ) {
	    // No music file was specified, probably.
	    c_strMusicPath = null;
	}
    }

    private boolean loadCurrentFile() {
	String strName = c_strMusicPath.toUpperCase();

	try {
	    ((TextView)findViewById( R.id.txtNowPlaying ))
			.setText( c_strMusicPath );

	    InputStream stmInput = this.getContentResolver().openInputStream(
			Uri.parse( c_strMusicPath ) );
	    byte[] a_bytData = DataReader.loadData( stmInput );

	    // TODO: Call the player service to play this music.
	    c_objPlayerI.loadData( a_bytData, strName );
	} catch( Exception ex ) {
	    Toast.makeText( this, this.getString( R.string.error_play ),
			Toast.LENGTH_LONG ).show();
	    return false;
	}

	return true;
    }
}
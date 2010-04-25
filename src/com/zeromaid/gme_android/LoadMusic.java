package com.zeromaid.gme_android;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class LoadMusic extends ListActivity {
    private List<String> directoryEntries = new ArrayList<String>();
    private File currentDirectory = new File( "/" );

    /** Called when the activity is first created. */
    @Override
    public void onCreate( Bundle icicle ) {
	super.onCreate( icicle );
	// setContentView() gets called within the next line,
	// so we do not need it here.
	browseToRoot();
    }

    /**
     * This function browses to the root-directory of the file-system.
     */
    private void browseToRoot() {
	File filChiptunes = new File( "/sdcard/Chiptunes" );

	// Create the chiptunes directory if it doesn't exist.
	if( !filChiptunes.exists() ) {
	    filChiptunes.mkdirs();
	}

	this.browseTo( filChiptunes );
    }

    /**
     * This function browses up one level according to the field:
     * currentDirectory
     */
    private void upOneLevel() {
	if( this.currentDirectory.getParent() != null )
	    this.browseTo( this.currentDirectory.getParentFile() );
    }

    private void browseTo( final File aDirectory ) {
	if( aDirectory.isDirectory() ) {
	    this.currentDirectory = aDirectory;
	    fill( aDirectory.listFiles() );
	} else {
	    // Lets start an intent to View the file, that was
	    // clicked...
	    Intent itePlay = new Intent( this, PlayMusic.class );
	    itePlay.putExtra( "musicPath", "file://"
			+ aDirectory.getAbsolutePath() );
	    startActivity( itePlay );
	}
    }

    private void fill( File[] files ) {
	this.directoryEntries.clear();

	// Add the "." and the ".." == 'Up one level'
	try {
	    Thread.sleep( 10 );
	} catch( InterruptedException e1 ) {
	    // TODO Auto-generated catch block
	    e1.printStackTrace();
	}
	this.directoryEntries.add( "." );

	if( this.currentDirectory.getParent() != null )
	    this.directoryEntries.add( ".." );

	// Concatenate the current working path to the beginning of the return
	// string.
	int currentPathStringLenght = this.currentDirectory.getAbsolutePath()
		    .length();
	for( File file : files ) {
	    this.directoryEntries.add( file.getAbsolutePath().substring(
			currentPathStringLenght ) );
	}

	ArrayAdapter<String> directoryList = new ArrayAdapter<String>( this,
		    R.layout.browse, this.directoryEntries );

	this.setListAdapter( directoryList );
    }

    @Override
    protected void onListItemClick( ListView l, View v, int position, long id ) {
	String selectedFileString = this.directoryEntries.get( position );
	if( selectedFileString.equals( "." ) ) {
	    // Refresh
	    this.browseTo( this.currentDirectory );
	} else if( selectedFileString.equals( ".." ) ) {
	    this.upOneLevel();
	} else {
	    File clickedFile = null;
	    clickedFile = new File( this.currentDirectory.getAbsolutePath()
			+ this.directoryEntries.get( position ) );
	    if( clickedFile != null )
		this.browseTo( clickedFile );
	}
    }
}
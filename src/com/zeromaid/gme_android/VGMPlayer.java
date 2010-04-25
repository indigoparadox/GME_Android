package com.zeromaid.gme_android;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

public class VGMPlayer implements Runnable {
    private final static String LOG_TAG = "GME for Android";

    private Thread c_thdPlayer;
    private AudioTrack c_trkLine;
    private int c_intSampleRate;
    private MusicEmu c_objEmu;
    private boolean c_bolPlaying;
    private double c_dblVolume = 1.0;

    public void run() {
	// Start listening on the output line.
	c_trkLine.play();

	// Play the track until a stop signal.
	byte[] buf = new byte[8192];
	while( c_bolPlaying && !c_objEmu.trackEnded() ) {
	    int count = c_objEmu.play( buf, buf.length / 2 );
	    c_trkLine.write( buf, 0, count * 2 );

	    // TODO: Should we pause or something here?
	}

	// We ran out of data so stop naturally.
	c_bolPlaying = false;
	c_trkLine.stop();
    }

    /**
     * Sets music emulator to get samples from
     * 
     * @param emu
     * @param sampleRate
     * @throws Exception
     */
    public void setEmu( MusicEmu objEmuIn, int intSampleRateIn ) {
	stop();
	c_objEmu = objEmuIn;
	if( c_objEmu != null && c_trkLine == null
		    && c_intSampleRate != intSampleRateIn ) {
	    // Just set the sample rate and get ready to play.
	    c_intSampleRate = intSampleRateIn;
	}
    }

    /**
     * @param v
     *            The playback volume, where 1.0 is normal, 2.0 is twice as
     *            loud. Can be changed while track is playing.
     */
    public void setVolume( double v ) {
	c_dblVolume = v;

	if( c_trkLine != null ) {
	    c_trkLine.setStereoVolume( (float)c_dblVolume, (float)c_dblVolume );
	}
    }

    /**
     * @return The number of tracks in the currently loaded file.
     */
    public int getTrackCount() {
	return c_objEmu.trackCount();
    }

    /**
     * @return The currently playing track.
     */
    public int getCurrentTrack() {
	return c_objEmu.currentTrack();
    }

    /**
     * @return The number of seconds played since last startTrack() call.
     */
    public int getCurrentTime() {
	return(c_objEmu == null ? 0 : c_objEmu.currentTime());
    }

    /**
     * @return Current playback volume.
     */
    public double getVolume() {
	return c_dblVolume;
    }

    /**
     * @return If the player is currently playing.
     */
    public boolean isPlaying() {
	return c_bolPlaying;
    }

    /**
     * Starts new track playing, where 0 is the first track. After time seconds,
     * the track starts fading.
     * 
     * @param intTrackIn
     * @param intTimeIn
     * @throws Exception
     */
    public void startTrack( int intTrackIn, int intTimeIn ) {
	pause();
	if( c_trkLine != null ) {
	    c_trkLine.flush();
	}
	c_objEmu.startTrack( intTrackIn );
	c_objEmu.setFade( intTimeIn, 6 );
	play();
    }

    /**
     * Resumes playback where it was paused.
     * 
     * @throws Exception
     */
    public void play() {
	if( c_trkLine == null ) {
	    c_trkLine = new AudioTrack( AudioManager.STREAM_MUSIC,
			c_intSampleRate,
			AudioFormat.CHANNEL_CONFIGURATION_MONO,
			AudioFormat.ENCODING_PCM_16BIT,
			AudioTrack.getMinBufferSize( c_intSampleRate,
				    AudioFormat.CHANNEL_CONFIGURATION_STEREO,
				    AudioFormat.ENCODING_PCM_16BIT ),
			AudioTrack.MODE_STREAM );

	    this.setVolume( c_dblVolume );
	}
	c_thdPlayer = new Thread( this );
	c_bolPlaying = true;
	c_thdPlayer.start();
    }

    /**
     * Pauses if a track was playing.
     * 
     * @throws Exception
     */
    public void pause() {
	if( c_thdPlayer != null ) {
	    c_bolPlaying = false;
	    try {
		c_thdPlayer.join();
	    } catch( InterruptedException ex ) {
		// TODO Auto-generated catch block
		Log.e( LOG_TAG, ex.getMessage() );
	    }
	    c_thdPlayer = null;
	}
    }

    /**
     * Stops playback and closes audio.
     * 
     * @throws Exception
     */
    public void stop() {
	pause();

	if( c_trkLine != null ) {
	    // Stop the AudioTrack from playing.
	    c_trkLine.stop();
	    c_trkLine = null;
	}
    }

    /**
     * Creates appropriate emulator for given filename.
     * 
     * @param strNameIn
     *            The name of the file to create the emulator for.
     * @return The created emulator.
     */
    private MusicEmu createEmu( String strNameIn ) {
	if( strNameIn.endsWith( ".VGM" ) || strNameIn.endsWith( ".VGZ" ) )
	    return new VgmEmu();

	if( strNameIn.endsWith( ".GBS" ) )
	    return new GbsEmu();

	if( strNameIn.endsWith( ".NSF" ) )
	    return new NsfEmu();

	if( strNameIn.endsWith( ".SPC" ) )
	    return new SpcEmu();

	return null;
    }

    public void loadData( byte[] a_bytDataIn, String strNameIn ) {
	MusicEmu objEmu = this.createEmu( strNameIn );
	if( objEmu == null )
	    return; // TODO: throw exception?
	int actualSampleRate = objEmu.setSampleRate( 44100 );
	objEmu.loadFile( a_bytDataIn );

	// now that new emulator is ready, replace old one
	// TODO: Find a way to do this that's more Android-like.
	this.setEmu( objEmu, actualSampleRate );
    }
}

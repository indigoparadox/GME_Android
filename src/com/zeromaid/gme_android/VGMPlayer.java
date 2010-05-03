package com.zeromaid.gme_android;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

public class VGMPlayer implements Runnable {
    private final static String LOG_TAG = "GME for Android";
    private final static int LEN_PCM_BUFFER = 8192;
    private final static int LEN_PCM_SAMPLE_BYTES = 2;

    private Thread c_thdPlayer;
    private AudioTrack c_trkLine;
    private int c_intSampleRate;
    private MusicEmu c_objEmu;
    private boolean c_bolPlaying;
    private double c_dblVolume = 1.0;
    private String c_strCurrentName = null;

    public void run() {
	// Start listening on the output line.
	c_trkLine.play();

	// Play the track until a stop signal.
	byte[] a_bytBuffer = new byte[LEN_PCM_BUFFER];
	while( c_bolPlaying && !c_objEmu.trackEnded() ) {
	    int intCount = c_objEmu.play( a_bytBuffer, a_bytBuffer.length / 2 );

	    c_trkLine.write( a_bytBuffer, 0, intCount * 2 );

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

    public String getCurrentName() {
	return c_strCurrentName;
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
			AudioFormat.CHANNEL_CONFIGURATION_STEREO,
			AudioFormat.ENCODING_PCM_16BIT,
			AudioTrack.getMinBufferSize( c_intSampleRate,
				    AudioFormat.CHANNEL_CONFIGURATION_STEREO,
				    AudioFormat.ENCODING_PCM_16BIT ),
			AudioTrack.MODE_STREAM );

	    this.setVolume( c_dblVolume );
	}
	if( null == c_thdPlayer ) {
	    c_thdPlayer = new Thread( this );
	}
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
		Log.e( LOG_TAG, ex.getMessage() );
	    }
	}
    }

    /**
     * Stops playback and closes audio.
     * 
     * @throws Exception
     */
    public void stop() {
	pause();
	c_thdPlayer = null;
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

    public void loadData( byte[] a_bytDataIn, String strPathIn ) {
	c_strCurrentName = strPathIn
		    .substring( strPathIn.lastIndexOf( '/' ) + 1 );
	MusicEmu objEmu = this.createEmu( strPathIn.toUpperCase() );
	if( objEmu == null ) {
	    // TODO: Throw exception?
	    return;
	}
	int actualSampleRate = objEmu.setSampleRate( 44100 );
	objEmu.loadFile( a_bytDataIn );

	// Now that new emulator is ready, replace the old one.
	this.setEmu( objEmu, actualSampleRate );
    }
}

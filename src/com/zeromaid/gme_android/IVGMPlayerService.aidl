package com.zeromaid.gme_android;

interface IVGMPlayerService {
	void start( in byte[] a_bytDataIn, String strPathIn, int intTrackIn );
	void pause();
	void stop();
	void prev();
	void next();
	boolean isPlaying();
	String getTitle();
	int getTrack();
	int getPlayTime();
}
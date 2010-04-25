package com.zeromaid.gme_android;

interface IVGMPlayerService {
	void loadData( in byte[] a_bytDataIn, String strNameIn );
	void startTrack( in int intTrackIn );
	void pause();
	void stop();
	void prev();
	void next();
	boolean isPlaying();
}
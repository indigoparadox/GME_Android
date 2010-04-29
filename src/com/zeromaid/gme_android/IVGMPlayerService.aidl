package com.zeromaid.gme_android;

interface IVGMPlayerService {
	void load( String strPathIn );
	void play();
	void pause();
	void stop();
	void prev();
	void next();
	boolean isPlaying();
	String getTitle();
	int getTrack();
	int getPlayTime();
}
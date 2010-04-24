Game_Music_Emu Java-based player
--------------------------------
This is a port of Game_Music_Emu to Java, with a simple GUI front-end.

Please contact me if you want to help improve the GUI. I don't code in
Java regularly and lack a decent machine to develop with.


Using player
------------
Click a track to add it to the playlist. If the Playlist checkbox is not
checked, adding a track will start it playing immediately. If Playlist
is checked, adding a track won't interrupt the current one. Use Prev and
Next to go through the playlist manually. Click Stop to pause, click
again to unpause.


File types supported
--------------------
.vgm    Sega Master System/Sega Genesis
.vgz    Gzipped .vgm
.nsf    Nintendo NES, APU only
.gbs    Game Boy
.spc    Super NES
.gz     Gzipped file
.zip    Archive of one or more game music files


Putting player on a page
------------------------
To use the player, put gme.jar in a suitable place on your site, and
insert the following where you want the Java applet to appear:

	<APPLET archive="gme.jar" code="gme" name="gme" width=400 height=100>
	</APPLET>

The GUI elements should auto-position, so you can make it wide if you
don't want two lines of GUI elements, for example.


Player configuration
--------------------
Between the APPLET and /APPLET tags, you can specify parameters. All are
optional.

	<PARAM NAME="PLAYURL" VALUE="http://...">

If present, this track is loaded and starts playing immediately.

	<PARAM NAME="PLAYPATH" VALUE="dir_inside_zip/file.nsf">

If PLAYURL specifies a zip file, this specifies the path inside the zip.

	<PARAM NAME="PLAYTRACK" VALUE="7">

Which track of the above file to play. Defaults to 1.

	<PARAM NAME="SAMPLERATE" VALUE="48000">

Sample rate to play at. Default is 44100. Reasonable values include
48000, 22050, and perhaps 11025.

	<PARAM NAME="BACKGROUND" VALUE="1">

Specifies whether to play music when applet is in background (switched
to another tab/window, etc.). Defaults to 0 (pause in background).

	<PARAM NAME="NOGUI" VALUE="1">

Specifies whether GUI should NOT be displayed. Defaults to 0, so that
GUI is shown.



Starting tracks via JavaScript
------------------------------
The player allows player control via JavaScript on the page. This allows
you to show a list of tracks that are clickable, for example. To provide
a URL that starts track 4 of a music file within a zip archive when
clicked, for example, do the following:

	<a
href="javascript:gme.playFile('http://example.com/somegame.zip','dir_in_archive/title.nsf',4)">Some
Game - Title</a>

These functions are available:

	playFile( String url, String path, int track )
	playFile( String url, String path, int track, String title )
	playFile( String url, String path, int track, String title, int time )

Adds track to playlist.

url  : URL of music file or archive.
path : path of file within archive if URL points to archive'
track: which track
title: name to appear in player GUI; defaults to filename
time : number of seconds to play track before fading; defaults to 180

	stopFile()

Same as clicking Stop in GUI.


-- 
Shay Green <gblargg@gmail.com>

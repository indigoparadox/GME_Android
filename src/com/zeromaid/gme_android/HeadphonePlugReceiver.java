package com.zeromaid.gme_android;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

public class HeadphonePlugReceiver extends BroadcastReceiver {
    private IVGMPlayerService c_objPlayerI = null;
    
    private ServiceConnection c_conService = new ServiceConnection() {
	public void onServiceDisconnected( ComponentName className ) {
	    c_objPlayerI = null;
	}

	public void onServiceConnected( ComponentName name, IBinder service ) {
	    // TODO: Update song display.
	    c_objPlayerI = IVGMPlayerService.Stub
			.asInterface( (IBinder)service );
	}
    };

    @Override
    public void onReceive( Context ctxInput, Intent iteInput ) {
	try {
	    // Try connecting to the player service.
	    Intent iteService = new Intent( ctxInput, VGMPlayerService.class );
	    ctxInput.bindService( iteService, c_conService,
			Context.BIND_AUTO_CREATE );

	    // If the player service is playing and the headset was unplugged,
	    // stop it.
	    if( c_objPlayerI.isPlaying()
			&& iteInput.getIntExtra( "state", 0 ) == 0 ) {
		c_objPlayerI.stop();
	    }
	} catch( Exception ex ) {
	    // TODO Auto-generated catch block
	    ex.printStackTrace();
	}

    }

}

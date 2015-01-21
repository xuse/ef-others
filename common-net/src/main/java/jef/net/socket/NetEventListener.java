package jef.net.socket;

import java.util.EventListener;

import jef.tools.security.IChannel;

public interface NetEventListener<T> extends EventListener{
	void onConnectionClose(T source);

	void messageSent(T source, byte[] message);

	void channelEstablished(T abstractSocketThread, IChannel channel);
}

package com.xmpp.chat.service;

import org.jivesoftware.smack.packet.Packet;

public abstract interface LivePacketListener {
	public abstract void OnPacketReceived(Packet paramPacket);
}

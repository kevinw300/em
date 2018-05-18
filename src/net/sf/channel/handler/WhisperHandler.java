/*
	This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc> 
                       Matthias Butz <matze@odinms.de>
                       Jan Christian Meyer <vimes@odinms.de>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License version 3
    as published by the Free Software Foundation. You may not use, modify
    or distribute this program under any other version of the
    GNU Affero General Public License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

/*
 * To change this template, choose Tools | Templates and open the template in the editor.
 */

package net.sf.channel.handler;

import java.rmi.RemoteException;
import client.MapleCharacter;
import client.MapleClient;
import client.messages.CommandProcessor;
import net.sf.AbstractMaplePacketHandler;
import net.sf.channel.ChannelServer;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

/**
 * 
 * @author Matze
 */
public class WhisperHandler extends AbstractMaplePacketHandler {

	public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
		byte mode = slea.readByte();
		if (mode == 6) { // whisper
			// System.out.println("in whisper handler");
			String recipient = slea.readMapleAsciiString();
			String text = slea.readMapleAsciiString();
			
			if (!CommandProcessor.getInstance().processCommand(c, text)) {
				MapleCharacter player = c.getChannelServer().getPlayerStorage().getCharacterByName(recipient);
				if (player != null) {
					player.getClient().getSession().write(MaplePacketCreator.getWhisper(c.getPlayer().getName(), c.getChannel(), text));
					c.getSession().write(MaplePacketCreator.getWhisperReply(recipient, (byte) 1));
				} else { // not found
					try {
						if (ChannelServer.getInstance(c.getChannel()).getWorldInterface().isConnected(recipient)) {
							ChannelServer.getInstance(c.getChannel()).getWorldInterface().whisper(
								c.getPlayer().getName(), recipient, c.getChannel(), text);
							c.getSession().write(MaplePacketCreator.getWhisperReply(recipient, (byte) 1));
						} else {
							c.getSession().write(MaplePacketCreator.getWhisperReply(recipient, (byte) 0));
						}
					} catch (RemoteException e) {
						c.getSession().write(MaplePacketCreator.getWhisperReply(recipient, (byte) 0));
						c.getChannelServer().reconnectWorld();
					}
				}
			}
		} else if (mode == 5) { // - /find
			String recipient = slea.readMapleAsciiString();
			MapleCharacter player = c.getChannelServer().getPlayerStorage().getCharacterByName(recipient);
			if (player != null && (c.getPlayer().isGM() || !player.isHidden())) {
				c.getSession().write(MaplePacketCreator.getFindReplyWithMap(recipient, player.getMap().getId()));
			} else { // not found
				try {
					int channel = ChannelServer.getInstance(c.getChannel()).getWorldInterface().find(recipient);
					if (channel > -1) {
						c.getSession().write(MaplePacketCreator.getFindReply(recipient, channel));
					} else {
						c.getSession().write(MaplePacketCreator.getWhisperReply(recipient, (byte) 0));
					}
				} catch (RemoteException e) {
					c.getChannelServer().reconnectWorld();
				}
			}
		}
	}
}

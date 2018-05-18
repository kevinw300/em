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
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.sf.channel.handler;

import client.MapleClient;
import net.sf.AbstractMaplePacketHandler;
import server.maps.MapleDoor;
import server.maps.MapleMapObject;
import tools.data.input.SeekableLittleEndianAccessor;

/**
 *
 * @author Matze
 */
public class DoorHandler extends AbstractMaplePacketHandler {

	public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
		int oid = slea.readInt();
		@SuppressWarnings("unused")
		byte mode = slea.readByte(); // specifies if backwarp or not, but currently we do not care
		for (MapleMapObject obj : c.getPlayer().getMap().getMapObjects()) {
			if (obj instanceof MapleDoor) {
				MapleDoor door = (MapleDoor) obj;
				if (door.getOwner().getId() == oid) {
					door.warp(c.getPlayer());
					return;
				}
			}
		}
	}

}
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

package net.sf.channel.remote;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import client.BuddyList.BuddyAddResult;
import client.BuddyList.BuddyOperation;
import net.sf.MaplePacket;
import net.sf.world.MapleParty;
import net.sf.world.MaplePartyCharacter;
import net.sf.world.PartyOperation;
import net.sf.world.guild.MapleGuildSummary;
import net.sf.world.remote.WorldChannelCommonOperations;

/**
 *
 * @author Matze
 */
public interface ChannelWorldInterface extends Remote, WorldChannelCommonOperations {

	public void setChannelId(int id) throws RemoteException;
	public int getChannelId() throws RemoteException;
	public String getIP() throws RemoteException;

	public boolean isConnected(int characterId) throws RemoteException;
	public int getConnected() throws RemoteException;
	
	public int getLocation(String name) throws RemoteException;
	
	public void updateParty (MapleParty party, PartyOperation operation, MaplePartyCharacter target) throws RemoteException;
	public void partyChat(MapleParty party, String chattext, String namefrom) throws RemoteException;
	
	public boolean isAvailable() throws RemoteException;
	public BuddyAddResult requestBuddyAdd(String addName, int channelFrom, int cidFrom, String nameFrom) throws RemoteException;
	public void buddyChanged(int cid, int cidFrom, String name, int channel, BuddyOperation op) throws RemoteException;
	public int[] multiBuddyFind(int charIdFrom, int [] characterIds) throws RemoteException;

	public void sendPacket(List<Integer> targetIds, MaplePacket packet, int exception) throws RemoteException;
	public void setGuildAndRank(int cid, int guildid, int rank) throws RemoteException;
	public void setOfflineGuildStatus(int guildid, byte guildrank, int cid) throws RemoteException;
	public void setGuildAndRank(List<Integer> cids, int guildid, int rank, int exception) throws RemoteException;
	public void reloadGuildCharacters() throws RemoteException;
	public void changeEmblem(int gid, List<Integer> affectedPlayers, MapleGuildSummary mgs) throws RemoteException;
}

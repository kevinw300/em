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

package scripting.event;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.script.ScriptException;

import client.MapleCharacter;
import database.DatabaseConnection;
import net.sf.world.MapleParty;
import net.sf.world.MaplePartyCharacter;
import provider.MapleDataProviderFactory;
import server.TimerManager;
import server.life.MapleMonster;
import server.maps.MapleMap;
import server.maps.MapleMapFactory;

/**
 *
 * @author Matze
 */
public class EventInstanceManager {

	private List<MapleCharacter> chars = new LinkedList<MapleCharacter>();
	private List<MapleMonster> mobs = new LinkedList<MapleMonster>();
	private Map<MapleCharacter,Integer> killCount = new HashMap<MapleCharacter,Integer>();
	private EventManager em;
	private MapleMapFactory mapFactory;
	private String name;
	private Properties props = new Properties();
	
	public EventInstanceManager(EventManager em, String name) {
		this.em = em;
		this.name = name;
		mapFactory = new MapleMapFactory(MapleDataProviderFactory.getDataProvider(new File(System.getProperty("net.sf.odinms.wzpath") + "/Map.wz")), MapleDataProviderFactory.getDataProvider(new File(System.getProperty("net.sf.odinms.wzpath") + "/String.wz")));
		mapFactory.setChannel(em.getChannelServer().getChannel());
	}
	
	public void registerPlayer(MapleCharacter chr) {
		try {
			chars.add(chr);
			chr.setEventInstance(this);
			em.getIv().invokeFunction("playerEntry", this, chr);
		} catch (ScriptException ex) {
			Logger.getLogger(EventInstanceManager.class.getName()).log(Level.SEVERE, null, ex);
		} catch (NoSuchMethodException ex) {
			Logger.getLogger(EventInstanceManager.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	
	public void registerParty(MapleParty party, MapleMap map) {
		for (MaplePartyCharacter pc : party.getMembers()) {
			MapleCharacter c = map.getCharacterById(pc.getId());
			registerPlayer(c);
		}
	}
	
	public void unregisterPlayer(MapleCharacter chr) {
		chars.remove(chr);
		chr.setEventInstance(null);
	}
	
	public int getPlayerCount() {
		return chars.size();
	}
	
	public List<MapleCharacter> getPlayers() {
		return new ArrayList<MapleCharacter>(chars);
	}
	
	public void registerMonster(MapleMonster mob) {
		mobs.add(mob);
		mob.setEventInstance(this);
	}
	
	public void unregisterMonster(MapleMonster mob) {
		mobs.remove(mob);
		mob.setEventInstance(null);
		if (mobs.size() == 0) {
			try {
				em.getIv().invokeFunction("allMonstersDead", this);
			} catch (ScriptException ex) {
				Logger.getLogger(EventInstanceManager.class.getName()).log(Level.SEVERE, null, ex);
			} catch (NoSuchMethodException ex) {
				Logger.getLogger(EventInstanceManager.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}
	
	public void playerKilled(MapleCharacter chr) {
		try {
			em.getIv().invokeFunction("playerDead", this, chr);
		} catch (ScriptException ex) {
			Logger.getLogger(EventInstanceManager.class.getName()).log(Level.SEVERE, null, ex);
		} catch (NoSuchMethodException ex) {
			Logger.getLogger(EventInstanceManager.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	
	public boolean revivePlayer(MapleCharacter chr) {
		try {
			Object b = em.getIv().invokeFunction("playerRevive", this, chr);
			if (b instanceof Boolean) {
				return (Boolean) b;
			}
		} catch (ScriptException ex) {
			Logger.getLogger(EventInstanceManager.class.getName()).log(Level.SEVERE, null, ex);
		} catch (NoSuchMethodException ex) {
			Logger.getLogger(EventInstanceManager.class.getName()).log(Level.SEVERE, null, ex);
		}
		return true;
	}
	
	public void playerDisconnected(MapleCharacter chr) {
		try {
			em.getIv().invokeFunction("playerDisconnected", this, chr);
		} catch (ScriptException ex) {
			Logger.getLogger(EventInstanceManager.class.getName()).log(Level.SEVERE, null, ex);
		} catch (NoSuchMethodException ex) {
			Logger.getLogger(EventInstanceManager.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	
	/**
	 * 
	 * @param chr
	 * @param mob
	 */
	public void monsterKilled(MapleCharacter chr, MapleMonster mob) {
		try {
			Integer kc = killCount.get(chr);
			int inc = ((Double) em.getIv().invokeFunction("monsterValue", this, mob.getId())).intValue();
			if (kc == null) {
				kc = inc;
			} else {
				kc += inc;
			}
			killCount.put(chr, kc);
		} catch (ScriptException ex) {
			Logger.getLogger(EventInstanceManager.class.getName()).log(Level.SEVERE, null, ex);
		} catch (NoSuchMethodException ex) {
			Logger.getLogger(EventInstanceManager.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	
	public int getKillCount(MapleCharacter chr) {
		Integer kc = killCount.get(chr);
		if (kc == null)
			return 0;
		else
			return kc;
	}
	
	public void dispose() {
		chars.clear();
		mobs.clear();
		killCount.clear();
		mapFactory = null;
		em.disposeInstance(name);
		em = null;
	}

	public MapleMapFactory getMapFactory() {
		return mapFactory;
	}
	
	public void schedule(final String methodName, long delay) {
		TimerManager.getInstance().schedule(new Runnable() {

			public void run() {
				try {
					em.getIv().invokeFunction(methodName, EventInstanceManager.this);
				} catch (ScriptException ex) {
					Logger.getLogger(EventManager.class.getName()).log(Level.SEVERE, null, ex);
				} catch (NoSuchMethodException ex) {
					Logger.getLogger(EventManager.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
			
		}, delay);
	}

	public String getName() {
		return name;
	}
	
	public void saveWinner(MapleCharacter chr) {
		try {
			Connection con = DatabaseConnection.getConnection();
			PreparedStatement ps = con.prepareStatement("INSERT INTO eventstats (event, instance, characterid, channel) VALUES (?, ?, ?, ?)");
			ps.setString(1, em.getName());
			ps.setString(2, getName());
			ps.setInt(3, chr.getId());
			ps.setInt(4, chr.getClient().getChannel());
			ps.executeUpdate();
		} catch (SQLException ex) {
			Logger.getLogger(EventInstanceManager.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	
	public MapleMap getMapInstance(int mapId) {
		boolean wasLoaded = mapFactory.isMapLoaded(mapId);
		MapleMap map = mapFactory.getMap(mapId);

		// in case reactors need shuffling and we are actually loading the map
		if (!wasLoaded) {
			if (em.getProperty("shuffleReactors") != null && em.getProperty("shuffleReactors").equals("true")) {
				map.shuffleReactors();
			}
		}

		return map;
	}

	public void setProperty(String key, String value) {
		props.setProperty(key, value);
	}
	
	public String getProperty(String key) {
		return props.getProperty(key);
	}
	
	public void leftParty(MapleCharacter chr) {
		try {
			em.getIv().invokeFunction("leftParty", this, chr);
		} catch (ScriptException ex) {
			Logger.getLogger(EventInstanceManager.class.getName()).log(Level.SEVERE, null, ex);
		} catch (NoSuchMethodException ex) {
			Logger.getLogger(EventInstanceManager.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	
	public void disbandParty() {
		try {
			em.getIv().invokeFunction("disbandParty", this);
		} catch (ScriptException ex) {
			Logger.getLogger(EventInstanceManager.class.getName()).log(Level.SEVERE, null, ex);
		} catch (NoSuchMethodException ex) {
			Logger.getLogger(EventInstanceManager.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	
	//Separate function to warp players to a "finish" map, if applicable
	public void finishPQ() {
		try {
			em.getIv().invokeFunction("clearPQ", this);
		} catch (ScriptException ex) {
			Logger.getLogger(EventInstanceManager.class.getName()).log(Level.SEVERE, null, ex);
		} catch (NoSuchMethodException ex) {
			Logger.getLogger(EventInstanceManager.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	
	public boolean isLeader(MapleCharacter chr) {
		return (chr.getParty().getLeader().getId() == chr.getId());
	}
}

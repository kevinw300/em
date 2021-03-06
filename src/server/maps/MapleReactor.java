package server.maps;

import java.awt.Rectangle;
import client.MapleClient;
import net.MaplePacket;
import scripting.reactor.ReactorScriptManager;
import net.packetcreator.MaplePacketCreator;
import tools.Pair;

public class MapleReactor extends AbstractMapleMapObject {
    //private static Logger log = LoggerFactory.getLogger(MapleReactor.class);

    private int rid;
    private MapleReactorStats stats;
    private byte state;
    private int delay;
    private MapleMap map;
    private boolean alive;

    public MapleReactor(MapleReactorStats stats, int rid) {
        this.stats = stats;
        this.rid = rid;
        alive = true;
    }

    public int getReactorId() {
        return rid;
    }

    public void setState(byte state) {
        this.state = state;
    }

    public byte getState() {
        return state;
    }

    public int getId() {
        return rid;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }

    public int getDelay() {
        return delay;
    }

    @Override
    public MapleMapObjectType getType() {
        return MapleMapObjectType.REACTOR;
    }

    public int getReactorType() {
        return stats.getType(state);
    }

    public void setMap(MapleMap map) {
        this.map = map;
    }

    public MapleMap getMap() {
        return map;
    }

    public Pair<Integer, Integer> getReactItem() {
        return stats.getReactItem(state);
    }

    public boolean isAlive() {
        return alive;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    @Override
    public void sendDestroyData(MapleClient client) {
        client.getSession().write(makeDestroyData());
    }

    public MaplePacket makeDestroyData() {
        return MaplePacketCreator.destroyReactor(this);
    }

    @Override
    public void sendSpawnData(MapleClient client) {
        client.getSession().write(makeSpawnData());
    }

    public MaplePacket makeSpawnData() {
        return MaplePacketCreator.spawnReactor(this);
    }

    //hitReactor command for item-triggered reactors
    public void hitReactor(MapleClient c) {
        hitReactor(0, (short) 0, c);
    }

    public void hitReactor(int charPos, short stance, MapleClient c) {
        if (stats.getType(state) < 999) {
            //type 2 = only hit from right (kerning swamp plants), 00 is air left 02 is ground left
            if (!(stats.getType(state) == 2 && (charPos == 0 || charPos == 2))) {
                //get next state
                state = stats.getNextState(state);

                if (stats.getNextState(state) == -1) {//end of reactor
                    if (stats.getType(state) < 100) { //reactor broken
                        if (delay > 0) {
                            map.destroyReactor(getObjectId());
                        } else {//trigger as normal
                            map.broadcastMessage(MaplePacketCreator.triggerReactor(this, stance));
                        }
                    } else { //item-triggered on final step
                        map.broadcastMessage(MaplePacketCreator.triggerReactor(this, stance));
                    }
                    ReactorScriptManager.getInstance().act(c, this);
                } else { //reactor not broken yet
                    map.broadcastMessage(MaplePacketCreator.triggerReactor(this, stance));
                }
            }
        }
    }

    public Rectangle getArea() {
        int height = stats.getBR().y - stats.getTL().y;
        int width = stats.getBR().x - stats.getTL().x;
        int origX = getPosition().x + stats.getTL().x;
        int origY = getPosition().y + stats.getTL().y;

        return new Rectangle(origX, origY, width, height);

    }

    @Override
    public String toString() {
        return "Reactor " + getObjectId() + " of type " + rid + " at position " + getPosition().toString();
    }
}

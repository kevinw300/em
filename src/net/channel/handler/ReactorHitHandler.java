package net.channel.handler;

import client.MapleClient;
import net.AbstractMaplePacketHandler;
import server.maps.MapleReactor;
import tools.data.input.SeekableLittleEndianAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReactorHitHandler extends AbstractMaplePacketHandler {

    private static final Logger log = LoggerFactory.getLogger(ReactorHitHandler.class);

    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        // 8C 00 <int Reactor unique ID> <character relative position> 00 00 00 <character stance?>

        int oid = slea.readInt();
        int charPos = slea.readInt();
        short stance = slea.readShort();

        MapleReactor reactor = c.getPlayer().getMap().getReactorByOid(oid);
        if (reactor != null) {
            reactor.hitReactor(charPos, stance, c);
        } else { // player hit a destroyed reactor, likely due to lag
            log.trace(c.getPlayer().getName() + "<" + c.getPlayer().getId() + "> attempted to hit destroyed reactor with oid " + oid);
        }
    }
}

package client;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Item implements IItem {

    private int id;
    private byte position;
    private short quantity;
    private String owner = "";
    protected List<String> log;
    //private static Logger log = LoggerFactory.getLogger(Item.class);

    public Item(int id, byte position, short quantity) {
        super();
        this.id = id;
        this.position = position;
        this.quantity = quantity;
        this.log = new LinkedList<String>();
    }

    // public static void loadInitialDataFromDB() {
    // try {
    // Connection con = DatabaseConnection.getConnection();
    // PreparedStatement ps = con.prepareStatement("SELECT MAX(inventoryitemid) " + "FROM inventoryitems");
    // ResultSet rs = ps.executeQuery();
    // if (rs.next()) {
    // lastOID = rs.getInt(1);
    // } else {
    // throw new DatabaseException("Could not retrieve current item OID");
    // }
    // } catch (SQLException e) {
    // log.error(e.toString());
    // }
    // }
    public IItem copy() {
        Item ret = new Item(id, position, quantity);
        ret.owner = owner;
        ret.log = new LinkedList<String>(log);
        return ret;
    }

    public void setPosition(byte position) {
        this.position = position;
    }

    public void setQuantity(short quantity) {
        this.quantity = quantity;
    }

    @Override
    public int getItemId() {
        return id;
    }

    @Override
    public byte getPosition() {
        return position;
    }

    @Override
    public short getQuantity() {
        return quantity;
    }

    @Override
    public byte getType() {
        return IItem.ITEM;
    }

    @Override
    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    @Override
    public int compareTo(IItem other) {
        if (Math.abs(position) < Math.abs(other.getPosition())) {
            return -1;
        } else if (Math.abs(position) == Math.abs(other.getPosition())) {
            return 0;
        } else {
            return 1;
        }
    }

    @Override
    public String toString() {
        return "Item: " + id + " quantity: " + quantity;
    }

    // no op for now as it eats too much ram :( once we have persistent inventoryids we can reenable it in some form.
    public void log(String msg, boolean fromDB) {
        // if (!fromDB) {
        // StringBuilder toLog = new StringBuilder("[");
        // toLog.append(Calendar.getInstance().getTime().toString());
        // toLog.append("] ");
        // toLog.append(msg);
        // log.add(toLog.toString());
        // } else {
        // log.add(msg);
        //		}
    }

    public List<String> getLog() {
        return Collections.unmodifiableList(log);
    }
}

/**
 * Created by velin.
 */
public class NetworkLink {
    Integer cost;
//    NetworkNode nodeId1;
//    NetworkNode nodeId2;


    public NetworkLink(Integer cost) {
        this.cost = cost;
//        this.nodeId1 = nodeId1;
//        this.nodeId2 = nodeId2;
    }

    @Override
    public String toString() {
        return String.valueOf(cost);
    }
}

/**
 * Created by velin.
 */
public class NetworkLink {
    Integer cost;
//    NetworkNode node1;
//    NetworkNode node2;


    public NetworkLink(Integer cost) {
        this.cost = cost;
//        this.node1 = node1;
//        this.node2 = node2;
    }

    @Override
    public String toString() {
        return "Link: cost{" + cost + "}";
    }
}

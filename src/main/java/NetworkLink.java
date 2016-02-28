/**
 * A class which will represent a network link between.
 */
public class NetworkLink {
    Integer cost;
    NetworkNode node1;
    NetworkNode node2;


    public NetworkLink(Integer cost, NetworkNode node1, NetworkNode node2) {
        this.cost = cost;
        this.node1 = node1;
        this.node2 = node2;
    }

    @Override
    public String toString() {
        return String.valueOf(cost);
    }

    public NetworkNode getNode1() {
        return node1;
    }

    public NetworkNode getNode2() {
        return node2;
    }
}

/**
 * A class which will represent a network link between two nodes.
 */
public class NetworkLink {
    /**
     * Cost of link.
     */
    Integer cost;

    /**
     * One of the nodes connected by the link.
     */
    NetworkNode node1;

    /**
     * The other node connected by the link.
     */
    NetworkNode node2;

    /**
     * Constructor
     * @param cost link cost
     * @param node1 one of the nodes connected by the link
     * @param node2 the other node connected by the link
     */
    public NetworkLink(Integer cost, NetworkNode node1, NetworkNode node2) {
        this.cost = cost;
        this.node1 = node1;
        this.node2 = node2;
    }

    /**
     * Getter for node1
     * @return node1
     */
    public NetworkNode getNode1() {
        return node1;
    }

    /**
     * Getter for node2
     * @return node2
     */
    public NetworkNode getNode2() {
        return node2;
    }

    @Override
    public String toString() {
        return String.valueOf(cost);
    }
}

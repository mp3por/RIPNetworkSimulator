/**
 * Created by velin.
 */
public class NetworkLink {
    Integer cost;

    public NetworkLink(Integer cost) {
        this.cost = cost;
    }

    @Override
    public String toString() {
        return "Link: cost-" + cost;
    }
}

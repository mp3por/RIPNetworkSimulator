import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.*;

/**
 * The simulator.
 * <p>
 * It registers when a route table of a node has changed.
 * It also finds the best route between two nodes.
 */
public class Simulator implements RouteTable.NetworkNodeRouteTableListener, ShowBestRouteEvent.ShowBestRouteCapable {
    public static final Integer DEFAULT_NUM_OF_EXCHANGES = 100;

    private final NetworkLink[][] links;
    private final int numOfNodes;

    private final ArrayList<Integer> nodesWithChangedRoutingTables = new ArrayList<Integer>();

    private final HashMap<Integer, NetworkNode> nodesMap;

    private final Integer maxExchanges;
    private final HashMap<Integer, ArrayList<ScheduledEvent>> scheduledEvents;
    private final Boolean manual;
    private final Integer infinityCost;
    private boolean splitHorizon;
    private boolean isStable = false;
    private final boolean untilStability;

    public void startSimulation() {
        // for clarity
        System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! Starting simulation !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");

        // print simulator initial state
        printCosts();
        printStateOfNodes();
        printScheduledEvents();

        // automatic vs manual
        Scanner userInput = new Scanner(System.in);
        String userInputLine;
        for (int currIteration = 0; currIteration < maxExchanges; currIteration++) {
            boolean shouldContinue;
            if (!manual) {
                shouldContinue = simulateRound(currIteration);
            } else {
                System.out.println("Press enter to simulate next round or toggle split horizon");
                userInputLine = userInput.nextLine();
                if (userInputLine == null) {
                    break;
                } else if (userInputLine.equals("")) {
                    // enter pressed
                    shouldContinue = simulateRound(currIteration);
                } else if (userInputLine.equals("split-horizon-on")) {
                    // split horizon on request
                    splitHorizonOn();
                    shouldContinue = simulateRound(currIteration);
                } else if (userInputLine.equals("split-horizon-off")) {
                    // split horizon off request
                    splitHorizonOff();
                    shouldContinue = simulateRound(currIteration);
                } else {
                    // unrecognized command
                    System.out.println("Command not recognized.");
                    shouldContinue = true;
                }
            }
            if (!shouldContinue) {
                break;
            }
        }
        System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! End simulation !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
    }

    /**
     * Simulates one exchange
     *
     * @param currExchange the index of the exchange to be simulated
     * @return true if simulation should continue, false if not
     */
    public boolean simulateRound(int currExchange) {
        System.out.println("========================= Round " + currExchange + " ==========================");

        // simulate network exchange
        simulateNetworkExchange();
        System.out.println();

        if (isStable) {
            // network stable. Decide if simulations should continue.
            if (untilStability) {
                System.out.println("------- !!! ------- Stability reached after exchange: " + (currExchange - 1) + " ------- !!! ------\n");
                return false;
            } else {
                System.out.println("------- !!! ------- Network stable for this round ------- !!! ------\n");
            }
        } else {
            // network was not stable.
            System.out.println("------- !!! ------- There were a changes in node routing tables ------- !!! -------");

            // print ids of nodes with changes
            printChangedNodes();
        }

        // simulate network events
        simulateNetworkEvents(currExchange);

        System.out.println("========================= End round " + currExchange + " ==========================\n\n");
        return true;
    }

    /**
     * Prints ids of nodes which route's tables have changed during the previous exchange
     */
    private void printChangedNodes() {
        System.out.println("Nodes with changed routing tables: ");
        Collections.sort(nodesWithChangedRoutingTables);
        for (Integer nodeIdWithChangedRoutingTable : nodesWithChangedRoutingTables) {
            System.out.print(nodeIdWithChangedRoutingTable + " ");
        }
        System.out.println();
        System.out.println();
    }

    /**
     * Simulates network events
     *
     * @param currExchange the current exchange index
     */
    private void simulateNetworkEvents(int currExchange) {
        ArrayList<ScheduledEvent> scheduledEvents = this.scheduledEvents.get(currExchange);
        if (scheduledEvents != null) {
            // events exist so execute them
            System.out.println("########### ScheduledEvents after exchange " + currExchange + " ###########");
            for (ScheduledEvent event : scheduledEvents) {
                event.executeEvent(currExchange);
                System.out.println();
            }
        }
    }

    /**
     * Simulates a network exchange
     */
    private void simulateNetworkExchange() {
        System.out.println("simulate network exchange start");

        // reset stability check
        resetStabilityCheck();

        // tell every node to contact its neighbour
        for (NetworkNode node : nodesMap.values()) {
            node.sendCostsToNeighbours();
        }

        System.out.println("simulate network exchange finish");
    }

    /**
     * Print link costs matrix
     */
    public void printCosts() {
        StringBuilder b = new StringBuilder("links costs:\n");
        for (int i = 0; i < numOfNodes; i++) {
            for (int y = 0; y < numOfNodes; y++) {
                b.append("\t" + links[i][y]);
            }
            b.append("\n");
        }
        System.out.println(b.toString());
    }

    /**
     * Prints scheduled events.
     */
    public void printScheduledEvents() {
        StringBuilder b = new StringBuilder("Scheduled events:\n");
        ArrayList<Integer> sortedKeys = new ArrayList<Integer>(scheduledEvents.keySet());
        for (Integer key : sortedKeys) {
            ArrayList<ScheduledEvent> scheduledEvents = this.scheduledEvents.get(key);
            if (scheduledEvents != null) {
                b.append("\tAt exchange " + key + " : [");
                for (ScheduledEvent event : scheduledEvents) {
                    b.append(event.toString() + "; ");
                }
                b.append("]");
            }
            b.append("\n");
        }
        System.out.println(b.toString());
    }

    /**
     * Prints the current state of nodes in the network
     */
    public void printStateOfNodes() {
        StringBuilder b = new StringBuilder("State of nodes:\n\n");
        for (NetworkNode node : nodesMap.values()) {
            b.append(node.toString() + "\n");
        }
        System.out.println(b.toString());
    }

    /**
     * Resets the stability check.
     */
    private void resetStabilityCheck() {
        nodesWithChangedRoutingTables.clear();
        isStable = true;
    }

    /**
     * Handles change in a node route table
     *
     * @param nodeId the id of the node whose route table registered an update
     */
    public void onRouteTableUpdate(Integer nodeId) {
        if (!nodesWithChangedRoutingTables.contains(nodeId)) {
            nodesWithChangedRoutingTables.add(nodeId);
        }
        isStable = false;
    }

    /**
     * Helper method for turning split-horizon off.
     */
    private void splitHorizonOff() {
        System.out.println("split horizon on");
        splitHorizon = false;
        for (NetworkNode node : nodesMap.values()) {
            node.setSplitHorizon(splitHorizon);
        }
    }

    /**
     * Helper method for turning split-horizon on.
     */
    private void splitHorizonOn() {
        System.out.println("split horizon off");
        splitHorizon = true;
        for (NetworkNode node : nodesMap.values()) {
            node.setSplitHorizon(splitHorizon);
        }
    }

    /**
     * Constructor
     *
     * @param fileName the config file name
     * @throws Exception
     */
    public Simulator(String fileName) throws Exception {
        // read file and parse into lines
        String[] inputLines = FileUtils.readFileToString(new File(fileName)).split("\n");
        int inputLinesIndex = 0;

        // look for numOfNodes
        List<String> configValues = Arrays.asList(inputLines[inputLinesIndex++].split("\\s+"));
        int numOfNodesIndex = configValues.indexOf("-numOfNodes");
        if (numOfNodesIndex == -1) {
            // this program wants at least this parameter
            throw new Exception("Must specify number of nodes in the network");
        }
        numOfNodes = Integer.valueOf(configValues.get(numOfNodesIndex + 1));
        if (numOfNodes <= 0) {
            throw new Exception("numOfNodes must be in the range [1, inf).");
        }

        // look maxExchanges flag
        int maxExchangesIndex = configValues.indexOf("-maxExchanges");
        if (maxExchangesIndex != -1) {
            maxExchanges = Integer.valueOf(configValues.get(maxExchangesIndex + 1));
            if (maxExchanges <= 0) {
                throw new Exception("maxExchanges must be in the range [1, inf).");
            }
        } else {
            maxExchanges = DEFAULT_NUM_OF_EXCHANGES;
        }

        // look for stability flag
        int untilStabilityIndex = configValues.indexOf("-untilStability");
        if (untilStabilityIndex != -1) {
            untilStability = Boolean.valueOf(configValues.get(untilStabilityIndex + 1));
        } else {
            untilStability = false;
        }

        // look for manual flag
        int manualIndex = configValues.indexOf("-manual");
        if (manualIndex != -1) {
            manual = Boolean.valueOf(configValues.get(manualIndex + 1));
        } else {
            manual = false;
        }

        // look for split horizon flag
        int splitHorizonIndex = configValues.indexOf("-splitHorizon");
        if (splitHorizonIndex != -1) {
            splitHorizon = configValues.get(splitHorizonIndex + 1).equals("on");
        } else {
            splitHorizon = false;
        }

        // look for infinity
        int infinityCostIndex = configValues.indexOf("-infinity");
        if (infinityCostIndex != -1) {
            infinityCost = Integer.valueOf(configValues.get(infinityCostIndex + 1));
            if (infinityCost <= 0) {
                throw new Exception("infinityCost must be in the range [1, inf).");
            }
        } else {
            infinityCost = RouteTable.INFINITY_COST_DEFAULT;
        }


        // instantiate nodes
        nodesMap = new HashMap<Integer, NetworkNode>();
        for (int i = 0; i < numOfNodes; i++) {
            NetworkNode node = new NetworkNode(i, this, splitHorizon, infinityCost);
            nodesMap.put(i, node);
        }

        // instantiate links with default value FAILED_LINK_COST = -1 => not connected
        links = new NetworkLink[numOfNodes][numOfNodes];
        for (int i = 0; i < numOfNodes; i++) {
            for (int y = i; y < numOfNodes; y++) {
                if (i == y) {
                    // distance to self = 0
                    links[i][y] = new NetworkLink(0, nodesMap.get(i), nodesMap.get(y));
                } else {
                    // distance to others set to FAILED_LINK_COST
                    NetworkLink networkLink = new NetworkLink(RouteTable.FAILED_LINK_COST, nodesMap.get(i), nodesMap.get(y));
                    links[i][y] = networkLink;
                    links[y][i] = networkLink;
                }
            }
        }

        // parse connections
        for (; inputLinesIndex < inputLines.length; inputLinesIndex++) {
            String inputLine = inputLines[inputLinesIndex];
            if (inputLine.contains("##")) {
                break;
            }
            String[] inputLineValues = inputLine.split("\\s+");
            Integer fromNodeId = Integer.valueOf(inputLineValues[0]);
            Integer toNodeId = Integer.valueOf(inputLineValues[1]);
            Integer linkCost = Integer.valueOf(inputLineValues[2]);
            if (fromNodeId >= numOfNodes || toNodeId >= numOfNodes) {
                throw new Exception("node with ID >= numOfNodes (" + numOfNodes + ") supplied in the links matrix");
            }
            NetworkLink networkLink = links[fromNodeId][toNodeId];
            networkLink.cost = linkCost;
            NetworkNode fromNode = nodesMap.get(fromNodeId);
            NetworkNode toNode = nodesMap.get(toNodeId);
            fromNode.addNeighbour(toNode, networkLink);
            toNode.addNeighbour(fromNode, networkLink);
        }
        inputLinesIndex++; // move to next line

        // parse link changes
        scheduledEvents = new HashMap<Integer, ArrayList<ScheduledEvent>>();
        for (; inputLinesIndex < inputLines.length; inputLinesIndex++) {
            String inputLine = inputLines[inputLinesIndex];
            if (inputLine.contains("##")) {
                break;
            }
            String[] inputLineValues = inputLine.split("\\s+");
            Integer fromNodeId = Integer.valueOf(inputLineValues[0]);
            Integer toNodeId = Integer.valueOf(inputLineValues[1]);
            Integer changeAfterExchange = Integer.valueOf(inputLineValues[2]);
            Integer newCost = Integer.valueOf(inputLineValues[3]);
            if (fromNodeId >= numOfNodes || toNodeId >= numOfNodes) {
                throw new Exception("node with ID >= numOfNodes (" + numOfNodes + ") used in the Link-Cost-Change scheduling.");
            }
            if (changeAfterExchange >= maxExchanges) {
                System.out.println("WARNING! A link cost change has been scheduled to happen after the simulation has finished. ChangeAfterExchange index > maxExchanges");
            }
            if (newCost < -1) {
                System.out.println("WARNING! A link cost change has been scheduled with unappropriate new cost value (<-1). Changing to -1.");
                newCost = RouteTable.FAILED_LINK_COST;
            }
            NetworkLink networkLink = links[fromNodeId][toNodeId];
            LinkCostChangeEvent event = new LinkCostChangeEvent(changeAfterExchange, networkLink, newCost);
            ArrayList<ScheduledEvent> scheduledNetworkEvents = scheduledEvents.get(changeAfterExchange);
            if (scheduledNetworkEvents == null) {
                scheduledNetworkEvents = new ArrayList<ScheduledEvent>();
                scheduledEvents.put(changeAfterExchange, scheduledNetworkEvents);
            }
            scheduledNetworkEvents.add(event);
        }
        inputLinesIndex++;

        // parse show best route
        for (; inputLinesIndex < inputLines.length; inputLinesIndex++) {
            String inputLine = inputLines[inputLinesIndex];
            if (inputLine.contains("##")) {
                break;
            }
            String[] inputLineValues = inputLine.split("\\s+");
            Integer fromNodeId = Integer.valueOf(inputLineValues[0]);
            Integer toNodeId = Integer.valueOf(inputLineValues[1]);
            Integer showAfterExchange = Integer.valueOf(inputLineValues[2]);
            if (fromNodeId >= numOfNodes || toNodeId >= numOfNodes) {
                throw new Exception("node with ID >= numOfNodes (" + numOfNodes + ") used in the Show-Best-Route scheduling.");
            }
            if (showAfterExchange >= maxExchanges) {
                System.out.println("WARNING! A Show-Best-Route has been scheduled to happen after the simulation has finished. ChangeAfterExchange index > maxExchanges");
            }
            ShowBestRouteEvent event = new ShowBestRouteEvent(showAfterExchange, nodesMap.get(fromNodeId), nodesMap.get(toNodeId), this);
            ArrayList<ScheduledEvent> scheduledNetworkEvents = scheduledEvents.get(showAfterExchange);
            if (scheduledNetworkEvents == null) {
                scheduledNetworkEvents = new ArrayList<ScheduledEvent>();
                scheduledEvents.put(showAfterExchange, scheduledNetworkEvents);
            }
            scheduledNetworkEvents.add(event);
        }
        inputLinesIndex++;

        // parse trace routing tables
        for (; inputLinesIndex < inputLines.length; inputLinesIndex++) {
            String inputLine = inputLines[inputLinesIndex];
            if (inputLine.contains("##")) {
                break;
            }
            String[] inputLineValues = inputLine.split("\\s+");
            Integer nodeId = Integer.valueOf(inputLineValues[0]);
            Integer exchangeStartIndex = Integer.valueOf(inputLineValues[1]);
            Integer exchangeEndIndex = Integer.valueOf(inputLineValues[2]);
            if (nodeId >= numOfNodes) {
                throw new Exception("node with ID >= numOfNodes (" + numOfNodes + ") used in the Trace-Route-Table scheduling.");
            }
            if (exchangeStartIndex >= maxExchanges || exchangeEndIndex >= maxExchanges) {
                System.out.println("WARNING! A Trace-Route-Table has been scheduled to happen after the simulation has finished. ExchangeStartIndex || ExchangeEndIndex > maxExchanges");
            }
            TraceRouteTableEvent event = new TraceRouteTableEvent(nodesMap.get(nodeId), exchangeStartIndex, exchangeEndIndex);
            for (int i = exchangeStartIndex; i < exchangeEndIndex; i++) {
                ArrayList<ScheduledEvent> scheduledNetworkEvents = scheduledEvents.get(i);
                if (scheduledNetworkEvents == null) {
                    scheduledNetworkEvents = new ArrayList<ScheduledEvent>();
                    scheduledEvents.put(i, scheduledNetworkEvents);
                }
                scheduledNetworkEvents.add(event);
            }
        }


        startSimulation();
    }


    /**
     * Finds best route between two nodes.
     *
     * @param fromNode from node
     * @param toNode   to node
     * @param currPath current path
     * @return the path between the two nodes
     */
    public ArrayList<NetworkNode> findBestRoute(NetworkNode fromNode, NetworkNode toNode, ArrayList<NetworkNode> currPath) {
        if (fromNode == null || toNode == null || fromNode.getNodeId().equals(toNode.getNodeId())) {
            return null;
        } else {
            Integer nextNodeInPathId = fromNode.getNextHopToDest(toNode);
            if (nextNodeInPathId != null) {
                NetworkNode nextNodeInPath = nodesMap.get(nextNodeInPathId);
                if (currPath.contains(nextNodeInPath)) {
                    System.out.println("There is a cycle in the routes meaning the end node (" + toNode.getNodeId() + ") has become unreachable!");
                    return currPath;
                } else {
                    currPath.add(nextNodeInPath);
                    findBestRoute(nextNodeInPath, toNode, currPath);
                    return currPath;
                }
            } else {
                currPath.add(null);
                return currPath;
            }
        }
    }
}

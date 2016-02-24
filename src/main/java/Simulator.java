import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.*;

/**
 * Created by velin.
 */
public class Simulator implements RouteTable.NetworkNodeRouteTableListener, ShowBestRouteEvent.ShowBestRouteCapable {
    public static final Integer DEFAULT_NUM_OF_EXCHANGES = 100;

    private final NetworkLink[][] links;
    private final int numOfNodes;

    private final HashMap<Integer, NetworkNode> nodesMap;

    private final Integer maxExchanges;
    private final HashMap<Integer, HashSet<ScheduledEvent>> scheduledEvents;
    private final Boolean manual;
    private boolean splitHorizon;
    private boolean isStable = false;
    private final boolean untilStability;

    public void startSimulation() {
        System.out.println("Starting simulation");
        printCosts();
        printStateOfNodes();
        printScheduledEvents();

        Scanner userInput = new Scanner(System.in);
        String userInputLine;
        for (int currIteration = 0; currIteration <= maxExchanges; currIteration++) {
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
    }

    private void splitHorizonOff() {
        System.out.println("split horizon on");
        splitHorizon = false;
        for (NetworkNode node : nodesMap.values()) {
            node.setSplitHorizon(splitHorizon);
        }
    }

    private void splitHorizonOn() {
        System.out.println("split horizon off");
        splitHorizon = true;
        for (NetworkNode node : nodesMap.values()) {
            node.setSplitHorizon(splitHorizon);
        }
    }

    public boolean simulateRound(int currExchange) {
        System.out.println("--------------- Round " + currExchange + " -----------------");

        simulateNetworkExchange();
        System.out.println();
        if (isStable) {
            if (untilStability) {
                System.out.println("Stability reached after exchange: " + (currExchange - 1));
                return false;
            } else {
                System.out.println("Network stable for this round");
                System.out.println();
//                    printStateOfNodes();
            }

        } else {
            System.out.println("There was a change(s). Nodes state after change(s):");
            printStateOfNodes();
        }
        simulateNetworkEvents(currExchange);

        System.out.println("--------------- End round " + currExchange + " -----------------------------------");
        System.out.println();
        System.out.println();
        return true;
    }

    private void simulateNetworkEvents(int currExchange) {
        HashSet<ScheduledEvent> scheduledEvents = this.scheduledEvents.get(currExchange);
        if (scheduledEvents != null) {
            System.out.println("ScheduledEvents after exchange " + currExchange);
            for (ScheduledEvent event : scheduledEvents) {
                event.executeEvent(currExchange);
            }
        }
    }

    private void simulateNetworkExchange() {
        System.out.println("simulate network exchange start");
        isStable = true;
        for (NetworkNode node : nodesMap.values()) {
            node.sendCostsToNeighbours();
        }
        System.out.println("simulate network exchange finish");
    }

    public void printCosts() {
        StringBuilder b = new StringBuilder("links:\n");
        for (int i = 0; i < numOfNodes; i++) {
            for (int y = 0; y < numOfNodes; y++) {
                b.append("\t" + links[i][y]);
            }
            b.append("\n");
        }
        System.out.println(b.toString());
    }

    public void printScheduledEvents() {
        StringBuilder b = new StringBuilder("Scheduled events:\n");
        ArrayList<Integer> sortedKeys = new ArrayList<Integer>(scheduledEvents.keySet());
        for (Integer key : sortedKeys) {
            HashSet<ScheduledEvent> scheduledEvents = this.scheduledEvents.get(key);
            if (scheduledEvents != null) {
                b.append("\t" + key + " : [");
                for (ScheduledEvent event : scheduledEvents) {
                    b.append(event.toString() + "; ");
                }
                b.append("]");
            }
            b.append("\n");
        }
        System.out.println(b.toString());
    }

    public void printNodes() {
        StringBuilder b = new StringBuilder("nodes:\n");

        for (NetworkNode networkNode : nodesMap.values()) {
            b.append(networkNode + "\n");
        }
        System.out.println(b.toString());
    }

    public void printStateOfNodes() {
        StringBuilder b = new StringBuilder("State of nodes:\n");
        for (NetworkNode node : nodesMap.values()) {
            b.append(node.toString() + "\n");
        }
        System.out.println(b.toString());
    }


    public void onRouteTableUpdate(Integer nodeId) {
        isStable = false;
    }

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

        // look maxExchanges flag
        int maxExchangesIndex = configValues.indexOf("-maxExchanges");
        if (maxExchangesIndex != -1) {
            maxExchanges = Integer.valueOf(configValues.get(maxExchangesIndex + 1));
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


        // instantiate nodes
        nodesMap = new HashMap<Integer, NetworkNode>();
        for (int i = 0; i < numOfNodes; i++) {
            NetworkNode node = new NetworkNode(i, this, splitHorizon);
            nodesMap.put(i, node);
        }

        // instantiate links with default value FAILED_LINK_COST = -1 => not connected
        links = new NetworkLink[numOfNodes][numOfNodes];
        for (int i = 0; i < numOfNodes; i++) {
            for (int y = i; y < numOfNodes; y++) {
                if (i == y) {
                    // distance to self = 0
                    links[i][y] = new NetworkLink(0);
                } else {
                    // distance to others set to FAILED_LINK_COST
                    NetworkLink networkLink = new NetworkLink(RouteTable.FAILED_LINK_COST);
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
                throw new Exception("node with ID greater than numOfNodes (" + numOfNodes + ") supplied in the links matrix");
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
        scheduledEvents = new HashMap<Integer, HashSet<ScheduledEvent>>();
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
                throw new Exception("node with ID greater than numOfNodes (" + numOfNodes + ") used in the Link-Cost-Change scheduling.");
            }
            if (changeAfterExchange >= maxExchanges) {
                System.out.println("WARNING! A link cost change has been scheduled to happen after the simulation has finished. ChangeAfterExchange index > maxExchanges");
            }
            if (newCost < -1) {
                System.out.println("WARNING! A link cost change has been scheduled with unappropriate new cost value (<-1). Changing to -1.");
                newCost = RouteTable.FAILED_LINK_COST;
            }
            NetworkLink networkLink = links[fromNodeId][toNodeId];
            LinkCostChangeEvent event = new LinkCostChangeEvent(changeAfterExchange, networkLink, newCost, nodesMap.get(fromNodeId), nodesMap.get(toNodeId));
            HashSet<ScheduledEvent> scheduledNetworkEvents = scheduledEvents.get(changeAfterExchange);
            if (scheduledNetworkEvents == null) {
                scheduledNetworkEvents = new HashSet<ScheduledEvent>();
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
                throw new Exception("node with ID greater than numOfNodes (" + numOfNodes + ") used in the Show-Best-Route scheduling.");
            }
            if (showAfterExchange >= maxExchanges) {
                System.out.println("WARNING! A Show-Best-Route has been scheduled to happen after the simulation has finished. ChangeAfterExchange index > maxExchanges");
            }
            ShowBestRouteEvent event = new ShowBestRouteEvent(showAfterExchange, nodesMap.get(fromNodeId), nodesMap.get(toNodeId), this);
            HashSet<ScheduledEvent> scheduledNetworkEvents = scheduledEvents.get(showAfterExchange);
            if (scheduledNetworkEvents == null) {
                scheduledNetworkEvents = new HashSet<ScheduledEvent>();
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
            if (nodeId>= numOfNodes) {
                throw new Exception("node with ID greater than numOfNodes (" + numOfNodes + ") used in the Trace-Route-Table scheduling.");
            }
            if (exchangeStartIndex >= maxExchanges || exchangeEndIndex >= maxExchanges) {
                System.out.println("WARNING! A Trace-Route-Table has been scheduled to happen after the simulation has finished. ExchangeStartIndex || ExchangeEndIndex > maxExchanges");
            }
            TraceRouteTableEvent event = new TraceRouteTableEvent(nodesMap.get(nodeId), exchangeStartIndex, exchangeEndIndex);
            for (int i = exchangeStartIndex; i < exchangeEndIndex; i++) {
                HashSet<ScheduledEvent> scheduledNetworkEvents = scheduledEvents.get(i);
                if (scheduledNetworkEvents == null) {
                    scheduledNetworkEvents = new HashSet<ScheduledEvent>();
                    scheduledEvents.put(i, scheduledNetworkEvents);
                }
                scheduledNetworkEvents.add(event);
            }
        }


        startSimulation();
    }


    public ArrayList<NetworkNode> findBestRoute(NetworkNode fromNode, NetworkNode toNode, ArrayList<NetworkNode> currPath) {
        if (fromNode.getNodeId().equals(toNode.getNodeId())) {
            return null;
        } else if (fromNode.isNodeNeighbour(toNode)) {
//            currPath.add(toNode);
            return currPath;
        } else {
            Integer nextNodeInPathId = fromNode.getNextHopToDest(toNode);
            if (nextNodeInPathId != null) {
                NetworkNode nextNodeInPath = nodesMap.get(nextNodeInPathId);
                currPath.add(nextNodeInPath);
                return findBestRoute(nextNodeInPath, toNode, currPath);
            } else {
                currPath.add(null);
                return currPath;
            }
        }
    }
}

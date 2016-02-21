import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.*;

/**
 * Created by velin.
 */
public class Simulator implements RouteTable.NetworkNodeRouteTableListener, ShowBestRouteEvent.ShowBestRouteCapable {
    public static final Integer DEFAULT_NUM_OF_ITERATIONS = 100;

    private final NetworkLink[][] links;
    private final int numOfNodes;

    private final HashMap<Integer, NetworkNode> nodesMap;

    private final Integer numOfIterations;
    private final HashMap<Integer, HashSet<ScheduledEvent>> scheduledEvents;
    private final Boolean manual;
    private boolean isStable = false;
    private boolean untilStability;

    public void startSimulation() {
        System.out.println("Starting simulation");
        printCosts();
        printStateOfNodes();
        printScheduledEvents();

        Scanner userInput = new Scanner(System.in);
        String userInputLine;
        for (int currIteration = 1; currIteration <= numOfIterations; currIteration++) {
            boolean shouldContinue;
            if (!manual) {
                shouldContinue = simulateRound(currIteration);
            } else {
                System.out.println("Press enter to simulate next round or toggle split horizon");
                userInputLine = userInput.nextLine();
                if (userInput == null) {
                    break;
                } else if (userInputLine.equals("")) {
                    // enter pressed
                    shouldContinue = simulateRound(currIteration);
                } else if (userInput.equals("split-horizon-on")) {
                    // split horizon on request
                    splitHorizonOn();
                    shouldContinue = simulateRound(currIteration);
                } else if (userInput.equals("split-horizon-off")) {
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
    }

    private void splitHorizonOn() {
        System.out.println("split horizon off");
    }

    public boolean simulateRound(int currIteration) {
        System.out.println("--------------- Round " + currIteration + " -----------------");

        simulateNetworkExchange();
        System.out.println();
        if (isStable) {
            if (untilStability) {
                System.out.println("Stability reached after iteration: " + (currIteration - 1));
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
        simulateNetworkEvents(currIteration);

        System.out.println("--------------- End round " + currIteration + " -----------------------------------");
        System.out.println();
        System.out.println();
        return true;
    }

    private void simulateNetworkEvents(int currIteration) {
        HashSet<ScheduledEvent> scheduledEvents = this.scheduledEvents.get(currIteration);
        if (scheduledEvents != null) {
            System.out.println("ScheduledEvents after exchange " + currIteration);
            for (ScheduledEvent event : scheduledEvents) {
                event.executeEvent();
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
        List<String> configValues = Arrays.asList(inputLines[inputLinesIndex++].split(" "));
        int numOfNodesIndex = configValues.indexOf("-numOfNodes");
        if (numOfNodesIndex == -1) {
            // this program wants at least this parameter
            throw new Exception("Must specify number of nodes in the network");
        }
        numOfNodes = Integer.valueOf(configValues.get(numOfNodesIndex + 1));

        // instantiate nodes
        nodesMap = new HashMap<Integer, NetworkNode>();
        for (int i = 0; i < numOfNodes; i++) {
            NetworkNode node = new NetworkNode(i, this);
            nodesMap.put(i, node);
        }

        // instantiate links with default value -1 => not connected
        links = new NetworkLink[numOfNodes][numOfNodes];
        for (int i = 0; i < numOfNodes; i++) {
            for (int y = i; y < numOfNodes; y++) {
                if (i == y) {
                    links[i][y] = new NetworkLink(0);
                } else {
                    NetworkLink networkLink = new NetworkLink(-1);
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
            String[] inputLineValues = inputLine.split(" ");
            Integer fromNodeId = Integer.valueOf(inputLineValues[0]);
            Integer toNodeId = Integer.valueOf(inputLineValues[1]);
            Integer linkCost = Integer.valueOf(inputLineValues[2]);
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
            String[] inputLineValues = inputLine.split(" ");
            Integer fromNodeId = Integer.valueOf(inputLineValues[0]);
            Integer toNodeId = Integer.valueOf(inputLineValues[1]);
            Integer changeAfterExchange = Integer.valueOf(inputLineValues[2]);
            Integer newCost = Integer.valueOf(inputLineValues[3]);
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
            String[] inputLineValues = inputLine.split(" ");
            Integer fromNodeId = Integer.valueOf(inputLineValues[0]);
            Integer toNodeId = Integer.valueOf(inputLineValues[1]);
            Integer showAfterExchange = Integer.valueOf(inputLineValues[2]);
            ShowBestRouteEvent event = new ShowBestRouteEvent(showAfterExchange, nodesMap.get(fromNodeId), nodesMap.get(toNodeId), this);
            HashSet<ScheduledEvent> scheduledNetworkEvents = scheduledEvents.get(showAfterExchange);
            if (scheduledNetworkEvents == null) {
                scheduledNetworkEvents = new HashSet<ScheduledEvent>();
                scheduledEvents.put(showAfterExchange, scheduledNetworkEvents);
            }
            scheduledNetworkEvents.add(event);
        }

        // look maxIterations flag
        int maxIterationsIndex = configValues.indexOf("-maxIterations");
        if (maxIterationsIndex != -1) {
            numOfIterations = Integer.valueOf(configValues.get(maxIterationsIndex + 1));
        } else {
            numOfIterations = DEFAULT_NUM_OF_ITERATIONS;
        }

        // look for stability flag
        int untilStabilityIndex = configValues.indexOf("-untilStability");
        if (untilStabilityIndex != -1) {
            untilStability = Boolean.valueOf(configValues.get(untilStabilityIndex + 1));
        }

        // look for manual flag
        int manualIndex = configValues.indexOf("-manual");
        if (manualIndex != -1) {
            manual = Boolean.valueOf(configValues.get(manualIndex + 1));
        } else {
            manual = false;
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

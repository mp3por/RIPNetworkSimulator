import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.*;

public class Main {
    public static void main(String[] args) throws Exception {
        // read file and parse into lines
        String[] inputLines = FileUtils.readFileToString(new File("src/main/java/input.txt")).split("\n");
        int inputLinesIndex = 0;

        // look for numOfNodes
        List<String> configValues = Arrays.asList(inputLines[inputLinesIndex++].split(" "));
        int numOfNodesIndex = configValues.indexOf("-numOfNodes");
        if (numOfNodesIndex == -1) {
            // this program wants at least this parameter
            throw new Exception("Must specify number of nodes in the network");
        }
        Integer numOfNodes = Integer.valueOf(configValues.get(numOfNodesIndex + 1));

        // instantiate links with default value -1 => not connected
        NetworkLink[][] links = new NetworkLink[numOfNodes][numOfNodes];
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
        HashMap<Integer, ArrayList<Integer>> connections = new HashMap<Integer, ArrayList<Integer>>();
        for (; inputLinesIndex < inputLines.length; inputLinesIndex++) {
            String inputLine = inputLines[inputLinesIndex];
            if (inputLine.contains("##")) {
                break;
            }
            String[] inputLineValues = inputLine.split(" ");
            Integer fromNodeId = Integer.valueOf(inputLineValues[0]);
            Integer toNodeId = Integer.valueOf(inputLineValues[1]);
            Integer linkCost = Integer.valueOf(inputLineValues[2]);
            links[fromNodeId][toNodeId].cost = linkCost;
            ArrayList<Integer> fromNodeConnections = connections.get(fromNodeId);
            if (fromNodeConnections == null) {
                fromNodeConnections = new ArrayList<Integer>();
                connections.put(fromNodeId, fromNodeConnections);
            }
            fromNodeConnections.add(toNodeId);

            ArrayList<Integer> toNodeConnections = connections.get(toNodeId);
            if (toNodeConnections == null) {
                toNodeConnections = new ArrayList<Integer>();
                connections.put(toNodeId, toNodeConnections);
            }
            toNodeConnections.add(fromNodeId);
        }
        inputLinesIndex++; // move to next line

        // parse link changes
        HashMap<Integer, HashSet<ScheduledNetworkEvent>> scheduledEvents = new HashMap<Integer, HashSet<ScheduledNetworkEvent>>();
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
            LinkCostChangeNetworkEvent event = new LinkCostChangeNetworkEvent(changeAfterExchange, networkLink, newCost, fromNodeId, toNodeId);
            HashSet<ScheduledNetworkEvent> scheduledNetworkEvents = scheduledEvents.get(changeAfterExchange);
            if (scheduledNetworkEvents == null) {
                scheduledNetworkEvents = new HashSet<ScheduledNetworkEvent>();
                scheduledEvents.put(changeAfterExchange, scheduledNetworkEvents);
            }
            scheduledNetworkEvents.add(event);
        }

        // look maxIterations flag
        Integer numOfIterations = null;
        int maxIterationsIndex = configValues.indexOf("-maxIterations");
        if (maxIterationsIndex != -1) {
            numOfIterations = Integer.valueOf(configValues.get(maxIterationsIndex + 1));
        }

        // look for stability flag
        boolean untilStability = false;
        int untilStabilityIndex = configValues.indexOf("-untilStability");
        if (untilStabilityIndex != -1) {
            untilStability = Boolean.valueOf(configValues.get(untilStabilityIndex + 1));
        }


        Simulator simulator = new Simulator(links, connections, numOfIterations, untilStability, scheduledEvents);
    }

}

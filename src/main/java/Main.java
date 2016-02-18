import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) throws Exception {
        // read file and parse into lines
        String[] inputLines = FileUtils.readFileToString(new File("src/main/java/input.txt")).split("\n");

        // look for numOfNodes
        List<String> configValues = Arrays.asList(inputLines[0].split(" "));
        int numOfNodesIndex = configValues.indexOf("-numOfNodes");
        if (numOfNodesIndex == -1) {
            // this program wants at least this parameter
            throw new Exception("Must specify number of nodes in the network");
        }
        Integer numOfNodes = Integer.valueOf(configValues.get(numOfNodesIndex + 1));

        // instantiate costs with default value -1 => not connected
        Integer[][] costs = new Integer[numOfNodes][numOfNodes];
        for (int i = 0; i < numOfNodes; i++) {
            for (int y = 0; y < numOfNodes; y++) {
                if (i == y) {
                    costs[i][y] = 0;
                } else {
                    costs[i][y] = -1;
                }
            }
        }

        // parse connections
        for (int i = 1; i < inputLines.length; i++) {
            String inputLine = inputLines[i];
            String[] inputLineValues = inputLine.split(" ");
            Integer fromNodeId = Integer.valueOf(inputLineValues[0]);
            Integer toNodeId = Integer.valueOf(inputLineValues[1]);
            Integer linkCost = Integer.valueOf(inputLineValues[2]);
            costs[fromNodeId][toNodeId] = linkCost;
            costs[toNodeId][fromNodeId] = linkCost;
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


        Simulator simulator = new Simulator(costs, numOfIterations, untilStability);
    }

}

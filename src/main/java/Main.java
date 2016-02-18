import org.apache.commons.io.FileUtils;

import javax.script.ScriptException;
import java.io.File;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException, ScriptException {
        System.out.println("Hello World!");

        String inputString = FileUtils.readFileToString(new File("src/main/java/input.txt"));
        String[] inputLines = inputString.split("\n");
        String configLine = inputLines[0];
        String[] configLineValues = configLine.split(" ");
        Integer numOfNodes = Integer.valueOf(configLineValues[0]);
        Integer numOfIterations = Integer.valueOf(configLineValues[1]);
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


        for (int i = 1; i < inputLines.length; i++) {
            String inputLine = inputLines[i];
            String[] inputLineValues = inputLine.split(" ");
            Integer fromNodeId = Integer.valueOf(inputLineValues[0]);
            Integer toNodeId = Integer.valueOf(inputLineValues[1]);
            Integer linkCost = Integer.valueOf(inputLineValues[2]);
            costs[fromNodeId][toNodeId] = linkCost;
            costs[toNodeId][fromNodeId] = linkCost;
        }


        Simulator simulator = new Simulator(costs, numOfIterations);


    }

}

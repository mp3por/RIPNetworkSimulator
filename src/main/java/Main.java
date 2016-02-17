import org.apache.commons.io.FileUtils;
import org.json.JSONObject;

import javax.script.ScriptException;
import java.io.File;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException, ScriptException {
        System.out.println("Hello World!");

        String inputString = FileUtils.readFileToString(new File("src/main/java/input.json"));
        JSONObject inputJSON = new JSONObject(inputString);

        Simulator simulator = new Simulator(inputJSON);
        System.out.println(simulator.toString());
    }
}

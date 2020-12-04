package marco;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class Main {

    public static int convertToInt(String num) {
        int n = 0;
        for(int i=num.length() -1; i>=0; i--) {
            n *= 10;
            n += num.charAt(i) - 48;
        }
        return n;
    }

    public static void main(String[] args) {
        try {
            String[] command = {
                    "/bin/sh",
                    "-c",
                    "curl https://www.puzzle-killer-sudoku.com/ | grep task | sed \"s/.*task = '//\" | sed \"s/'.*//\""
            };
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader bufferedReaderOutput = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String currentLine = "";
            String output = "";
            while ((currentLine = bufferedReaderOutput.readLine()) != null)
                output += currentLine + "\n";

            System.err.println(output);

            String[] outputs = output.split(";");

            String[] allRegionsString = outputs[1].split(",");
            int[] allRegions = new int[allRegionsString.length];
            for(int i=0; i<allRegions.length; i++) {
                allRegions[i] = convertToInt(allRegionsString[i]);
            }

            String[] sums = outputs[2].split("_|[a-z]");

            ArrayList<ArrayList<Cell>> regions = new ArrayList<ArrayList<Cell>>();  // like matrix
            for(int i=0; i< sums.length -1; i++) {
                regions.add(new ArrayList<Cell>());
            }

            int cont = 0;
            for(int i=1; i<5; i++) {
                for(int j=1; j<5; j++){
                    regions.get(allRegions[cont++] -1).add(new Cell(i, j));
                }
            }

            int i = 0;
            for(ArrayList<Cell> region : regions) {
                System.out.print("region " + (i+1) + ": sum = " + sums[i++] + " , size = " + region.size() + " , ");
                for(Cell cell : region) {
                    System.out.print(cell + " ");
                }
                System.out.println();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}

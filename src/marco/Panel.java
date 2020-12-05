package marco;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class Panel extends JPanel {

    static int n;    // number of rows
    static int m;    // number of columns

    static int[][] matrix;   // contains the element of the sudoku matrix
    static int[][] regions;  // contains the region of each element

    static String[] sumNumbers;   // contains the sum of each regions
    static String[] sumChars;     // contains the cell where the sum must be shown

    public Panel() {
        createDataFile(3,3,3);
        fillMatrix(solve());
    }

    public static void createDataFile(int N, int M, int difficulty) {
        try {
            n = N;
            m = M;

            String size = "";
            if(n == 2 && m == 2 && difficulty == 1) size = "";
            if(n == 2 && m == 2 && difficulty == 2) size = "?size=1";
            if(n == 2 && m == 2 && difficulty == 3) size = "?size=2";
            if(n == 2 && m == 3 && difficulty == 1) size = "?size=3";
            if(n == 2 && m == 3 && difficulty == 2) size = "?size=4";
            if(n == 2 && m == 3 && difficulty == 3) size = "?size=5";
            if(n == 3 && m == 3 && difficulty == 1) size = "?size=6";
            if(n == 3 && m == 3 && difficulty == 2) size = "?size=7";
            if(n == 3 && m == 3 && difficulty == 3) size = "?size=8";

            String[] command = {   // update for different request
                    "/bin/sh",
                    "-c",
                    "curl https://www.puzzle-killer-sudoku.com/" + size + " | grep task | sed \"s/.*task = '//\" | sed \"s/'.*//\""
            };

            Process process = Runtime.getRuntime().exec(command);
            BufferedReader bufferedReaderOutput = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String currentLine = "";
            String output = "";
            while ((currentLine = bufferedReaderOutput.readLine()) != null)
                output += currentLine + "\n";
            System.err.println(output);

            String[] outputs = output.split(";");

            regions = new int[n*m][n*m];
            String[] allRegionsString = outputs[1].split(",");
            int k=0;
            for(int i=0; i<n*m; i++) {
                for(int j=0; j<n*m; j++) {
                    regions[i][j] = convertToInt(allRegionsString[k++]);
                }
            }

            sumNumbers = outputs[2].split("_|[a-z]");
            sumChars = new String[sumNumbers.length];
            int cont = 0;
            for(int i=0; i<outputs[2].length(); i++) {
                if((outputs[2].charAt(i) == '_') || outputs[2].charAt(i) >= 'a' && outputs[2].charAt(i) <= 'z') {
                    sumChars[cont++] = "" + outputs[2].charAt(i);
                }
            }

            int row = 0;
            int col = 0;
            ArrayList<ArrayList<Cell>> regionsList = new ArrayList<ArrayList<Cell>>();
            for(int i=0; i< sumNumbers.length -1; i++) {
                regionsList.add(new ArrayList<Cell>());
            }
            for(int i=1; i<n*m+1; i++) {
                for(int j=1; j<n*m+1; j++){
                    regionsList.get(regions[row][col++]-1).add(new Cell(i, j));
                    if(col == n*m) {
                        col = 0;
                        row++;
                    }
                }
            }

            String file = "";
            file += "n = " + n + ";\n";
            file += "m = " + m + ";\n";
            file += "num_of_regions = " + (sumNumbers.length -1) + ";\n";

            int regions_max_size = 0;
            for(ArrayList<Cell> list : regionsList) {
                if(list.size() > regions_max_size) {
                    regions_max_size = list.size();
                }
            }
            file += "regions_max_size = " + regions_max_size + ";\n\n";
            file += "regions = [|";

            int i = 0;
            for(ArrayList<Cell> region : regionsList) {
                file += "\n" + sumNumbers[i++] + ", " + region.size() + ", ";
                for(Cell cell : region) {
                    file += cell.toString() + " ";
                }
                for(int j=region.size(); j<regions_max_size; j++) {
                    file += "0,0, ";
                }
                file += "|";
            }
            file += "];";

            FileWriter myWriter = new FileWriter("./minizinc/data.dzn");
            myWriter.write(file);
            myWriter.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void fillMatrix(String output) {
        matrix = new int[n*m][n*m];
        int k = 0;
        for(int i=0; i<n*m; i++) {
            for(int j=0; j<n*m; j++) {
                matrix[i][j] = convertToInt("" + output.charAt(k));
                k += 2;
            }
        }
    }

    public static String solve() {
        String output = "";
        try {
            Process process = Runtime.getRuntime().exec("minizinc ./minizinc/model.mzn ./minizinc/data.dzn");
            BufferedReader bufferedReaderOutput = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String currentLine;
            while ((currentLine = bufferedReaderOutput.readLine()) != null) {
                output += currentLine + " ";
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return output;
    }

    public static int convertToInt(String num) {
        int n = 0;
        for(int i=0; i<num.length(); i++) {
            n *= 10;
            n += num.charAt(i) - 48;
        }
        return n;
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);

        Graphics2D g2d = (Graphics2D) g;
        g2d.setFont(new Font("TimesRoman", Font.PLAIN, 30));

        g2d.setStroke(new BasicStroke(4));
        for(int i=0; i<=m; i++){    // bold orizzontal line
            g2d.drawLine(100, 100 + i*50*n, 100 + (n*m)*50, 100 + i*50*n);
        }
        for(int i=0; i<=n; i++) {   // blod vertical line
            g2d.drawLine(100 + i*50*m, 100, 100 + i*50*m, 100 + (n*m)*50);
        }

        g2d.setStroke(new BasicStroke(1));
        for(int i=0; i<m; i++){    // normal orizzontal line
            for(int j=1; j<n; j++)
                g2d.drawLine(100, 100 + (i*50*n)+(50*j), 100 + (n*m)*50, 100 + (i*50*n)+(50 * j));
        }
        for(int i=0; i<n; i++){    // normal vertical line
            for(int j=1; j<m; j++)
                g2d.drawLine(100 + (i*50*m)+(50*j),100, 100 + (i*50*m)+(50 * j), 100 + (n*m)*50);
        }

        for(int i=0; i<n*m; i++) {   // elements of the matrix
            for(int j=0; j<n*m; j++) {
                g2d.drawString("" + matrix[i][j], j*50 + 100 + 17, i*50 + 100 + 37);
            }
        }

        g2d.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{3}, 0));
        for(int i=0; i<n*m; i++) {
            for(int j=0; j<n*m; j++) {
                if((i == 0) || (regions[i][j] != regions[i-1][j]))   // margin up
                    g2d.drawLine(100 + j*50 + 5, 100 + i*50 + 5, 100 + j*50 + 45, 100 + i*50 + 5);
                if((i == n*m -1) || (regions[i][j] != regions[i+1][j]))  // margin dow
                    g2d.drawLine(100 + j*50 + 5, 100 + i*50 + 45, 100 + j*50 + 45, 100 + i*50 + 45);
                if((j == 0) || (regions[i][j] != regions[i][j-1]))  // margin left
                    g2d.drawLine(100 + j*50 + 5, 100 + i*50 + 5, 100 + j*50 + 5, 100 + i*50 + 45);
                if((j == n*m -1) || (regions[i][j] != regions[i][j+1])) // margin right
                    g2d.drawLine(100 + j*50 + 45, 100 + i*50 + 5, 100 + j*50 + 45, 100 + i*50 + 45);
            }
        }

        g2d.setFont(new Font("TimesRoman", Font.PLAIN, 10));
        int sum = 0;
        int i, j;
        for(int k=0; k<sumNumbers.length -1; k++) {
            i = sum / (n*m);
            j = sum % (n*m);
            g2d.drawString(sumNumbers[k], 100 + j*50 + 10, 100 + i*50 + 15);
            if(sumChars[k].charAt(0) == '_') sum += 1;
            else sum += sumChars[k].charAt(0) - 95;
        }

    }
}

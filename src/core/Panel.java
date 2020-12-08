package core;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;

public class Panel extends JPanel {

    static int n = 2;           // number of rows
    static int m = 2;           // number of columns
    static int difficulty = 1;  // difficulty of the sudoku

    static int[][] matrix;      // contains the element of the sudoku matrix
    static int[][] regions;     // contains the region of each element

    static String[] sumNumbers; // contains the sum of each regions
    static String[] sumChars;   // contains the cell where the sum must be shown

    static Cell focus;          // cell with light blue background
    static Cell[] clickable;    // the number on the right, with the sudoku must be filled

    Image light_blue;           // light blue background image
    String answer;              // the answer of the minizinc call

    public Panel() {
        initEventHandler();
        createDataFile();
    }

    public void initEventHandler() {
        focus = new Cell(this);
        clickable = new Cell[n*m];
        for(int i=0; i<n*m; i++) {
            clickable[i] = new Cell(i, n*m + 1, i+1);
        }
        light_blue = loadAssets("../resources/light_blue.png");
        answer = "";

        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(e.getX() > 100 + n*m*50 + 50 && e.getX() < 100 + n*m*50 + 100 && e.getY() > 100 && e.getY() < 100 + n*m*50) {  // one of the clickable
                    if(focus.isFocused()) {
                        matrix[focus.getI()][focus.getJ()] = ((e.getY() - 100) / 50) + 1;
                        answer = "";
                        focus.setFocused(false);
                    }
                }
                else if(e.getX() > 150 && e.getX() < 300 && e.getY() > 100 + m*n*50 && e.getY() < 100 + m*n*50 + 50) {  // solve button
                    if(solve().charAt(0) == 'G')
                        answer = "GOOD JOB!";
                    else
                        answer = "WRONG ANSWER!";
                    focus.setFocused(false);
                }
                else if(e.getX() < 100 || e.getX() > (100 + n*m*50) || e.getY() < 100 || e.getY() > (100 + n*m*50)) {  // out of the sudoku cells
                    answer = "";
                    focus.setFocused(false);
                }
                else {    // one of the sudoku cells
                    answer = "";
                    focus.setJ((e.getX() - 100) / 50);
                    focus.setI((e.getY() - 100) / 50);
                    matrix[focus.getI()][focus.getJ()] = 0;
                    focus.setFocused(true);
                }
            }
        });
    }

    public static void createDataFile() {
        try {
            matrix = new int[n*m][n*m];
            for(int i=0; i<n*m; i++) {
                for(int j=0; j<n*m; j++) {
                    matrix[i][j] = 0;
                }
            }

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

            String[] command = {
                    "/bin/sh",
                    "-c",
                    "curl https://www.puzzle-killer-sudoku.com/" + size + " | grep task | sed \"s/.*task = '//\" | sed \"s/'.*//\""
            };

            Process process = Runtime.getRuntime().exec(command);
            BufferedReader bufferedReaderOutput = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String currentLine;
            StringBuilder output = new StringBuilder();
            while ((currentLine = bufferedReaderOutput.readLine()) != null)
                output.append(currentLine).append("\n");
            System.err.println(output);

            String[] outputs = output.toString().split(";");

            regions = new int[n*m][n*m];
            String[] regionsString = outputs[1].split(",");
            int k=0;
            for(int i=0; i<n*m; i++) {
                for(int j=0; j<n*m; j++) {
                    regions[i][j] = convertToInt(regionsString[k++]);
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
            ArrayList<ArrayList<Cell>> regionsList = new ArrayList<>();
            for(int i=0; i< sumNumbers.length -1; i++) {
                regionsList.add(new ArrayList<>());
            }
            for(int i=1; i<=n*m; i++) {
                for(int j=1; j<=n*m; j++){
                    regionsList.get(regions[row][col++]-1).add(new Cell(i, j));
                    if(col == n*m) {
                        col = 0;
                        row++;
                    }
                }
            }

            StringBuilder file = new StringBuilder();
            file.append("n = ").append(n).append(";\n");
            file.append("m = ").append(m).append(";\n");
            file.append("num_of_regions = ").append(sumNumbers.length - 1).append(";\n");

            int regions_max_size = 0;
            for(ArrayList<Cell> list : regionsList) {
                if(list.size() > regions_max_size) {
                    regions_max_size = list.size();
                }
            }
            file.append("regions_max_size = ").append(regions_max_size).append(";\n\n");

            file.append("regions = [|");
            int i = 0;
            for(ArrayList<Cell> region : regionsList) {
                file.append("\n").append(sumNumbers[i++]).append(", ").append(region.size()).append(", ");
                for(Cell cell : region) {
                    file.append(cell.toString()).append(" ");
                }
                file.append("0,0, ".repeat(Math.max(0, regions_max_size - region.size())));
                file.append("|");
            }
            file.append("];");

            FileWriter writer = new FileWriter("./minizinc/data.dzn");
            writer.write(file.toString());
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String solve() {
        StringBuilder output = new StringBuilder();

        try {
            BufferedReader reader = new BufferedReader(new FileReader("./minizinc/data.dzn"));
            StringBuilder data = new StringBuilder();
            String currentLine;
            while ((currentLine = reader.readLine()) != null) {
                data.append(currentLine).append("\n");
            }

            data.append("\nmatrix = [|");
            for(int i=0; i<n*m; i++) {
                data.append("\n");
                for(int j=0; j<n*m; j++) {
                    data.append(matrix[i][j]).append(", ");
                }
                data.append("|");
            }
            data.append("];");

            FileWriter writer = new FileWriter("./minizinc/input.dzn");
            writer.write(data.toString());
            writer.close();

            Process process = Runtime.getRuntime().exec("minizinc ./minizinc/model.mzn ./minizinc/input.dzn");
            BufferedReader bufferedReaderOutput = new BufferedReader(new InputStreamReader(process.getInputStream()));

            while ((currentLine = bufferedReaderOutput.readLine()) != null) {
                output.append(currentLine).append(" ");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return output.toString();
    }

    public static int convertToInt(String num) {
        int n = 0;
        for(int i=0; i<num.length(); i++) {
            n *= 10;
            n += num.charAt(i) - 48;
        }
        return n;
    }

    public Image loadAssets(String path) {
        URL url = this.getClass().getResource(path);
        return Toolkit.getDefaultToolkit().getImage(url);
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D g2d = (Graphics2D) g;

        if(focus.isFocused()) {
            g2d.drawImage(light_blue, focus.getJ()*50 + 100, focus.getI()*50 + 100, 50, 50, this);
        }

        g2d.setFont(new Font("TimesRoman", Font.PLAIN, 30));
        g2d.setStroke(new BasicStroke(4));
        for(int i=0; i<=m; i++){    // bold horizontal line
            g2d.drawLine(100, 100 + i*50*n, 100 + (n*m)*50, 100 + i*50*n);
        }
        for(int i=0; i<=n; i++) {   // bold vertical line
            g2d.drawLine(100 + i*50*m, 100, 100 + i*50*m, 100 + (n*m)*50);
        }

        g2d.setStroke(new BasicStroke(1));
        for(int i=0; i<m; i++){    // normal horizontal line
            for(int j=1; j<n; j++)
                g2d.drawLine(100, 100 + (i*50*n)+(50*j), 100 + (n*m)*50, 100 + (i*50*n)+(50 * j));
        }
        for(int i=0; i<n; i++){    // normal vertical line
            for(int j=1; j<m; j++)
                g2d.drawLine(100 + (i*50*m)+(50*j),100, 100 + (i*50*m)+(50 * j), 100 + (n*m)*50);
        }

        for(int i=0; i<n*m; i++) {   // elements of the matrix
            for(int j=0; j<n*m; j++) {
                if(matrix[i][j] != 0){
                    g2d.drawString("" + matrix[i][j], j*50 + 100 + 17, i*50 + 100 + 37);
                }
            }
        }

        for(int i=0; i<n*m; i++) {
            g2d.drawString("" + clickable[i].getValue(), clickable[i].getJ()*50 + 100 + 17, clickable[i].getI()*50 + 100 + 37);
        }

        g2d.drawString("SOLVE", 150, 100 + n*m*50 + 50);
        g2d.drawString(answer, 300, 100 + n*m*50 + 50);

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
        for(int k=0; k<sumNumbers.length -1; k++) {     // sums of the regions
            i = sum / (n*m);
            j = sum % (n*m);
            g2d.drawString(sumNumbers[k], 100 + j*50 + 10, 100 + i*50 + 15);
            if(sumChars[k].charAt(0) == '_')
                sum += 1;
            else
                sum += sumChars[k].charAt(0) - 95;
        }
    }
}
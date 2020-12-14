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
    Image red;                  // red background image
    String answer;              // the answer of the minizinc call

    String state = "help";      // can be help, rules or game

    public Panel() {
        initEventHandler();
    }

    public void initEventHandler() {
        focus = new Cell(this);
        light_blue = loadAssets("../resources/light_blue.png");
        red = loadAssets("../resources/red.jpg");
        answer = " ";

        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(e.getY() < 60) {   // want to change size or difficulty
                    if(e.getX() < 700/9) {
                        n = 2;
                        m = 2;
                        difficulty = 1;
                    }
                    else if(e.getX() > 700/9 && e.getX() < 700/9*2) {
                        n = 2;
                        m = 2;
                        difficulty = 2;
                    }
                    else if(e.getX() > 700/9*2 && e.getX() < 700/9*3) {
                        n = 2;
                        m = 2;
                        difficulty = 3;
                    }
                    else if(e.getX() > 700/9*3 && e.getX() < 700/9*4) {
                        n = 2;
                        m = 3;
                        difficulty = 1;
                    }
                    else if(e.getX() > 700/9*4 && e.getX() < 700/9*5) {
                        n = 2;
                        m = 3;
                        difficulty = 2;
                    }
                    else if(e.getX() > 700/9*5 && e.getX() < 700/9*6) {
                        n = 2;
                        m = 3;
                        difficulty = 3;
                    }
                    else if(e.getX() > 700/9*6 && e.getX() < 700/9*7) {
                        n = 3;
                        m = 3;
                        difficulty = 1;
                    }
                    else if(e.getX() > 700/9*7 && e.getX() < 700/9*8) {
                        n = 3;
                        m = 3;
                        difficulty = 2;
                    }
                    else {
                        n = 3;
                        m = 3;
                        difficulty = 3;
                    }
                    createDataFile();
                    answer = "";
                    state = "game";
                    focus.setFocused(false);
                }
                else if(e.getX() < 120 && e.getY() > 620) {   // click on HELP
                    if(state.equals("help"))
                        state = "game";
                    else state = "help";
                    focus.setFocused(false);

                }
                else if(e.getX() > 580 && e.getY() > 620) {   // click on RULES
                    if(state.equals("rules"))
                        state = "game";
                    else state = "rules";
                    focus.setFocused(false);
                }
                else if(e.getX() > 100 + n*m*50 + 50 && e.getX() < 100 + n*m*50 + 100 && e.getY() > 100 && e.getY() < 100 + n*m*50) {  // one of the clickable
                    if(focus.isFocused()) {
                        matrix[focus.getI()][focus.getJ()] = ((e.getY() - 100) / 50) + 1;
                        answer = "";
                        focus.setFocused(false);
                    }
                }
                else if(e.getX() > 150 && e.getX() < 300 && e.getY() > 100 + m*n*50 && e.getY() < 100 + m*n*50 + 50) {  // solve button
                    String output = solve();
                    if(output.isEmpty()) {
                        answer = "INPUT NOT COMPLETE";
                    }
                    else if(output.charAt(0) == 'G') {
                        answer = "GOOD JOB!";
                    }
                    else {
                        answer = "WRONG ANSWER!";
                    }
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
            clickable = new Cell[n*m];
            matrix = new int[n*m][n*m];
            for(int i=0; i<n*m; i++) {
                clickable[i] = new Cell(i, n*m + 1, i+1);
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

    public static boolean verifyRow(int k) {
        for(int i=0; i<n*m; i++) {
            if(matrix[k][i] != 0) {
                for(int j=i+1; j<n*m; j++) {
                    if(matrix[k][i] == matrix[k][j]) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean verifyColumn(int k) {
        for(int i=0; i<n*m; i++) {
            if(matrix[i][k] != 0) {
                for(int j=i+1; j<n*m; j++) {
                    if(matrix[i][k] == matrix[j][k]) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean verifySector(int i, int j) {
        for(int k=0; k<n; k++) {
            for(int w=0; w<m; w++) {
                if(matrix[k+(i*n)][w+(j*m)] != 0) {
                    for(int a=k; a<n; a++) {
                        for(int s=0; s<m; s++) {
                            if(matrix[k+(i*n)][w+(j*m)] == matrix[a+(i*n)][s+(j*m)] && (k!=a || w!=s)) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    public static boolean verifyRegion(int region) {
        for(int i=0; i<n*m; i++) {
            for(int j=0; j<n*m; j++) {
                if(regions[i][j] == region && matrix[i][j] != 0) {
                    for(int k=i; k<n*m; k++) {
                        for(int w=0; w<n*m; w++) {
                            if(regions[k][w] == region && (i!=k || j!=w) && matrix[k][w] == matrix[i][j]) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    public static int getSumOfRegion(int region) {
        int sum = 0;
        for(int i=0; i<n*m; i++) {
            for(int j=0; j<n*m; j++) {
                if(regions[i][j] == region) {
                    if(matrix[i][j] == 0) {
                        return 0;    // region not complete
                    }
                    sum += matrix[i][j];
                }
            }
        }
        return sum;
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D g2d = (Graphics2D) g;

        g2d.setFont(new Font("TimesRoman", Font.PLAIN, 15));
        g2d.setStroke(new BasicStroke(3));
        g2d.drawLine(0, 60, 700, 60);
        for(int i=1; i<9; i++) {
            g2d.drawLine(700/9*i, 0, 700/9*i, 60);
        }
        g2d.drawString("2x2", 22, 25);
        g2d.drawString("EASY", 15, 45);
        g2d.drawString("2x2", 700/9 + 22, 25);
        g2d.drawString("MEDIUM", 700/9 + 4, 45);
        g2d.drawString("2x2", 700/9*2 + 22, 25);
        g2d.drawString("HARD", 700/9*2 + 13, 45);
        g2d.drawString("2x3", 700/9*3+ 22, 25);
        g2d.drawString("EASY", 700/9*3 + 15, 45);
        g2d.drawString("2x3", 700/9*4 + 22, 25);
        g2d.drawString("MEDIUM", 700/9*4 + 4, 45);
        g2d.drawString("2x3", 700/9*5 + 22, 25);
        g2d.drawString("HARD", 700/9*5 + 13, 45);
        g2d.drawString("3X3", 700/9*6 + 22, 25);
        g2d.drawString("EASY", 700/9*6 + 15, 45);
        g2d.drawString("3X3", 700/9*7 + 22, 25);
        g2d.drawString("MEDIUM", 700/9*7 + 4, 45);
        g2d.drawString("3X3", 700/9*8 + 22, 25);
        g2d.drawString("HARD", 700/9*8 + 13, 45);


        g2d.setFont(new Font("TimesRoman", Font.PLAIN, 30));
        g2d.drawString("HELP", 10, 650);
        g2d.drawString("RULES", 580, 650);

        if(state.equals("help")) {
            g2d.drawString("KILLER SUDOKU - HELP", 100, 150);
            g2d.setFont(new Font("TimesRoman", Font.PLAIN, 20));
            g2d.drawString("Select the size and the difficulty on the top menù.", 100, 250);
            g2d.drawString("To fill the cells, click the respective cell, then click the", 100, 300 );
            g2d.drawString("number on the right you want to put inside the cell.", 100, 330);
            g2d.drawString("To empty a cell just click it.", 100, 380);
            g2d.drawString("To check the solution click on SOLVE.", 100, 430);
            g2d.drawString("Click on HELP to show this window", 100, 480);
            g2d.drawString("Click on RULES the see the rules.", 100, 530);
            return;
        }
        else if(state.equals("rules")) {
            g2d.drawString("KILLER SUDOKU - RULES", 100, 150);
            g2d.setFont(new Font("TimesRoman", Font.PLAIN, 20));
            g2d.drawString("Killer Sudoku is a logic puzzle with simple rules", 100, 200);
            g2d.drawString("and challenging solutions.", 100, 230 );
            g2d.drawString("The rules of Killer Sudoku are simple:", 100, 280);
            g2d.drawString("1. The basic Sudoku rules apply.", 100, 330);
            g2d.drawString("2. The sum of all numbers in a cage must match", 100, 380);
            g2d.drawString("   the small number printed in its corner.", 100, 410);
            g2d.drawString("3. No number appears more than once in a cage.", 100, 460);
            g2d.drawString("Click on HELP to show the help window", 100, 510);
            g2d.drawString("Click on RULES the see this window.", 100, 560);
            return;
        }

        for(int i=0; i<n*m; i++) {
            if(verifyRow(i)) {   // draw the row in red if there are two element equals
                g2d.drawImage(red, 100, 100 + i*50, n*m*50, 50, this);
            }
            if(verifyColumn(i)) {   // draw the column red if there are two element equals
                g2d.drawImage(red, 100 + i*50, 100, 50, n*m*50, this);
            }
        }
        for(int i=0; i<m; i++) {
            for(int j=0; j<n; j++) {
                if(verifySector(i,j)) {   // draw the sector red if there are two element equals
                    g2d.drawImage(red, 100 + 50*m*j, 100 + 50*n*i, m*50, n*50, this);
                }
            }
        }
        for(int region=0; region< sumNumbers.length-1; region++) {
            // draw the the region red if there are two element equals in the same region or the region is full and the sum is different
            if(verifyRegion(region) || (getSumOfRegion(region) != 0 && getSumOfRegion(region) != convertToInt(sumNumbers[region-1]))) {
                for(int i=0; i<n*m; i++) {
                    for(int j=0; j<n*m; j++) {
                        if(regions[i][j] == region) {
                            g2d.drawImage(red, 100 + j*50, 100 + i*50, 50, 50, this);
                        }
                    }
                }
            }
        }

        if(focus.isFocused()) {   // draw light blu background in the cell where you click
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

        for(int i=0; i<n*m; i++) {   // elements to fill the sudoku
            g2d.drawString("" + clickable[i].getValue(), clickable[i].getJ()*50 + 100 + 17, clickable[i].getI()*50 + 100 + 37);
        }

        g2d.drawString("SOLVE", 150, 100 + n*m*50 + 50);
        g2d.drawString(answer, 300, 100 + n*m*50 + 50);

        g2d.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{3}, 0));
        for(int i=0; i<n*m; i++) {
            for(int j=0; j<n*m; j++) {
                if((i == 0) || (regions[i][j] != regions[i-1][j]))   // margin up region
                    g2d.drawLine(100 + j*50 + 5, 100 + i*50 + 5, 100 + j*50 + 45, 100 + i*50 + 5);
                if((i == n*m -1) || (regions[i][j] != regions[i+1][j]))  // margin down region
                    g2d.drawLine(100 + j*50 + 5, 100 + i*50 + 45, 100 + j*50 + 45, 100 + i*50 + 45);
                if((j == 0) || (regions[i][j] != regions[i][j-1]))  // margin left region
                    g2d.drawLine(100 + j*50 + 5, 100 + i*50 + 5, 100 + j*50 + 5, 100 + i*50 + 45);
                if((j == n*m -1) || (regions[i][j] != regions[i][j+1])) // margin right region
                    g2d.drawLine(100 + j*50 + 45, 100 + i*50 + 5, 100 + j*50 + 45, 100 + i*50 + 45);
            }
        }

        g2d.setFont(new Font("TimesRoman", Font.PLAIN, 10));
        int sum = 0;
        int row, col;
        for(int k=0; k<sumNumbers.length -1; k++) {     // sums of the regions
            row = sum / (n*m);
            col = sum % (n*m);
            g2d.drawString(sumNumbers[k], 100 + col*50 + 10, 100 + row*50 + 15);
            if(sumChars[k].charAt(0) == '_')
                sum += 1;
            else
                sum += sumChars[k].charAt(0) - 95;
        }

    }
}
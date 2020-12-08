package core;

import javax.swing.*;

public class Main {

    public static JFrame frame;

    public static void main(String[] args) {

        frame = new JFrame();
        frame.setSize(700,700);
        frame.setLocation(350, 0);
        frame.setContentPane(new Panel());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

    }
}

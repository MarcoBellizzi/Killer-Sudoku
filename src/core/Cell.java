package core;

import javax.swing.*;

public class Cell {

    JPanel panel;

    int i, j, value;

    boolean focused;

    public Cell() {}

    public Cell(JPanel panel) {
        focused = false;
        this.panel = panel;
    }

    public Cell(int i, int j) {
        this.i = i;
        this.j = j;
        this.focused = false;
    }

    public Cell(int i, int j, int value) {
        this.i = i;
        this.j = j;
        this.value = value;
    }

    public int getI() {
        return i;
    }

    public void setI(int i) {
        this.i = i;
    }

    public int getJ() {
        return j;
    }

    public void setJ(int j) {
        this.j = j;
    }

    public boolean isFocused() {
        return focused;
    }

    public void setFocused(boolean focused) {
        this.focused = focused;
        panel.repaint();
    }

    public JPanel getPanel() {
        return panel;
    }

    public void setPanel(JPanel panel) {
        this.panel = panel;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "" + i + "," + j + ",";
    }
}

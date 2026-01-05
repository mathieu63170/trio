package client;

import javax.swing.*;

/**
 * Main - Point d'entrée du client Trio
 */
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TrioGUI());
    }
}

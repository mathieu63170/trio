package client;

import javax.swing.*;


// Point d'entrée du client : lance l'interface graphique
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TrioGUI());
    }
}

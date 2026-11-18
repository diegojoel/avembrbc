package br.com.avembrbc;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public final class AvemRbc {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception ignored) {
            }
            Tema.aplicarFontes();
            new Janela().setVisible(true);
        });
    }

    private AvemRbc() {
    }
}

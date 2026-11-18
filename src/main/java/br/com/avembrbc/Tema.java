package br.com.avembrbc;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import javax.swing.UIManager;

public final class Tema {

    public static final Color VERDE = new Color(0x00783E);
    public static final Color AZUL = new Color(0x29428C);
    public static final Color VERDE_ESCURO = new Color(0x005529);
    public static final Color GELO = new Color(0xCDDEE9);
    public static final Color FUNDO = Color.WHITE;
    public static final Color PAGINA = new Color(0xF3F6FB);
    public static final Color GRAFITE = new Color(0x333333);
    public static final Color BORDA = new Color(0xD9D9D9);
    public static final Color TEXTO = new Color(0x1A1A1A);

    private Tema() {
    }

    public static void aplicarFontes() {
        Font fonte = new Font("Segoe UI", Font.PLAIN, 13);
        String[] chaves = {
                "Label.font", "Button.font", "TextField.font", "TextArea.font", "Table.font",
                "ComboBox.font", "CheckBox.font", "TabbedPane.font", "Menu.font", "MenuItem.font",
                "TitledBorder.font", "List.font"
        };
        for (String chave : chaves) {
            UIManager.put(chave, fonte);
        }
        UIManager.put("Table.selectionBackground", GELO);
        UIManager.put("Table.selectionForeground", TEXTO);
        UIManager.put("Panel.background", FUNDO);
    }

    public static Image icone(int tamanho) {
        BufferedImage imagem = new BufferedImage(tamanho, tamanho, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = imagem.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(VERDE);
        g.fillRoundRect(0, 0, tamanho, tamanho, tamanho / 4, tamanho / 4);
        g.setColor(Color.WHITE);
        int u = tamanho / 16;
        g.fillOval(3 * u, 7 * u, 8 * u, 6 * u);
        g.fillOval(9 * u, 4 * u, 5 * u, 5 * u);
        g.fillPolygon(new int[] {3 * u, u, 4 * u}, new int[] {9 * u, 12 * u, 11 * u}, 3);
        g.setColor(GELO);
        g.fillOval(10 * u, 2 * u, 2 * u, 3 * u);
        g.setColor(AZUL);
        g.fillPolygon(new int[] {13 * u, 15 * u, 13 * u}, new int[] {5 * u, 6 * u, 7 * u}, 3);
        g.fillOval(11 * u + u / 2, 5 * u + u / 2, Math.max(2, u), Math.max(2, u));
        g.setColor(Color.WHITE);
        g.fillPolygon(new int[] {8 * u, 9 * u, 7 * u}, new int[] {12 * u, 15 * u, 15 * u}, 3);
        g.dispose();
        return imagem;
    }

    public static Image menuComSeta(String titulo, String subtitulo) {
        Font tituloFonte = new Font("Segoe UI", Font.BOLD, 13);
        Font subFonte = new Font("Segoe UI", Font.ITALIC, 11);
        BufferedImage medida = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D mg = medida.createGraphics();
        mg.setFont(tituloFonte);
        int larguraTitulo = mg.getFontMetrics().stringWidth(titulo);
        mg.setFont(subFonte);
        int larguraSub = mg.getFontMetrics().stringWidth(subtitulo);
        mg.dispose();
        int largura = Math.max(larguraTitulo + 22, larguraSub) + 8;
        int altura = 36;
        BufferedImage imagem = new BufferedImage(largura, altura, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = imagem.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setFont(tituloFonte);
        int x = (largura - larguraTitulo - 18) / 2;
        g.setColor(GRAFITE);
        g.drawString(titulo, x, 15);
        int seta = x + larguraTitulo + 6;
        g.fillPolygon(new int[] {seta, seta + 11, seta + 5}, new int[] {6, 6, 14}, 3);
        g.setFont(subFonte);
        g.setColor(new Color(0x8AA0B8));
        g.drawString(subtitulo, (largura - larguraSub) / 2, 30);
        g.dispose();
        return imagem;
    }
}

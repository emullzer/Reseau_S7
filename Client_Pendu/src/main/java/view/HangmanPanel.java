package view;

import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

public class HangmanPanel extends JPanel {

    private static final int STAGES = 6;

    private final int maxAttempts;
    private int errors;

    public HangmanPanel(int maxAttempts) {
        this.maxAttempts = maxAttempts;
        setPreferredSize(new Dimension(300, 320));
        setBackground(Color.WHITE);
    }

    public void setErrors(int errors) {
        this.errors = Math.max(0, Math.min(errors, maxAttempts));
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setStroke(new BasicStroke(4));
        g2.setColor(Color.DARK_GRAY);

        g2.drawLine(40, 300, 120, 300);
        g2.drawLine(80, 300, 80, 30);
        g2.drawLine(80, 30, 190, 30);
        g2.drawLine(190, 30, 190, 60);

        int stage = (errors * STAGES + maxAttempts - 1) / maxAttempts;

        if (stage >= 1) g2.drawOval(165, 60, 50, 50);       // tête
        if (stage >= 2) g2.drawLine(190, 110, 190, 175);    //corps
        if (stage >= 3) g2.drawLine(190, 125, 155, 160);    // bras auche
        if (stage >= 4) g2.drawLine(190, 125, 225, 160);    // bras droit
        if (stage >= 5) g2.drawLine(190, 175, 160, 225);    // jambe gauche
        if (stage >= 6) g2.drawLine(190, 175, 220, 225);    // jambe droite
    }
}
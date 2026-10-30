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
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setStroke(new BasicStroke(4));
        g2.setColor(Color.DARK_GRAY);

        int w = getWidth();
        int h = getHeight();
        int baseY = h - 20;
        int postX = w / 4;
        int topY = 30;
        int ropeX = w / 2 + w / 8;
        int headR = Math.min(w, h) / 12;
        int headCenterY = topY + 30 + headR;
        int bodyTop = headCenterY + headR;
        int bodyBottom = bodyTop + h / 5;

        g2.drawLine(postX - 40, baseY, postX + 40, baseY);
        g2.drawLine(postX, baseY, postX, topY);
        g2.drawLine(postX, topY, ropeX, topY);
        g2.drawLine(ropeX, topY, ropeX, topY + 30);

        int stage = maxAttempts == 0 ? 0 : errors * STAGES / maxAttempts;
        if (errors > 0 && stage == 0) {
            stage = 1;
        }

        if (stage >= 1) {
            g2.drawOval(ropeX - headR, headCenterY - headR, 2 * headR, 2 * headR);
        }
        if (stage >= 2) {
            g2.drawLine(ropeX, bodyTop, ropeX, bodyBottom);
        }
        if (stage >= 3) {
            g2.drawLine(ropeX, bodyTop + 15, ropeX - 35, bodyTop + 50);
        }
        if (stage >= 4) {
            g2.drawLine(ropeX, bodyTop + 15, ropeX + 35, bodyTop + 50);
        }
        if (stage >= 5) {
            g2.drawLine(ropeX, bodyBottom, ropeX - 30, bodyBottom + 50);
        }
        if (stage >= 6) {
            g2.drawLine(ropeX, bodyBottom, ropeX + 30, bodyBottom + 50);
        }
        g2.dispose();
    }
}

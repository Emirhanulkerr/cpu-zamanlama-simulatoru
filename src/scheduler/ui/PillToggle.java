package scheduler.ui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.RoundRectangle2D;

import javax.swing.BorderFactory;
import javax.swing.JToggleButton;
import javax.swing.plaf.basic.BasicToggleButtonUI;

/** Koyu başlık çubuğundaki görünüm seçici (Simülasyon / Karşılaştırma) için hap biçimli düğme. */
final class PillToggle extends JToggleButton {

    PillToggle(String text) {
        super(text);
        setFont(Theme.font(Font.BOLD, 12.5f));
        setBorder(BorderFactory.createEmptyBorder(7, 16, 7, 16));
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setRolloverEnabled(true);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    @Override
    public void updateUI() {
        setUI(new BasicToggleButtonUI());
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        Theme.prepare(g);
        int width = getWidth();
        int height = getHeight();
        RoundRectangle2D shape = new RoundRectangle2D.Double(0, 0, width, height, height, height);

        if (isSelected()) {
            g.setColor(Color.WHITE);
            g.fill(shape);
        } else if (getModel().isRollover()) {
            g.setColor(Theme.withAlpha(Color.WHITE, 30));
            g.fill(shape);
        }

        g.setFont(getFont());
        FontMetrics metrics = g.getFontMetrics();
        g.setColor(isSelected() ? Theme.HEADER : Theme.withAlpha(Color.WHITE, 215));
        g.drawString(getText(),
                (width - metrics.stringWidth(getText())) / 2,
                (height - metrics.getHeight()) / 2 + metrics.getAscent());
        g.dispose();
    }
}

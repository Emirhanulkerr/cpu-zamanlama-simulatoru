package scheduler.ui;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.RoundRectangle2D;

import javax.swing.JComponent;

/** Tek bir özet ölçümü (etiket + büyük değer + birim) gösteren kutu. */
final class MetricTile extends JComponent {

    private final String label;
    private final String unit;
    private String value = "—";

    /**
     * @param label   ölçümün adı
     * @param unit    değerin yanında küçük yazılan birim; yoksa boş metin
     * @param tooltip ölçümün nasıl hesaplandığını anlatan ipucu
     */
    MetricTile(String label, String unit, String tooltip) {
        this.label = label;
        this.unit = unit;
        setOpaque(false);
        setToolTipText(tooltip);
    }

    void setValue(String value) {
        this.value = value;
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(120, 66);
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        Theme.prepare(g);
        RoundRectangle2D shape = new RoundRectangle2D.Double(0.5, 0.5, getWidth() - 1, getHeight() - 1, 14, 14);
        g.setColor(Theme.SURFACE);
        g.fill(shape);
        g.setColor(Theme.BORDER);
        g.draw(shape);

        int x = 14;
        g.setFont(Theme.font(Font.PLAIN, 11.5f));
        g.setColor(Theme.TEXT_SECONDARY);
        FontMetrics labelMetrics = g.getFontMetrics();
        g.drawString(Theme.ellipsize(label, labelMetrics, getWidth() - 2 * x), x, 22);

        g.setFont(Theme.font(Font.BOLD, 21f));
        g.setColor(Theme.TEXT);
        int baseline = getHeight() - 14;
        g.drawString(value, x, baseline);
        int valueWidth = g.getFontMetrics().stringWidth(value);

        if (!unit.isEmpty() && !value.equals("—")) {
            g.setFont(Theme.font(Font.PLAIN, 11f));
            g.setColor(Theme.TEXT_MUTED);
            int room = getWidth() - x - valueWidth - 5 - x;
            if (g.getFontMetrics().stringWidth(unit) <= room) {
                g.drawString(unit, x + valueWidth + 5, baseline);
            }
        }
        g.dispose();
    }
}

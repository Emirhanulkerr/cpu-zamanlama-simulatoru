package scheduler.ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Line2D;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

import scheduler.model.GanttSegment;

/** Gantt dilimlerini çizen ortak yardımcı; hem ana şema hem karşılaştırma görünümü kullanır. */
final class GanttPainter {

    /** Bitişik dilimler arasında bırakılan boşluk (piksel). */
    private static final double GAP = 2;

    private GanttPainter() {
    }

    /**
     * @param originX 0 anının x koordinatı
     * @param scale   bir zaman biriminin piksel genişliği
     * @param faded   {@code true} ise dilimler soluk çizilir (henüz oynatılmamış kısım)
     */
    static void paint(
            Graphics2D g, List<GanttSegment> segments, double originX, double scale, int y, int height, boolean faded) {
        g.setFont(Theme.font(Font.BOLD, 12f));
        FontMetrics metrics = g.getFontMetrics();
        int baseline = y + (height - metrics.getHeight()) / 2 + metrics.getAscent();

        for (GanttSegment segment : segments) {
            double x = originX + segment.start() * scale + GAP / 2;
            double width = Math.max(1, segment.duration() * scale - GAP);
            double arc = Math.min(8, width);
            Shape shape = new RoundRectangle2D.Double(x, y, width, height, arc, arc);

            String label;
            Color textColor;
            if (segment.isIdle()) {
                paintIdle(g, shape, x, y, width, height);
                label = "Boşta";
                textColor = Theme.TEXT_MUTED;
            } else {
                Color color = Theme.processColor(segment.process().id());
                g.setColor(faded ? Theme.lighten(color, 0.72) : color);
                g.fill(shape);
                label = segment.process().name();
                textColor = faded ? Theme.TEXT_MUTED : Theme.labelOn(color);
            }

            // Etiket yalnızca dilime rahatça sığıyorsa yazılır.
            int labelWidth = metrics.stringWidth(label);
            if (labelWidth + 6 <= width) {
                g.setColor(textColor);
                g.drawString(label, (float) (x + (width - labelWidth) / 2), baseline);
            }
        }
    }

    /** Boşta dilimi: açık gri zemin üzerine çapraz tarama. */
    private static void paintIdle(Graphics2D g, Shape shape, double x, int y, double width, int height) {
        g.setColor(Theme.IDLE);
        g.fill(shape);
        Shape oldClip = g.getClip();
        g.clip(shape);
        g.setColor(Theme.IDLE_HATCH);
        for (double offset = -height; offset < width; offset += 7) {
            g.draw(new Line2D.Double(x + offset, y + height, x + offset + height, y));
        }
        g.setClip(oldClip);
    }

    /**
     * Zaman ekseni için "yuvarlak" bir adım seçer (1, 2, 5, 10, 20, 50, ...);
     * ardışık iki etiket arasında en az {@code minPixels} piksel kalır.
     */
    static int niceStep(double scale, int minPixels) {
        for (int magnitude = 1; magnitude < 1_000_000; magnitude *= 10) {
            for (int base : new int[] {1, 2, 5}) {
                if (base * magnitude * scale >= minPixels) {
                    return base * magnitude;
                }
            }
        }
        return 1_000_000;
    }

    /** İpucu metni: "P2 · 4 → 10 (6 birim)". */
    static String describe(GanttSegment segment) {
        String who = segment.isIdle() ? "CPU boşta" : segment.process().name();
        return who + " · " + segment.start() + " → " + segment.end() + " (" + segment.duration() + " birim)";
    }
}

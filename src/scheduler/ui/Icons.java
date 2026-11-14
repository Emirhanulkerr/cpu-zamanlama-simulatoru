package scheduler.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

import javax.swing.Icon;

/**
 * Düğmelerde kullanılan, kodla çizilen simgeler. Yazı tipine bağlı olmadıkları için
 * her işletim sisteminde aynı görünürler.
 */
final class Icons {

    enum Shape { PLAY, PAUSE, STEP_BACK, STEP_FORWARD, TO_START, TO_END, PLUS, CHEVRON_DOWN }

    private Icons() {
    }

    static Icon of(Shape shape, Color color) {
        return new ShapeIcon(shape, color, 14);
    }

    /** Tablo hücrelerinde süreç adının yanında duran renk noktası. */
    static Icon dot(Color color) {
        return new DotIcon(color);
    }

    /** Başlıktaki uygulama simgesi: küçük bir Gantt şeması. */
    static Icon logo() {
        return new LogoIcon();
    }

    private record ShapeIcon(Shape shape, Color color, int size) implements Icon {

        @Override
        public void paintIcon(Component c, Graphics graphics, int x, int y) {
            Graphics2D g = (Graphics2D) graphics.create();
            Theme.prepare(g);
            g.translate(x, y);
            g.setColor(c.isEnabled() ? color : Theme.withAlpha(color, 90));
            double s = size;
            switch (shape) {
                case PLAY -> g.fill(triangle(s * 0.22, s * 0.10, s * 0.92, s * 0.90, true));
                case PAUSE -> {
                    g.fill(new RoundRectangle2D.Double(s * 0.18, s * 0.10, s * 0.24, s * 0.80, 2, 2));
                    g.fill(new RoundRectangle2D.Double(s * 0.58, s * 0.10, s * 0.24, s * 0.80, 2, 2));
                }
                case STEP_FORWARD -> {
                    g.fill(triangle(s * 0.12, s * 0.14, s * 0.66, s * 0.86, true));
                    g.fill(new Rectangle2D.Double(s * 0.72, s * 0.14, s * 0.16, s * 0.72));
                }
                case STEP_BACK -> {
                    g.fill(triangle(s * 0.34, s * 0.14, s * 0.88, s * 0.86, false));
                    g.fill(new Rectangle2D.Double(s * 0.12, s * 0.14, s * 0.16, s * 0.72));
                }
                case TO_END -> {
                    g.fill(triangle(s * 0.02, s * 0.16, s * 0.46, s * 0.84, true));
                    g.fill(triangle(s * 0.42, s * 0.16, s * 0.86, s * 0.84, true));
                    g.fill(new Rectangle2D.Double(s * 0.86, s * 0.16, s * 0.12, s * 0.68));
                }
                case TO_START -> {
                    g.fill(triangle(s * 0.54, s * 0.16, s * 0.98, s * 0.84, false));
                    g.fill(triangle(s * 0.14, s * 0.16, s * 0.58, s * 0.84, false));
                    g.fill(new Rectangle2D.Double(s * 0.02, s * 0.16, s * 0.12, s * 0.68));
                }
                case PLUS -> {
                    g.fill(new RoundRectangle2D.Double(s * 0.42, s * 0.12, s * 0.16, s * 0.76, 2, 2));
                    g.fill(new RoundRectangle2D.Double(s * 0.12, s * 0.42, s * 0.76, s * 0.16, 2, 2));
                }
                case CHEVRON_DOWN -> {
                    Path2D path = new Path2D.Double();
                    path.moveTo(s * 0.22, s * 0.38);
                    path.lineTo(s * 0.50, s * 0.66);
                    path.lineTo(s * 0.78, s * 0.38);
                    g.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g.draw(path);
                }
            }
            g.dispose();
        }

        /** Sola ya da sağa bakan üçgen. */
        private static Path2D triangle(double left, double top, double right, double bottom, boolean pointsRight) {
            Path2D path = new Path2D.Double();
            double middle = (top + bottom) / 2;
            if (pointsRight) {
                path.moveTo(left, top);
                path.lineTo(right, middle);
                path.lineTo(left, bottom);
            } else {
                path.moveTo(right, top);
                path.lineTo(left, middle);
                path.lineTo(right, bottom);
            }
            path.closePath();
            return path;
        }

        @Override
        public int getIconWidth() {
            return size;
        }

        @Override
        public int getIconHeight() {
            return size;
        }
    }

    private record DotIcon(Color color) implements Icon {

        @Override
        public void paintIcon(Component c, Graphics graphics, int x, int y) {
            Graphics2D g = (Graphics2D) graphics.create();
            Theme.prepare(g);
            g.setColor(color);
            g.fill(new Ellipse2D.Double(x, y, 10, 10));
            g.dispose();
        }

        @Override
        public int getIconWidth() {
            return 10;
        }

        @Override
        public int getIconHeight() {
            return 10;
        }
    }

    private static final class LogoIcon implements Icon {

        @Override
        public void paintIcon(Component c, Graphics graphics, int x, int y) {
            Graphics2D g = (Graphics2D) graphics.create();
            Theme.prepare(g);
            g.translate(x, y);
            g.setColor(Theme.withAlpha(Color.WHITE, 28));
            g.fill(new RoundRectangle2D.Double(0, 0, 36, 36, 10, 10));
            double[][] bars = {{7, 8, 14}, {13, 15, 16}, {9, 22, 11}};
            for (int i = 0; i < bars.length; i++) {
                g.setColor(Theme.lighten(Theme.processColor(i), 0.15));
                g.fill(new RoundRectangle2D.Double(bars[i][0], bars[i][1], bars[i][2], 5, 3, 3));
            }
            g.dispose();
        }

        @Override
        public int getIconWidth() {
            return 36;
        }

        @Override
        public int getIconHeight() {
            return 36;
        }
    }
}

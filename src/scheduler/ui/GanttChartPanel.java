package scheduler.ui;

import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.List;
import java.util.function.IntConsumer;

import javax.swing.JComponent;
import javax.swing.ToolTipManager;

import scheduler.model.GanttSegment;
import scheduler.model.SimulationResult;

/**
 * Ana Gantt şeması. Tüm çizelgeyi gösterir; oynatma konumuna kadar olan kısım canlı renkte,
 * sonrası soluk çizilir. Şemaya tıklayarak ya da sürükleyerek zamanda gezinilebilir.
 */
final class GanttChartPanel extends JComponent {

    private static final int PAD_LEFT = 14;
    private static final int PAD_RIGHT = 22;
    private static final int BAR_TOP = 24;
    private static final int BAR_HEIGHT = 42;
    private static final int AXIS_HEIGHT = 24;

    private SimulationResult result;
    private double playTime;
    private IntConsumer seekListener = time -> { };

    GanttChartPanel() {
        setOpaque(false);
        ToolTipManager.sharedInstance().registerComponent(this);
        MouseAdapter mouse = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                seek(e);
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                seek(e);
            }
        };
        addMouseListener(mouse);
        addMouseMotionListener(mouse);
    }

    void setResult(SimulationResult result) {
        this.result = result;
        repaint();
    }

    /** Oynatma konumu (kesirli olabilir); bu ana kadarki dilimler canlı renkte çizilir. */
    void setPlayTime(double playTime) {
        this.playTime = playTime;
        repaint();
    }

    /** Kullanıcı şema üzerinde bir zamana tıkladığında çağrılır. */
    void onSeek(IntConsumer listener) {
        this.seekListener = listener;
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(400, BAR_TOP + BAR_HEIGHT + AXIS_HEIGHT);
    }

    private double scale() {
        return (getWidth() - PAD_LEFT - PAD_RIGHT) / (double) Math.max(1, result.totalTime());
    }

    private void seek(MouseEvent e) {
        if (result == null) {
            return;
        }
        long time = Math.round((e.getX() - PAD_LEFT) / scale());
        seekListener.accept((int) Math.max(0, Math.min(result.totalTime(), time)));
    }

    @Override
    public String getToolTipText(MouseEvent e) {
        if (result == null || e.getY() < BAR_TOP || e.getY() > BAR_TOP + BAR_HEIGHT) {
            return null;
        }
        double time = (e.getX() - PAD_LEFT) / scale();
        for (GanttSegment segment : result.gantt()) {
            if (time >= segment.start() && time < segment.end()) {
                return GanttPainter.describe(segment);
            }
        }
        return null;
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        Theme.prepare(g);

        if (result == null) {
            g.setFont(Theme.font(Font.PLAIN, 13f));
            g.setColor(Theme.TEXT_MUTED);
            String message = "Gantt şemasını görmek için soldaki tabloya en az bir süreç ekleyin.";
            FontMetrics metrics = g.getFontMetrics();
            g.drawString(message, (getWidth() - metrics.stringWidth(message)) / 2, getHeight() / 2);
            g.dispose();
            return;
        }

        double scale = scale();
        List<GanttSegment> segments = result.gantt();
        double playX = PAD_LEFT + playTime * scale;

        // Önce tüm çizelge soluk; ardından oynatılan kısım canlı renkte üzerine çizilir.
        GanttPainter.paint(g, segments, PAD_LEFT, scale, BAR_TOP, BAR_HEIGHT, true);
        Shape oldClip = g.getClip();
        g.clip(new Rectangle2D.Double(0, 0, playX, getHeight()));
        GanttPainter.paint(g, segments, PAD_LEFT, scale, BAR_TOP, BAR_HEIGHT, false);
        g.setClip(oldClip);

        paintAxis(g, segments, scale);
        if (playTime < result.totalTime()) {
            paintPlayhead(g, playX);
        }
        g.dispose();
    }

    /** Dilim sınırlarına zaman etiketleri yazar; üst üste binecek etiketler atlanır. */
    private void paintAxis(Graphics2D g, List<GanttSegment> segments, double scale) {
        g.setFont(Theme.font(Font.PLAIN, 11f));
        FontMetrics metrics = g.getFontMetrics();
        int tickTop = BAR_TOP + BAR_HEIGHT + 3;
        int textY = tickTop + 6 + metrics.getAscent();

        int total = result.totalTime();
        String lastLabel = String.valueOf(total);
        double lastLabelLeft = PAD_LEFT + total * scale - metrics.stringWidth(lastLabel) / 2.0;
        double previousRight = Double.NEGATIVE_INFINITY;

        for (int i = 0; i <= segments.size(); i++) {
            int time = i == segments.size() ? total : segments.get(i).start();
            boolean isLast = i == segments.size();
            String label = String.valueOf(time);
            double x = PAD_LEFT + time * scale;
            double left = x - metrics.stringWidth(label) / 2.0;
            double right = left + metrics.stringWidth(label);

            g.setColor(Theme.AXIS);
            g.draw(new Line2D.Double(x, tickTop, x, tickTop + 4));
            boolean fits = left >= previousRight + 5 && (isLast || right + 5 <= lastLabelLeft);
            if (fits) {
                g.setColor(Theme.TEXT_SECONDARY);
                g.drawString(label, (float) left, textY);
                previousRight = right;
            }
        }
    }

    /** Oynatma konumunu gösteren dikey çizgi ve üzerindeki "t = 7" etiketi. */
    private void paintPlayhead(Graphics2D g, double x) {
        g.setColor(Theme.TEXT);
        g.setStroke(new BasicStroke(1.5f));
        g.draw(new Line2D.Double(x, BAR_TOP - 5, x, BAR_TOP + BAR_HEIGHT + 3));

        String label = "t = " + (int) Math.floor(playTime);
        g.setFont(Theme.font(Font.BOLD, 10.5f));
        FontMetrics metrics = g.getFontMetrics();
        double width = metrics.stringWidth(label) + 12;
        double left = Math.max(0, Math.min(getWidth() - width, x - width / 2));
        g.fill(new RoundRectangle2D.Double(left, 1, width, 17, 8, 8));
        g.setColor(Theme.SURFACE);
        g.drawString(label, (float) (left + 6), 1 + (17 - metrics.getHeight()) / 2f + metrics.getAscent());
    }
}

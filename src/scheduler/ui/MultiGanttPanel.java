package scheduler.ui;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Line2D;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.ToolTipManager;

import scheduler.model.GanttSegment;
import scheduler.model.SimProcess;
import scheduler.model.SimulationResult;

/**
 * Tüm algoritmaların Gantt şemalarını ortak bir zaman ekseninde alt alta gösterir;
 * böylece aynı süreç setinin farklı algoritmalarla nasıl sıralandığı tek bakışta karşılaştırılır.
 */
final class MultiGanttPanel extends JComponent {

    private static final int LABEL_WIDTH = 128;
    private static final int PAD_RIGHT = 20;
    private static final int LEGEND_ROW_HEIGHT = 22;
    private static final int LEGEND_BOTTOM_GAP = 10;
    private static final int BAR_HEIGHT = 28;
    private static final int ROW_GAP = 12;
    private static final int AXIS_HEIGHT = 24;
    private static final int SWATCH = 10;
    private static final int LEGEND_ITEM_GAP = 18;

    private List<SimulationResult> results = List.of();

    MultiGanttPanel() {
        setOpaque(false);
        setFont(Theme.font(Font.PLAIN, 11.5f));
        ToolTipManager.sharedInstance().registerComponent(this);
        // Açıklama satır sayısı genişliğe bağlıdır; genişlik değişince yükseklik yeniden hesaplanır.
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                revalidate();
            }
        });
    }

    void setResults(List<SimulationResult> results) {
        this.results = List.copyOf(results);
        revalidate();
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        if (results.isEmpty()) {
            return new Dimension(400, 60);
        }
        return new Dimension(400, rowsTop() + results.size() * (BAR_HEIGHT + ROW_GAP) - ROW_GAP + AXIS_HEIGHT);
    }

    private List<SimProcess> processes() {
        return results.get(0).processes();
    }

    /** Açıklamanın (süreç renkleri) kaç satır tuttuğunu mevcut genişliğe göre hesaplar. */
    private int legendRows() {
        FontMetrics metrics = getFontMetrics(getFont());
        int available = Math.max(200, getWidth() > 0 ? getWidth() : 800);
        int rows = 1;
        int x = 0;
        for (SimProcess p : processes()) {
            int itemWidth = SWATCH + 6 + metrics.stringWidth(p.name());
            if (x > 0 && x + itemWidth > available) {
                rows++;
                x = 0;
            }
            x += itemWidth + LEGEND_ITEM_GAP;
        }
        return rows;
    }

    private int rowsTop() {
        return legendRows() * LEGEND_ROW_HEIGHT + LEGEND_BOTTOM_GAP;
    }

    private int totalTime() {
        return results.stream().mapToInt(SimulationResult::totalTime).max().orElse(1);
    }

    private double scale() {
        return (getWidth() - LABEL_WIDTH - PAD_RIGHT) / (double) Math.max(1, totalTime());
    }

    @Override
    public String getToolTipText(MouseEvent e) {
        if (results.isEmpty() || e.getY() < rowsTop() || e.getX() < LABEL_WIDTH) {
            return null;
        }
        int offset = e.getY() - rowsTop();
        int row = offset / (BAR_HEIGHT + ROW_GAP);
        if (row >= results.size() || offset % (BAR_HEIGHT + ROW_GAP) > BAR_HEIGHT) {
            return null;
        }
        double time = (e.getX() - LABEL_WIDTH) / scale();
        SimulationResult result = results.get(row);
        for (GanttSegment segment : result.gantt()) {
            if (time >= segment.start() && time < segment.end()) {
                return result.algorithmShortName() + " — " + GanttPainter.describe(segment);
            }
        }
        return null;
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        Theme.prepare(g);
        if (results.isEmpty()) {
            g.setFont(Theme.font(Font.PLAIN, 13f));
            g.setColor(Theme.TEXT_MUTED);
            g.drawString("Karşılaştırma için soldaki tabloya en az bir süreç ekleyin.", 0, 24);
            g.dispose();
            return;
        }

        paintLegend(g);

        int top = rowsTop();
        int bottom = top + results.size() * (BAR_HEIGHT + ROW_GAP) - ROW_GAP;
        double scale = scale();
        int total = totalTime();

        // Zaman ekseni: yuvarlak adımlarda ince kılavuz çizgileri ve etiketler.
        g.setFont(Theme.font(Font.PLAIN, 11f));
        FontMetrics axisMetrics = g.getFontMetrics();
        int step = GanttPainter.niceStep(scale, 34);
        for (int time = 0; time <= total; time += step) {
            double x = LABEL_WIDTH + time * scale;
            g.setColor(Theme.GRID);
            g.draw(new Line2D.Double(x, top - 4, x, bottom + 4));
            g.setColor(Theme.TEXT_SECONDARY);
            String label = String.valueOf(time);
            g.drawString(label, (float) (x - axisMetrics.stringWidth(label) / 2.0),
                    bottom + 8 + axisMetrics.getAscent());
        }

        Font nameFont = Theme.font(Font.PLAIN, 12f);
        for (int i = 0; i < results.size(); i++) {
            SimulationResult result = results.get(i);
            int y = top + i * (BAR_HEIGHT + ROW_GAP);

            g.setFont(nameFont);
            FontMetrics nameMetrics = g.getFontMetrics();
            g.setColor(Theme.TEXT);
            g.drawString(Theme.ellipsize(result.algorithmShortName(), nameMetrics, LABEL_WIDTH - 10),
                    0, y + (BAR_HEIGHT - nameMetrics.getHeight()) / 2 + nameMetrics.getAscent());

            GanttPainter.paint(g, result.gantt(), LABEL_WIDTH, scale, y, BAR_HEIGHT, false);
        }
        g.dispose();
    }

    /** Süreç renklerinin açıklaması; dar dilimlerde ad yazılamadığı için her zaman gösterilir. */
    private void paintLegend(Graphics2D g) {
        g.setFont(getFont());
        FontMetrics metrics = g.getFontMetrics();
        int available = Math.max(200, getWidth());
        int x = 0;
        int row = 0;
        for (SimProcess p : processes()) {
            int itemWidth = SWATCH + 6 + metrics.stringWidth(p.name());
            if (x > 0 && x + itemWidth > available) {
                row++;
                x = 0;
            }
            int rowTop = row * LEGEND_ROW_HEIGHT;
            g.setColor(Theme.processColor(p.id()));
            g.fill(new RoundRectangle2D.Double(x, rowTop + (LEGEND_ROW_HEIGHT - SWATCH) / 2.0, SWATCH, SWATCH, 4, 4));
            g.setColor(Theme.TEXT_SECONDARY);
            g.drawString(p.name(), x + SWATCH + 6,
                    rowTop + (LEGEND_ROW_HEIGHT - metrics.getHeight()) / 2 + metrics.getAscent());
            x += itemWidth + LEGEND_ITEM_GAP;
        }
    }
}

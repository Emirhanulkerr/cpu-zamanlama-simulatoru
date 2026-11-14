package scheduler.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.function.ToDoubleFunction;

import javax.swing.JComponent;
import javax.swing.ToolTipManager;

import scheduler.model.SimulationResult;

/**
 * Tek bir ölçümü algoritmalar arasında karşılaştıran yatay çubuk grafik.
 * En iyi (en düşük) değerli çubuk koyu, diğerleri açık renkte çizilir.
 */
final class MetricBarChart extends JComponent {

    private static final int TITLE_HEIGHT = 26;
    private static final int ROW_HEIGHT = 24;
    private static final int BAR_HEIGHT = 14;
    private static final int VALUE_ROOM = 46;
    private static final Color OTHER_BAR = new Color(0x86B6EF);

    private record Bar(String label, double value) {
    }

    private final String title;
    private final ToDoubleFunction<SimulationResult> metric;
    private List<Bar> bars = List.of();

    MetricBarChart(String title, ToDoubleFunction<SimulationResult> metric) {
        this.title = title;
        this.metric = metric;
        setOpaque(false);
        ToolTipManager.sharedInstance().registerComponent(this);
    }

    void setResults(List<SimulationResult> results) {
        List<Bar> list = new ArrayList<>();
        for (SimulationResult r : results) {
            list.add(new Bar(r.algorithmShortName(), metric.applyAsDouble(r)));
        }
        bars = list;
        revalidate();
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(220, TITLE_HEIGHT + Math.max(1, bars.size()) * ROW_HEIGHT + 2);
    }

    @Override
    public String getToolTipText(MouseEvent e) {
        int row = (e.getY() - TITLE_HEIGHT) / ROW_HEIGHT;
        if (e.getY() < TITLE_HEIGHT || row >= bars.size()) {
            return null;
        }
        Bar bar = bars.get(row);
        return bar.label() + " · " + title.toLowerCase(Theme.TR) + ": " + Theme.number(bar.value()) + " birim";
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        Theme.prepare(g);

        g.setFont(Theme.font(Font.BOLD, 12f));
        g.setColor(Theme.TEXT);
        g.drawString(title, 0, 13);
        if (bars.isEmpty()) {
            g.dispose();
            return;
        }

        Font labelFont = Theme.font(Font.PLAIN, 11.5f);
        Font bestFont = Theme.font(Font.BOLD, 11.5f);
        FontMetrics metrics = g.getFontMetrics(labelFont);

        int labelWidth = 0;
        double max = 0;
        double best = Double.MAX_VALUE;
        for (Bar bar : bars) {
            labelWidth = Math.max(labelWidth, metrics.stringWidth(bar.label()));
            max = Math.max(max, bar.value());
            best = Math.min(best, bar.value());
        }
        labelWidth = Math.min(labelWidth, getWidth() / 2);
        int axisX = labelWidth + 10;
        int maxBarWidth = Math.max(0, getWidth() - axisX - VALUE_ROOM);

        for (int i = 0; i < bars.size(); i++) {
            Bar bar = bars.get(i);
            boolean isBest = Math.abs(bar.value() - best) < 1e-9;
            int rowTop = TITLE_HEIGHT + i * ROW_HEIGHT;
            int textY = rowTop + (ROW_HEIGHT - metrics.getHeight()) / 2 + metrics.getAscent();

            g.setFont(labelFont);
            g.setColor(Theme.TEXT_SECONDARY);
            String label = Theme.ellipsize(bar.label(), metrics, labelWidth);
            g.drawString(label, labelWidth - metrics.stringWidth(label), textY);

            // Çubuk taban çizgisinde düz, veri ucunda yuvarlaktır.
            double width = max == 0 ? 0 : bar.value() / max * maxBarWidth;
            double barY = rowTop + (ROW_HEIGHT - BAR_HEIGHT) / 2.0;
            if (width > 0) {
                g.setColor(isBest ? Theme.ACCENT : OTHER_BAR);
                g.fill(new RoundRectangle2D.Double(axisX, barY, width, BAR_HEIGHT, 8, 8));
                g.fill(new Rectangle2D.Double(axisX, barY, Math.min(width, 4), BAR_HEIGHT));
            }

            g.setFont(isBest ? bestFont : labelFont);
            g.setColor(isBest ? Theme.TEXT : Theme.TEXT_SECONDARY);
            g.drawString(Theme.number(bar.value()), (float) (axisX + width + 6), textY);
        }

        g.setColor(Theme.AXIS);
        g.draw(new Line2D.Double(axisX, TITLE_HEIGHT + 1, axisX, TITLE_HEIGHT + bars.size() * ROW_HEIGHT - 1));
        g.dispose();
    }
}

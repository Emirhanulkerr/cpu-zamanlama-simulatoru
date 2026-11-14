package scheduler.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.Scrollable;

import scheduler.model.SimulationResult;

/**
 * Karşılaştırma görünümü: aynı süreç seti tüm algoritmalarla çalıştırılır;
 * özet tablo, ortalama sürelerin çubuk grafikleri ve Gantt şemaları alt alta gösterilir.
 */
final class ComparisonPanel extends JPanel {

    private final ComparisonTableModel tableModel = new ComparisonTableModel();
    private final MetricBarChart waitingChart =
            new MetricBarChart("Ortalama bekleme süresi", SimulationResult::averageWaitingTime);
    private final MetricBarChart turnaroundChart =
            new MetricBarChart("Ortalama dönüş süresi", SimulationResult::averageTurnaroundTime);
    private final MetricBarChart responseChart =
            new MetricBarChart("Ortalama yanıt süresi", SimulationResult::averageResponseTime);
    private final MultiGanttPanel ganttPanel = new MultiGanttPanel();

    ComparisonPanel() {
        super(new BorderLayout());
        setOpaque(false);

        JPanel charts = new JPanel(new GridLayout(1, 3, 22, 0));
        charts.setOpaque(false);
        charts.add(waitingChart);
        charts.add(turnaroundChart);
        charts.add(responseChart);

        ScrollablePanel content = new ScrollablePanel();
        content.add(new Card("Algoritma Karşılaştırması")
                .headerRight(note("Vurgulu hücreler o ölçütteki en iyi değerdir"))
                .content(buildTable()));
        content.add(new Card("Ortalama Süreler")
                .headerRight(note("Düşük değer daha iyidir · koyu çubuk en iyi değer"))
                .content(charts));
        content.add(new Card("Gantt Şemaları")
                .headerRight(note("Ortak zaman ekseni"))
                .content(ganttPanel));

        JScrollPane scrollPane = new JScrollPane(content,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(18);
        add(scrollPane, BorderLayout.CENTER);
    }

    void showResults(List<SimulationResult> results) {
        tableModel.setResults(results);
        waitingChart.setResults(results);
        turnaroundChart.setResults(results);
        responseChart.setResults(results);
        ganttPanel.setResults(results);
    }

    private JPanel buildTable() {
        JTable table = new JTable(tableModel);
        Tables.style(table, ComparisonTableModel.TOOLTIPS);
        table.setRowSelectionAllowed(false);
        table.setFocusable(false);
        table.setFillsViewportHeight(false);
        table.getColumnModel().getColumn(0).setPreferredWidth(240);

        Font bold = Theme.font(Font.BOLD, 13f);
        Tables.CellRenderer renderer = new Tables.CellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, column);
                if (tableModel.isBest(row, column)) {
                    setFont(bold);
                    setBackground(Theme.ACCENT_SOFT);
                }
                return this;
            }
        };
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }

        // Tablo kaydırma bölmesinde olmadığı için başlığı elle üstüne yerleştirilir.
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createLineBorder(Theme.GRID));
        panel.add(table.getTableHeader(), BorderLayout.NORTH);
        panel.add(table, BorderLayout.CENTER);
        return panel;
    }

    private static JLabel note(String text) {
        JLabel label = new JLabel(text);
        label.setFont(Theme.font(Font.PLAIN, 11.5f));
        label.setForeground(Theme.TEXT_MUTED);
        return label;
    }

    /** Kaydırma bölmesinde genişliği görünüm alanına uyan, yalnızca dikeyde kayan içerik paneli. */
    private static final class ScrollablePanel extends JPanel implements Scrollable {

        ScrollablePanel() {
            super(new VStackLayout(10));
            setOpaque(false);
        }

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 18;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            return Math.max(18, visibleRect.height - 36);
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }
}

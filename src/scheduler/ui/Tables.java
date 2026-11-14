package scheduler.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.util.function.IntFunction;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

/** Tabloların ortak görünümü: sade başlık, ince satır çizgileri, ortalanmış sayılar. */
final class Tables {

    static final int ROW_HEIGHT = 28;

    private Tables() {
    }

    /**
     * @param headerTooltips sütun başlıklarının ipuçları; ipucu istenmiyorsa {@code null}
     */
    static void style(JTable table, String[] headerTooltips) {
        table.setRowHeight(ROW_HEIGHT);
        table.setFont(Theme.font(Font.PLAIN, 13f));
        table.setForeground(Theme.TEXT);
        table.setBackground(Theme.SURFACE);
        table.setShowGrid(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(Theme.GRID);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setFillsViewportHeight(true);
        table.setSelectionBackground(Theme.ACCENT_SOFT);
        table.setSelectionForeground(Theme.TEXT);

        JTableHeader header = table.getTableHeader();
        header.setReorderingAllowed(false);
        header.setResizingAllowed(false);
        header.setDefaultRenderer(new HeaderRenderer(headerTooltips));

        CellRenderer renderer = new CellRenderer();
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }
    }

    static JScrollPane scroll(JTable table) {
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(Theme.GRID));
        scrollPane.getViewport().setBackground(Theme.SURFACE);
        return scrollPane;
    }

    /** Varsayılan hücre çizimi: ilk sütun sola dayalı, diğerleri ortalanmış. */
    static class CellRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, false, row, column);
            setHorizontalAlignment(column == 0 ? SwingConstants.LEFT : SwingConstants.CENTER);
            setBorder(BorderFactory.createEmptyBorder(0, column == 0 ? 12 : 4, 0, 4));
            setFont(table.getFont());
            setIcon(null);
            if (!isSelected) {
                setBackground(table.getBackground());
                setForeground(Theme.TEXT);
            }
            return this;
        }
    }

    /** Süreç adı sütunu: adın soluna sürecin rengini taşıyan bir nokta koyar. */
    static class ProcessNameRenderer extends CellRenderer {

        private final IntFunction<Color> colorOfRow;

        /** @param colorOfRow satırın süreç rengi; nokta çizilmeyecek satırlar için {@code null} */
        ProcessNameRenderer(IntFunction<Color> colorOfRow) {
            this.colorOfRow = colorOfRow;
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            Color color = colorOfRow.apply(row);
            setIcon(color == null ? null : Icons.dot(color));
            setIconTextGap(8);
            return this;
        }
    }

    private static final class HeaderRenderer extends DefaultTableCellRenderer {

        private final String[] tooltips;

        HeaderRenderer(String[] tooltips) {
            this.tooltips = tooltips;
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, false, false, row, column);
            label.setFont(Theme.font(Font.BOLD, 11.5f));
            label.setForeground(Theme.TEXT_SECONDARY);
            label.setBackground(Theme.SURFACE_ALT);
            label.setOpaque(true);
            label.setHorizontalAlignment(column == 0 ? SwingConstants.LEFT : SwingConstants.CENTER);
            label.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.GRID),
                    BorderFactory.createEmptyBorder(8, column == 0 ? 12 : 4, 8, 4)));
            int modelColumn = table.convertColumnIndexToModel(column);
            label.setToolTipText(tooltips != null && modelColumn < tooltips.length ? tooltips[modelColumn] : null);
            return label;
        }
    }
}

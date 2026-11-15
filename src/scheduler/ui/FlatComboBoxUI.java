package scheduler.ui;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.RoundRectangle2D;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.BasicPopupMenuUI;
import javax.swing.plaf.basic.ComboPopup;

/** Temaya uyan, düz görünümlü açılır liste. İşletim sisteminin görünümünden bağımsızdır. */
final class FlatComboBoxUI extends BasicComboBoxUI {

    private static final int TEXT_PADDING = 10;

    static void install(JComboBox<?> comboBox) {
        comboBox.setUI(new FlatComboBoxUI());
        comboBox.setOpaque(false);
        comboBox.setBackground(Theme.CONTROL);
        comboBox.setForeground(Theme.TEXT);
        comboBox.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 4));
        comboBox.setRenderer(new ItemRenderer());
        comboBox.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    @Override
    public void paint(Graphics graphics, JComponent c) {
        Graphics2D g = (Graphics2D) graphics.create();
        Theme.prepare(g);
        RoundRectangle2D shape = new RoundRectangle2D.Double(0.5, 0.5, c.getWidth() - 1, c.getHeight() - 1, 9, 9);
        g.setColor(Theme.CONTROL);
        g.fill(shape);
        g.setColor(comboBox.hasFocus() ? Theme.withAlpha(Theme.ACCENT, 170) : Theme.BORDER);
        g.draw(shape);

        Object selected = comboBox.getSelectedItem();
        if (selected != null) {
            Rectangle area = rectangleForCurrentValue();
            g.setFont(comboBox.getFont());
            FontMetrics metrics = g.getFontMetrics();
            g.setColor(comboBox.isEnabled() ? Theme.TEXT : Theme.TEXT_MUTED);
            String text = Theme.ellipsize(selected.toString(), metrics, area.width - TEXT_PADDING - 2);
            g.drawString(text, area.x + TEXT_PADDING, area.y + (area.height - metrics.getHeight()) / 2 + metrics.getAscent());
        }
        g.dispose();
    }

    @Override
    protected JButton createArrowButton() {
        JButton button = new JButton(Icons.of(Icons.Shape.CHEVRON_DOWN, Theme.TEXT_SECONDARY)) {
            @Override
            public void updateUI() {
                setUI(new BasicButtonUI());
            }
        };
        button.setBorder(BorderFactory.createEmptyBorder());
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setFocusable(false);
        button.setOpaque(false);
        return button;
    }

    @Override
    protected ComboPopup createPopup() {
        return new BasicComboPopup(comboBox) {
            @Override
            public void updateUI() {
                setUI(new BasicPopupMenuUI());
            }

            @Override
            protected void configurePopup() {
                super.configurePopup();
                setBorder(BorderFactory.createLineBorder(Theme.BORDER));
                setBackground(Theme.CONTROL);
            }

            @Override
            protected void configureList() {
                super.configureList();
                list.setBackground(Theme.CONTROL);
                list.setForeground(Theme.TEXT);
                list.setSelectionBackground(Theme.ACCENT_SOFT);
                list.setSelectionForeground(Theme.TEXT);
            }

            @Override
            protected void configureScroller() {
                super.configureScroller();
                scroller.setBorder(BorderFactory.createEmptyBorder());
                scroller.getViewport().setBackground(Theme.CONTROL);
                FlatScrollBarUI.install(scroller);
            }
        };
    }

    /** Açılan listedeki satırların çizimi. */
    private static final class ItemRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(
                JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, false);
            setBorder(BorderFactory.createEmptyBorder(6, TEXT_PADDING, 6, TEXT_PADDING));
            setBackground(isSelected ? Theme.ACCENT_SOFT : Theme.CONTROL);
            setForeground(Theme.TEXT);
            return this;
        }
    }
}

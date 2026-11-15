package scheduler.ui;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.RoundRectangle2D;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.plaf.basic.BasicScrollBarUI;

/** Temaya uyan ince kaydırma çubuğu: ok düğmeleri yok, yalnızca yuvarlak köşeli bir tutamak. */
final class FlatScrollBarUI extends BasicScrollBarUI {

    private static final int THICKNESS = 10;

    /** Kaydırma bölmesinin iki çubuğuna da bu görünümü uygular. */
    static void install(JScrollPane scrollPane) {
        for (JScrollBar bar : new JScrollBar[] {scrollPane.getVerticalScrollBar(), scrollPane.getHorizontalScrollBar()}) {
            if (bar == null) {
                continue;  // açılır listelerin kaydırma bölmesinde yatay çubuk bulunmaz
            }
            bar.setUI(new FlatScrollBarUI());
            bar.setOpaque(false);
            bar.setUnitIncrement(18);
        }
    }

    @Override
    public Dimension getPreferredSize(JComponent c) {
        return scrollbar.getOrientation() == JScrollBar.VERTICAL
                ? new Dimension(THICKNESS, 48)
                : new Dimension(48, THICKNESS);
    }

    @Override
    protected Dimension getMinimumThumbSize() {
        return new Dimension(24, 24);
    }

    @Override
    protected Dimension getMaximumThumbSize() {
        return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    @Override
    protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
        // Yol çizilmez; çubuk arkasındaki zeminle bütünleşir.
    }

    @Override
    protected void paintThumb(Graphics graphics, JComponent c, Rectangle thumb) {
        if (thumb.isEmpty() || !scrollbar.isEnabled()) {
            return;
        }
        Graphics2D g = (Graphics2D) graphics.create();
        Theme.prepare(g);
        g.setColor(isDragging || isThumbRollover() ? Theme.TEXT_MUTED : Theme.AXIS);
        g.fill(new RoundRectangle2D.Double(thumb.x + 2, thumb.y + 2, thumb.width - 4, thumb.height - 4, 6, 6));
        g.dispose();
    }

    @Override
    protected JButton createDecreaseButton(int orientation) {
        return invisibleButton();
    }

    @Override
    protected JButton createIncreaseButton(int orientation) {
        return invisibleButton();
    }

    private static JButton invisibleButton() {
        JButton button = new JButton();
        Dimension zero = new Dimension(0, 0);
        button.setPreferredSize(zero);
        button.setMinimumSize(zero);
        button.setMaximumSize(zero);
        button.setFocusable(false);
        return button;
    }
}

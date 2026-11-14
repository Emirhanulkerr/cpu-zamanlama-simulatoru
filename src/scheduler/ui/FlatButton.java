package scheduler.ui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.RoundRectangle2D;

import javax.swing.BorderFactory;
import javax.swing.ButtonModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.plaf.basic.BasicButtonUI;

/**
 * Düz görünümlü, yuvarlak köşeli düğme. Tümüyle kodla çizildiği için
 * işletim sisteminin görünümünden bağımsızdır.
 */
final class FlatButton extends JButton {

    enum Kind {
        /** Ana eylem: dolgulu, vurgu renginde. */
        PRIMARY,
        /** İkincil eylem: beyaz zemin, ince çerçeve. */
        SECONDARY,
        /** Yalnızca simge taşıyan, çerçevesiz düğme. */
        GHOST
    }

    private static final int RADIUS = 9;
    private static final int ICON_GAP = 7;

    private final Kind kind;
    private boolean iconTrailing;

    FlatButton(String text, Kind kind) {
        this(text, null, kind);
    }

    FlatButton(String text, Icon icon, Kind kind) {
        super(text, icon);
        this.kind = kind;
        setFont(Theme.font(kind == Kind.PRIMARY ? Font.BOLD : Font.PLAIN, 12.5f));
        setForeground(kind == Kind.PRIMARY ? Color.WHITE : Theme.TEXT);
        setBorder(BorderFactory.createEmptyBorder(7, 12, 7, 12));
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setRolloverEnabled(true);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    /** Simgeyi metnin soluna değil sağına yerleştirir (ör. açılır menü oku). */
    FlatButton iconTrailing() {
        iconTrailing = true;
        return this;
    }

    @Override
    public void updateUI() {
        setUI(new BasicButtonUI());
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        Theme.prepare(g);
        int width = getWidth();
        int height = getHeight();
        ButtonModel model = getModel();
        boolean pressed = model.isArmed() && model.isPressed();
        boolean hover = model.isRollover() && isEnabled();

        RoundRectangle2D shape = new RoundRectangle2D.Double(0.5, 0.5, width - 1, height - 1, RADIUS, RADIUS);
        switch (kind) {
            case PRIMARY -> {
                Color fill = pressed ? Theme.ACCENT_PRESSED : hover ? Theme.ACCENT_HOVER : Theme.ACCENT;
                g.setColor(isEnabled() ? fill : Theme.lighten(Theme.ACCENT, 0.55));
                g.fill(shape);
            }
            case SECONDARY -> {
                g.setColor(pressed ? Theme.GRID : hover ? Theme.SURFACE_ALT : Theme.SURFACE);
                g.fill(shape);
                g.setColor(Theme.BORDER);
                g.draw(shape);
            }
            case GHOST -> {
                if (pressed || hover) {
                    g.setColor(pressed ? Theme.GRID : Theme.SURFACE_ALT);
                    g.fill(shape);
                }
            }
        }
        if (isFocusOwner() && kind != Kind.PRIMARY) {
            g.setColor(Theme.withAlpha(Theme.ACCENT, 150));
            g.draw(shape);
        }

        // Simge ve metin birlikte ortalanır.
        Icon icon = getIcon();
        String text = getText() == null ? "" : getText();
        g.setFont(getFont());
        FontMetrics metrics = g.getFontMetrics();
        int textWidth = metrics.stringWidth(text);
        int iconWidth = icon == null ? 0 : icon.getIconWidth();
        int gap = icon != null && !text.isEmpty() ? ICON_GAP : 0;
        int x = (width - textWidth - iconWidth - gap) / 2;
        int textX = iconTrailing ? x : x + iconWidth + gap;
        int iconX = iconTrailing ? x + textWidth + gap : x;

        if (icon != null) {
            icon.paintIcon(this, g, iconX, (height - icon.getIconHeight()) / 2);
        }
        if (!text.isEmpty()) {
            g.setColor(isEnabled() ? getForeground() : Theme.withAlpha(getForeground(), 110));
            g.drawString(text, textX, (height - metrics.getHeight()) / 2 + metrics.getAscent());
        }
        g.dispose();
    }
}

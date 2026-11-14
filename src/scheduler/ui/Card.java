package scheduler.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.RoundRectangle2D;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

/** Beyaz, yuvarlak köşeli içerik kartı; isteğe bağlı başlık ve başlığın sağında ek bileşen taşır. */
final class Card extends JPanel {

    private static final int RADIUS = 14;

    private JPanel header;

    Card() {
        this(null);
    }

    Card(String title) {
        super(new BorderLayout(0, 10));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
        if (title != null) {
            JLabel label = new JLabel(title);
            label.setFont(Theme.font(Font.BOLD, 13.5f));
            label.setForeground(Theme.TEXT);
            header = new JPanel(new BorderLayout(12, 0));
            header.setOpaque(false);
            header.add(label, BorderLayout.WEST);
            add(header, BorderLayout.NORTH);
        }
    }

    /** Başlık satırının sağına bir bileşen (ör. açıklama etiketi) yerleştirir. */
    Card headerRight(JComponent component) {
        header.add(component, BorderLayout.EAST);
        return this;
    }

    Card content(Component component) {
        add(component, BorderLayout.CENTER);
        return this;
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        Theme.prepare(g);
        RoundRectangle2D shape =
                new RoundRectangle2D.Double(0.5, 0.5, getWidth() - 1, getHeight() - 1, RADIUS, RADIUS);
        g.setColor(Theme.SURFACE);
        g.fill(shape);
        g.setColor(Theme.BORDER);
        g.draw(shape);
        g.dispose();
    }
}

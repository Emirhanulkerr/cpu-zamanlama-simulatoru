package scheduler.ui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager2;

/**
 * Bileşenleri yukarıdan aşağıya, tam genişlikte dizen yerleşim yöneticisi.
 * Her bileşen tercih ettiği yüksekliği alır; {@link #FILL} kısıtıyla eklenen
 * tek bir bileşen ise kalan tüm yüksekliği doldurur.
 */
final class VStackLayout implements LayoutManager2 {

    static final String FILL = "fill";

    private final int gap;
    private Component fill;

    VStackLayout(int gap) {
        this.gap = gap;
    }

    @Override
    public void addLayoutComponent(Component component, Object constraints) {
        if (FILL.equals(constraints)) {
            fill = component;
        }
    }

    @Override
    public void addLayoutComponent(String name, Component component) {
        addLayoutComponent(component, name);
    }

    @Override
    public void removeLayoutComponent(Component component) {
        if (component == fill) {
            fill = null;
        }
    }

    @Override
    public Dimension preferredLayoutSize(Container parent) {
        return size(parent, true);
    }

    @Override
    public Dimension minimumLayoutSize(Container parent) {
        return size(parent, false);
    }

    @Override
    public Dimension maximumLayoutSize(Container target) {
        return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    private Dimension size(Container parent, boolean preferred) {
        Insets insets = parent.getInsets();
        int width = 0;
        int height = 0;
        int visible = 0;
        for (Component c : parent.getComponents()) {
            if (!c.isVisible()) {
                continue;
            }
            // Dolduran bileşen küçülebilir; diğerleri her zaman tercih ettiği yüksekliği ister.
            Dimension d = preferred || c != fill ? c.getPreferredSize() : c.getMinimumSize();
            width = Math.max(width, preferred ? d.width : c.getMinimumSize().width);
            height += d.height;
            visible++;
        }
        height += gap * Math.max(0, visible - 1);
        return new Dimension(width + insets.left + insets.right, height + insets.top + insets.bottom);
    }

    @Override
    public void layoutContainer(Container parent) {
        Insets insets = parent.getInsets();
        int width = parent.getWidth() - insets.left - insets.right;
        int available = parent.getHeight() - insets.top - insets.bottom;

        int fixed = 0;
        int visible = 0;
        for (Component c : parent.getComponents()) {
            if (!c.isVisible()) {
                continue;
            }
            visible++;
            if (c != fill) {
                fixed += c.getPreferredSize().height;
            }
        }
        int fillHeight = Math.max(0, available - fixed - gap * Math.max(0, visible - 1));

        int y = insets.top;
        for (Component c : parent.getComponents()) {
            if (!c.isVisible()) {
                continue;
            }
            int height = c == fill ? fillHeight : c.getPreferredSize().height;
            c.setBounds(insets.left, y, width, height);
            y += height + gap;
        }
    }

    @Override
    public float getLayoutAlignmentX(Container target) {
        return 0.5f;
    }

    @Override
    public float getLayoutAlignmentY(Container target) {
        return 0.5f;
    }

    @Override
    public void invalidateLayout(Container target) {
        // önbelleğe alınan bir durum yok
    }
}

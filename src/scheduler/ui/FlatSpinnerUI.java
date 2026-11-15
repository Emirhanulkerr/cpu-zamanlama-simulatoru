package scheduler.ui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.RoundRectangle2D;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JSpinner;
import javax.swing.SwingConstants;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.plaf.basic.BasicFormattedTextFieldUI;
import javax.swing.plaf.basic.BasicSpinnerUI;

/** Temaya uyan sayı kutusu: yuvarlak köşeli çerçeve, sağda küçük artır / azalt okları. */
final class FlatSpinnerUI extends BasicSpinnerUI {

    static void install(JSpinner spinner) {
        spinner.setUI(new FlatSpinnerUI());
        spinner.setOpaque(false);
        spinner.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 3));

        if (spinner.getEditor() instanceof JSpinner.DefaultEditor editor) {
            editor.setOpaque(false);
            JFormattedTextField field = editor.getTextField();
            field.setUI(new BasicFormattedTextFieldUI());
            field.setOpaque(false);
            field.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 2));
            field.setHorizontalAlignment(SwingConstants.CENTER);
            field.setFont(spinner.getFont());
            field.setForeground(Theme.TEXT);
            field.setCaretColor(Theme.TEXT);
            field.setSelectionColor(Theme.ACCENT_SOFT);
            field.setSelectedTextColor(Theme.TEXT);
        }
    }

    @Override
    public void paint(Graphics graphics, JComponent c) {
        Graphics2D g = (Graphics2D) graphics.create();
        Theme.prepare(g);
        RoundRectangle2D shape = new RoundRectangle2D.Double(0.5, 0.5, c.getWidth() - 1, c.getHeight() - 1, 9, 9);
        g.setColor(Theme.CONTROL);
        g.fill(shape);
        g.setColor(Theme.BORDER);
        g.draw(shape);
        g.dispose();
    }

    @Override
    protected Component createNextButton() {
        JButton button = arrowButton(Icons.Shape.CHEVRON_UP);
        installNextButtonListeners(button);
        return button;
    }

    @Override
    protected Component createPreviousButton() {
        JButton button = arrowButton(Icons.Shape.CHEVRON_DOWN);
        installPreviousButtonListeners(button);
        return button;
    }

    private static JButton arrowButton(Icons.Shape shape) {
        JButton button = new JButton(Icons.of(shape, Theme.TEXT_SECONDARY, 10)) {
            @Override
            public void updateUI() {
                setUI(new BasicButtonUI());
            }
        };
        button.setBorder(BorderFactory.createEmptyBorder());
        button.setPreferredSize(new Dimension(18, 12));
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setFocusable(false);
        button.setOpaque(false);
        return button;
    }
}

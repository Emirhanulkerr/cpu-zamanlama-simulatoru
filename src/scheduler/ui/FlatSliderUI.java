package scheduler.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;

import javax.swing.JSlider;
import javax.swing.plaf.basic.BasicSliderUI;

/**
 * Temaya uyan zaman kaydırıcısı: ince bir yol, geçilen kısım vurgu renginde, yuvarlak tutamak.
 * Yolun herhangi bir yerine tıklandığında tutamak doğrudan o konuma gider.
 */
final class FlatSliderUI extends BasicSliderUI {

    private static final int TRACK_HEIGHT = 4;
    private static final int THUMB_SIZE = 16;

    private FlatSliderUI(JSlider slider) {
        super(slider);
    }

    static void install(JSlider slider) {
        slider.setUI(new FlatSliderUI(slider));
        slider.setOpaque(false);
    }

    @Override
    protected Dimension getThumbSize() {
        return new Dimension(THUMB_SIZE, THUMB_SIZE);
    }

    @Override
    public void paintTrack(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        Theme.prepare(g);
        double y = trackRect.y + (trackRect.height - TRACK_HEIGHT) / 2.0;
        double thumbCenter = thumbRect.x + thumbRect.width / 2.0;

        g.setColor(Theme.CONTROL_PRESSED);
        g.fill(new RoundRectangle2D.Double(trackRect.x, y, trackRect.width, TRACK_HEIGHT, TRACK_HEIGHT, TRACK_HEIGHT));
        if (slider.isEnabled()) {
            g.setColor(Theme.ACCENT);
            g.fill(new RoundRectangle2D.Double(
                    trackRect.x, y, thumbCenter - trackRect.x, TRACK_HEIGHT, TRACK_HEIGHT, TRACK_HEIGHT));
        }
        g.dispose();
    }

    @Override
    public void paintThumb(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        Theme.prepare(g);
        Ellipse2D thumb = new Ellipse2D.Double(
                thumbRect.x + 1.5, thumbRect.y + 1.5, thumbRect.width - 3, thumbRect.height - 3);
        if (slider.isEnabled()) {
            g.setColor(Theme.isDark() ? Theme.TEXT : Color.WHITE);
            g.fill(thumb);
            g.setColor(Theme.isDark() ? Theme.TEXT : Theme.AXIS);
            g.draw(thumb);
        } else {
            g.setColor(Theme.CONTROL_PRESSED);
            g.fill(thumb);
        }
        g.dispose();
    }

    @Override
    public void paintFocus(Graphics graphics) {
        // Odak çerçevesi çizilmez; tutamak zaten belirgindir.
    }

    @Override
    protected TrackListener createTrackListener(JSlider target) {
        return new TrackListener() {
            @Override
            public void mousePressed(MouseEvent e) {
                // Yola tıklandığında önce o konuma atlanır; ardından sürükleme oradan devam eder.
                if (slider.isEnabled() && !thumbRect.contains(e.getPoint())) {
                    slider.setValue(valueForXPosition(e.getX()));
                }
                super.mousePressed(e);
            }
        };
    }
}

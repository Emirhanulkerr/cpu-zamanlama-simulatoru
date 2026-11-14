package scheduler.ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.util.Locale;
import java.util.Map;

import javax.swing.UIManager;

/** Arayüzün renkleri, yazı tipleri ve ortak çizim yardımcıları. */
final class Theme {

    private Theme() {
    }

    // Yüzeyler
    static final Color PAGE = new Color(0xF1F2F4);
    static final Color SURFACE = Color.WHITE;
    static final Color SURFACE_ALT = new Color(0xF7F7F5);
    static final Color HEADER = new Color(0x14213D);

    // Metin
    static final Color TEXT = new Color(0x1A1A19);
    static final Color TEXT_SECONDARY = new Color(0x52514E);
    static final Color TEXT_MUTED = new Color(0x898781);

    // Çizgiler
    static final Color BORDER = new Color(0xDEDDD6);
    static final Color GRID = new Color(0xE8E7E1);
    static final Color AXIS = new Color(0xC3C2B7);

    // Vurgu
    static final Color ACCENT = new Color(0x2A78D6);
    static final Color ACCENT_HOVER = new Color(0x256ABF);
    static final Color ACCENT_PRESSED = new Color(0x1C5CAB);
    static final Color ACCENT_SOFT = new Color(0xE6F0FC);

    // CPU'nun boşta olduğu dilimler
    static final Color IDLE = new Color(0xEEEDE8);
    static final Color IDLE_HATCH = new Color(0xDAD9D2);

    /**
     * Süreç renkleri. Sıra bilinçli seçilmiştir: yan yana gelen renkler renk körlüğünde de
     * birbirinden ayırt edilebilir. Sekizden fazla süreçte renkler baştan tekrar eder;
     * bu yüzden her dilimde süreç adı da yazılır.
     */
    private static final Color[] PROCESS_COLORS = {
        new Color(0x2A78D6),  // mavi
        new Color(0xEB6834),  // turuncu
        new Color(0x1BAF7A),  // turkuaz
        new Color(0xEDA100),  // sarı
        new Color(0xE87BA4),  // pembe
        new Color(0x008300),  // yeşil
        new Color(0x6250D6),  // mor
        new Color(0xE34948),  // kırmızı
    };
    private static final Color YELLOW = PROCESS_COLORS[3];

    static final Locale TR = Locale.forLanguageTag("tr-TR");

    private static final Font BASE_FONT = baseFont();
    private static final Map<?, ?> DESKTOP_HINTS = desktopHints();

    static Color processColor(int processId) {
        return PROCESS_COLORS[Math.floorMod(processId, PROCESS_COLORS.length)];
    }

    /** Renkli bir dolgunun üzerine yazılacak metnin rengi. */
    static Color labelOn(Color fill) {
        return fill.equals(YELLOW) ? TEXT : Color.WHITE;
    }

    /** {@code color} rengini {@code amount} oranında (0–1) beyaza doğru açar. */
    static Color lighten(Color color, double amount) {
        return new Color(
                (int) Math.round(color.getRed() + (255 - color.getRed()) * amount),
                (int) Math.round(color.getGreen() + (255 - color.getGreen()) * amount),
                (int) Math.round(color.getBlue() + (255 - color.getBlue()) * amount));
    }

    static Color withAlpha(Color color, int alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
    }

    static Font font(int style, float size) {
        return BASE_FONT.deriveFont(style, size);
    }

    /** Kenar yumuşatmayı ve sistemin metin ayarlarını çizim bağlamına uygular. */
    static void prepare(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        if (DESKTOP_HINTS != null) {
            g.addRenderingHints(DESKTOP_HINTS);
        } else {
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        }
    }

    /** Ondalıklı sayıyı Türkçe biçimde (virgüllü) iki haneyle yazar. */
    static String number(double value) {
        return String.format(TR, "%.2f", value);
    }

    /** Metin {@code maxWidth} pikselden genişse sonunu "…" ile kısaltır. */
    static String ellipsize(String text, FontMetrics metrics, int maxWidth) {
        if (metrics.stringWidth(text) <= maxWidth) {
            return text;
        }
        String ellipsis = "…";
        int end = text.length();
        while (end > 0 && metrics.stringWidth(text.substring(0, end) + ellipsis) > maxWidth) {
            end--;
        }
        return end == 0 ? "" : text.substring(0, end) + ellipsis;
    }

    private static Font baseFont() {
        Font font = UIManager.getFont("Label.font");
        return font != null ? font : new Font(Font.SANS_SERIF, Font.PLAIN, 13);
    }

    private static Map<?, ?> desktopHints() {
        try {
            Object hints = Toolkit.getDefaultToolkit().getDesktopProperty("awt.font.desktophints");
            return hints instanceof Map<?, ?> map ? map : null;
        } catch (RuntimeException e) {
            return null;
        }
    }
}

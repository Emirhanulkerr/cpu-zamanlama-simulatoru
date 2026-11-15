package scheduler.ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.util.Locale;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.UIManager;
import javax.swing.plaf.BorderUIResource;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;

/**
 * Arayüzün renkleri, yazı tipleri ve ortak çizim yardımcıları.
 *
 * <p>Koyu ve açık olmak üzere iki tema vardır. Renk alanları {@link #setDark(boolean)} ile
 * yeniden atandığı için {@code final} değildir; tema değişince arayüz baştan kurulur.
 */
final class Theme {

    private Theme() {
    }

    /** Başlık çubuğu her iki temada da aynı laciverttir. */
    static final Color HEADER = new Color(0x14213D);
    static final Locale TR = Locale.forLanguageTag("tr-TR");

    // Yüzeyler
    static Color PAGE;
    static Color SURFACE;
    static Color SURFACE_ALT;

    // Metin
    static Color TEXT;
    static Color TEXT_SECONDARY;
    static Color TEXT_MUTED;

    // Çizgiler
    static Color BORDER;
    static Color GRID;
    static Color AXIS;

    // Vurgu
    static Color ACCENT;
    static Color ACCENT_HOVER;
    static Color ACCENT_PRESSED;
    static Color ACCENT_SOFT;
    static Color DANGER;

    // Düğme, açılır liste gibi denetimlerin zemini
    static Color CONTROL;
    static Color CONTROL_HOVER;
    static Color CONTROL_PRESSED;

    // CPU'nun boşta olduğu dilimler
    static Color IDLE;
    static Color IDLE_HATCH;

    /** Karşılaştırma grafiklerinde en iyi olmayan çubukların rengi. */
    static Color BAR_OTHER;

    /**
     * Süreç renkleri. Sıra bilinçli seçilmiştir: yan yana gelen renkler renk körlüğünde de
     * birbirinden ayırt edilebilir. Koyu temada aynı renklerin koyu zemine uygun tonları kullanılır.
     * Sekizden fazla süreçte renkler baştan tekrar eder; bu yüzden her dilimde süreç adı da yazılır.
     */
    private static final Color[] LIGHT_PROCESS_COLORS = {
        new Color(0x2A78D6),  // mavi
        new Color(0xEB6834),  // turuncu
        new Color(0x1BAF7A),  // turkuaz
        new Color(0xEDA100),  // sarı
        new Color(0xE87BA4),  // pembe
        new Color(0x008300),  // yeşil
        new Color(0x6250D6),  // mor
        new Color(0xE34948),  // kırmızı
    };
    private static final Color[] DARK_PROCESS_COLORS = {
        new Color(0x3987E5),
        new Color(0xD95926),
        new Color(0x199E70),
        new Color(0xC98500),
        new Color(0xD55181),
        new Color(0x0CA30C),
        new Color(0x9085E9),
        new Color(0xE66767),
    };
    private static final Color LIGHT_YELLOW = LIGHT_PROCESS_COLORS[3];
    private static final Color INK = new Color(0x1A1A19);

    private static final Font BASE_FONT = baseFont();
    private static final Map<?, ?> DESKTOP_HINTS = desktopHints();

    private static boolean dark;
    private static Color[] processColors;

    static {
        setDark(true);
    }

    static boolean isDark() {
        return dark;
    }

    /** Temayı seçer. Değişikliğin görünmesi için arayüzün yeniden kurulması gerekir. */
    static void setDark(boolean value) {
        dark = value;
        if (dark) {
            PAGE = new Color(0x0F0F10);
            SURFACE = new Color(0x1A1A19);
            SURFACE_ALT = new Color(0x222221);
            TEXT = new Color(0xF0EFEC);
            TEXT_SECONDARY = new Color(0xC3C2B7);
            TEXT_MUTED = new Color(0x898781);
            BORDER = new Color(0x31312F);
            GRID = new Color(0x2B2B29);
            AXIS = new Color(0x4D4C47);
            ACCENT = new Color(0x3987E5);
            ACCENT_HOVER = new Color(0x5598E7);
            ACCENT_PRESSED = new Color(0x2A78D6);
            ACCENT_SOFT = new Color(0x17304E);
            DANGER = new Color(0xE66767);
            CONTROL = new Color(0x262625);
            CONTROL_HOVER = new Color(0x30302E);
            CONTROL_PRESSED = new Color(0x3B3B38);
            IDLE = new Color(0x242423);
            IDLE_HATCH = new Color(0x393936);
            BAR_OTHER = new Color(0x1C5CAB);
            processColors = DARK_PROCESS_COLORS;
        } else {
            PAGE = new Color(0xF1F2F4);
            SURFACE = Color.WHITE;
            SURFACE_ALT = new Color(0xF7F7F5);
            TEXT = INK;
            TEXT_SECONDARY = new Color(0x52514E);
            TEXT_MUTED = new Color(0x898781);
            BORDER = new Color(0xDEDDD6);
            GRID = new Color(0xE8E7E1);
            AXIS = new Color(0xC3C2B7);
            ACCENT = new Color(0x2A78D6);
            ACCENT_HOVER = new Color(0x256ABF);
            ACCENT_PRESSED = new Color(0x1C5CAB);
            ACCENT_SOFT = new Color(0xE6F0FC);
            DANGER = new Color(0xD03B3B);
            CONTROL = Color.WHITE;
            CONTROL_HOVER = new Color(0xF7F7F5);
            CONTROL_PRESSED = new Color(0xE8E7E1);
            IDLE = new Color(0xEEEDE8);
            IDLE_HATCH = new Color(0xDAD9D2);
            BAR_OTHER = new Color(0x86B6EF);
            processColors = LIGHT_PROCESS_COLORS;
        }
        installToolTipDefaults();
    }

    /** İpuçları (tooltip) her iki temada da koyu zemin üzerine açık metinle gösterilir. */
    private static void installToolTipDefaults() {
        Color background = dark ? new Color(0x33332F) : new Color(0x262625);
        Color border = dark ? new Color(0x4D4C47) : background;
        UIManager.put("ToolTip.background", new ColorUIResource(background));
        UIManager.put("ToolTip.foreground", new ColorUIResource(new Color(0xF0EFEC)));
        UIManager.put("ToolTip.font", new FontUIResource(font(Font.PLAIN, 12f)));
        UIManager.put("ToolTip.border", new BorderUIResource(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(border), BorderFactory.createEmptyBorder(4, 8, 4, 8))));
    }

    static Color processColor(int processId) {
        return processColors[Math.floorMod(processId, processColors.length)];
    }

    /** Süreç renginde bir dolgunun üzerine yazılacak metnin rengi. */
    static Color labelOn(Color fill) {
        return fill.equals(LIGHT_YELLOW) ? INK : Color.WHITE;
    }

    /** {@code color} rengini {@code amount} oranında (0–1) beyaza doğru açar. */
    static Color lighten(Color color, double amount) {
        return mix(color, Color.WHITE, amount);
    }

    /** {@code color} rengini {@code amount} oranında (0–1) kart zeminine doğru soldurur. */
    static Color fade(Color color, double amount) {
        return mix(color, SURFACE, amount);
    }

    private static Color mix(Color from, Color to, double amount) {
        return new Color(
                (int) Math.round(from.getRed() + (to.getRed() - from.getRed()) * amount),
                (int) Math.round(from.getGreen() + (to.getGreen() - from.getGreen()) * amount),
                (int) Math.round(from.getBlue() + (to.getBlue() - from.getBlue()) * amount));
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

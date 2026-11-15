package scheduler.ui;

import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/** Uygulamanın ana penceresi. */
public final class MainFrame extends JFrame {

    private static final String TITLE = "CPU Zamanlama Simülatörü";

    public MainFrame() {
        super(TITLE);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setContentPane(new MainPanel());
        setMinimumSize(new Dimension(1080, 700));

        // Pencere, ekranın kullanılabilir alanını aşmayacak şekilde boyutlandırılır.
        Rectangle screen = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        setSize(Math.min(1340, screen.width), Math.min(840, screen.height));
        setLocationRelativeTo(null);
    }

    /**
     * Sistemin görünümünü (Look and Feel) uygular ve pencereyi açar.
     *
     * @param dark {@code true} ise koyu, değilse açık temayla başlar
     */
    public static void launch(boolean dark) {
        // macOS'ta pencere başlığını temayla uyumlu yapar; pencere açılmadan önce ayarlanmalıdır.
        System.setProperty("apple.awt.application.name", TITLE);
        System.setProperty("apple.awt.application.appearance",
                dark ? "NSAppearanceNameDarkAqua" : "NSAppearanceNameAqua");

        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                // Sistem görünümü yüklenemezse Java'nın varsayılan görünümüyle devam edilir.
            }
            Theme.setDark(dark);
            new MainFrame().setVisible(true);
        });
    }
}

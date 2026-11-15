package scheduler;

import java.awt.GraphicsEnvironment;
import java.util.Arrays;
import java.util.List;

import scheduler.cli.ConsoleApp;
import scheduler.ui.MainFrame;

/**
 * Programın giriş noktası.
 *
 * <ul>
 *   <li>Argümansız çalıştırıldığında grafik arayüzü koyu temayla açar.</li>
 *   <li>{@code --acik} ile grafik arayüz açık temayla başlar.</li>
 *   <li>{@code --cli} ile çalıştırıldığında konsol modunda çalışır
 *       (ayrıntılar için {@code --cli --yardim}).</li>
 * </ul>
 */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        boolean cli = args.length > 0 && (args[0].equals("--cli") || args[0].equals("--konsol"));
        if (cli) {
            System.exit(ConsoleApp.run(Arrays.copyOfRange(args, 1, args.length)));
        } else if (GraphicsEnvironment.isHeadless()) {
            System.err.println("Grafik ortamı bulunamadı; konsol modu için --cli seçeneğini kullanın.");
            System.exit(1);
        } else {
            MainFrame.launch(!List.of(args).contains("--acik"));
        }
    }
}

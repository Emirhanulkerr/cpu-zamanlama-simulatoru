package scheduler.cli;

import java.util.List;
import java.util.Locale;

import scheduler.model.GanttSegment;
import scheduler.model.ProcessMetrics;
import scheduler.model.SimProcess;
import scheduler.model.SimulationResult;

/** Simülasyon sonuçlarını konsol için düz metne (ASCII Gantt şeması ve tablolar) dönüştürür. */
public final class ConsoleReport {

    private static final Locale TR = Locale.forLanguageTag("tr-TR");
    private static final int MAX_LINE_WIDTH = 100;

    private ConsoleReport() {
    }

    /** Tek bir algoritmanın tam raporu: Gantt şeması, süreç tablosu ve özet ölçümler. */
    public static String render(SimulationResult result) {
        StringBuilder sb = new StringBuilder();
        String title = "=== " + result.algorithmName() + " ===";
        sb.append(title).append("\n\n");
        sb.append("Gantt Şeması:\n").append(gantt(result.gantt())).append('\n');

        String rowFormat = "%-10s %6s %8s %8s %10s %6s %6s %8s %6s%n";
        sb.append(String.format(rowFormat,
                "Süreç", "Varış", "Çalışma", "Öncelik", "Başlangıç", "Bitiş", "Dönüş", "Bekleme", "Yanıt"));
        sb.append("-".repeat(76)).append('\n');
        for (ProcessMetrics m : result.metrics()) {
            SimProcess p = m.process();
            sb.append(String.format(rowFormat,
                    p.name(), p.arrivalTime(), p.burstTime(), p.priority(),
                    m.startTime(), m.completionTime(), m.turnaroundTime(), m.waitingTime(), m.responseTime()));
        }
        sb.append("-".repeat(76)).append('\n');
        sb.append(String.format(rowFormat, "Ortalama", "", "", "", "", "",
                number(result.averageTurnaroundTime()),
                number(result.averageWaitingTime()),
                number(result.averageResponseTime())));

        sb.append('\n');
        sb.append(String.format("Ortalama bekleme süresi : %s%n", number(result.averageWaitingTime())));
        sb.append(String.format("Ortalama dönüş süresi   : %s%n", number(result.averageTurnaroundTime())));
        sb.append(String.format("Ortalama yanıt süresi   : %s%n", number(result.averageResponseTime())));
        sb.append(String.format("CPU kullanım oranı      : %%%s%n", number(result.cpuUtilization())));
        sb.append(String.format("İş çıkarma oranı        : %s süreç/birim%n", number(result.throughput())));
        sb.append(String.format("Bağlam değişimi sayısı  : %d%n", result.contextSwitches()));
        return sb.toString();
    }

    /** Birden çok algoritmanın özet ölçümlerini yan yana gösteren karşılaştırma tablosu. */
    public static String comparison(List<SimulationResult> results) {
        String rowFormat = "%-32s %12s %10s %10s %8s %8s%n";
        StringBuilder sb = new StringBuilder();
        sb.append("=== Algoritma Karşılaştırması ===\n\n");
        sb.append(String.format(rowFormat, "Algoritma", "Ort.Bekleme", "Ort.Dönüş", "Ort.Yanıt", "CPU %", "Bağlam"));
        sb.append("-".repeat(85)).append('\n');
        for (SimulationResult r : results) {
            sb.append(String.format(rowFormat,
                    r.algorithmName(),
                    number(r.averageWaitingTime()),
                    number(r.averageTurnaroundTime()),
                    number(r.averageResponseTime()),
                    number(r.cpuUtilization()),
                    r.contextSwitches()));
        }
        return sb.toString();
    }

    /**
     * Gantt şemasını kutular ve altlarında zaman etiketleriyle çizer. Örnek:
     * <pre>
     * +------+----+------+
     * |  P1  | P2 |  P1  |
     * +------+----+------+
     * 0      3    5      8
     * </pre>
     * Şema çok genişse birden fazla satıra bölünür.
     */
    static String gantt(List<GanttSegment> segments) {
        StringBuilder out = new StringBuilder();
        StringBuilder border = new StringBuilder("+");
        StringBuilder labels = new StringBuilder("|");
        StringBuilder times = new StringBuilder();

        for (int i = 0; i < segments.size(); i++) {
            GanttSegment segment = segments.get(i);
            String label = segment.isIdle() ? "boş" : segment.process().name();
            int width = Math.max(
                    Math.max(label.length() + 2, String.valueOf(segment.end()).length() + 1),
                    Math.min(segment.duration() * 2, 12));

            if (border.length() > 1 && border.length() + width + 1 > MAX_LINE_WIDTH) {
                flush(out, border, labels, times, segment.start());
            }

            pad(times, border.length() - 1);
            times.append(segment.start());
            border.append("-".repeat(width)).append('+');
            labels.append(center(label, width)).append('|');

            if (i == segments.size() - 1) {
                flush(out, border, labels, times, segment.end());
            }
        }
        return out.toString();
    }

    /** Biriken satırları çıktıya yazar ve yeni bir blok için tamponları sıfırlar. */
    private static void flush(
            StringBuilder out, StringBuilder border, StringBuilder labels, StringBuilder times, int endTime) {
        pad(times, border.length() - 1);
        times.append(endTime);
        out.append(border).append('\n')
                .append(labels).append('\n')
                .append(border).append('\n')
                .append(times).append('\n');
        border.setLength(0);
        border.append('+');
        labels.setLength(0);
        labels.append('|');
        times.setLength(0);
    }

    private static void pad(StringBuilder sb, int length) {
        while (sb.length() < length) {
            sb.append(' ');
        }
    }

    private static String center(String text, int width) {
        int left = (width - text.length()) / 2;
        return " ".repeat(left) + text + " ".repeat(width - text.length() - left);
    }

    private static String number(double value) {
        return String.format(TR, "%.2f", value);
    }
}

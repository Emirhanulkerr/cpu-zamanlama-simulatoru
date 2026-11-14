package scheduler.ui;

import java.util.List;
import java.util.function.ToDoubleFunction;

import javax.swing.table.AbstractTableModel;

import scheduler.model.SimulationResult;

/** Tüm algoritmaların özet ölçümlerini yan yana gösteren karşılaştırma tablosu. */
final class ComparisonTableModel extends AbstractTableModel {

    static final String[] TOOLTIPS = {
        "Zamanlama algoritması",
        "Süreçlerin hazır kuyruğunda geçirdiği ortalama süre",
        "Varıştan tamamlanmaya kadar geçen ortalama süre",
        "Varıştan CPU'yu ilk almaya kadar geçen ortalama süre",
        "CPU'nun dolu olduğu sürenin toplam süreye oranı",
        "CPU'nun bir süreçten farklı bir sürece geçme sayısı",
    };

    private static final String[] COLUMNS = {
        "Algoritma", "Ort. Bekleme", "Ort. Dönüş", "Ort. Yanıt", "CPU Kullanımı", "Bağlam Değişimi",
    };

    private List<SimulationResult> results = List.of();

    void setResults(List<SimulationResult> results) {
        this.results = List.copyOf(results);
        fireTableDataChanged();
    }

    /** Hücredeki değer, kendi sütununda en iyi (en düşük) değer mi? */
    boolean isBest(int row, int column) {
        ToDoubleFunction<SimulationResult> metric = switch (column) {
            case 1 -> SimulationResult::averageWaitingTime;
            case 2 -> SimulationResult::averageTurnaroundTime;
            case 3 -> SimulationResult::averageResponseTime;
            case 5 -> SimulationResult::contextSwitches;
            default -> null;  // CPU kullanımı bu algoritmaların hepsinde aynıdır
        };
        if (metric == null || results.size() < 2) {
            return false;
        }
        double best = results.stream().mapToDouble(metric).min().orElse(0);
        return Math.abs(metric.applyAsDouble(results.get(row)) - best) < 1e-9;
    }

    @Override
    public int getRowCount() {
        return results.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMNS.length;
    }

    @Override
    public String getColumnName(int column) {
        return COLUMNS[column];
    }

    @Override
    public Object getValueAt(int row, int column) {
        SimulationResult r = results.get(row);
        return switch (column) {
            case 0 -> r.algorithmName();
            case 1 -> Theme.number(r.averageWaitingTime());
            case 2 -> Theme.number(r.averageTurnaroundTime());
            case 3 -> Theme.number(r.averageResponseTime());
            case 4 -> "%" + String.format(Theme.TR, "%.1f", r.cpuUtilization());
            default -> r.contextSwitches();
        };
    }
}

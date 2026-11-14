package scheduler.ui;

import javax.swing.table.AbstractTableModel;

import scheduler.model.ProcessMetrics;
import scheduler.model.SimProcess;
import scheduler.model.SimulationResult;

/** Süreç bazlı sonuç tablosu; son satırda ortalamalar yer alır. */
final class ResultsTableModel extends AbstractTableModel {

    static final String[] TOOLTIPS = {
        "Süreç adı",
        "Varış zamanı",
        "Çalışma süresi (CPU burst)",
        "Öncelik değeri",
        "Sürecin CPU'yu ilk aldığı an",
        "Sürecin tamamlandığı an",
        "Dönüş süresi = Bitiş − Varış",
        "Bekleme süresi = Dönüş − Çalışma",
        "Yanıt süresi = Başlangıç − Varış",
    };

    private static final String[] COLUMNS = {
        "Süreç", "Varış", "Çalışma", "Öncelik", "Başlangıç", "Bitiş", "Dönüş", "Bekleme", "Yanıt",
    };

    private SimulationResult result;

    void setResult(SimulationResult result) {
        this.result = result;
        fireTableDataChanged();
    }

    /** Ortalamaların yer aldığı son satır mı? */
    boolean isAverageRow(int row) {
        return result != null && row == result.processes().size();
    }

    @Override
    public int getRowCount() {
        return result == null ? 0 : result.processes().size() + 1;
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
        if (isAverageRow(row)) {
            return switch (column) {
                case 0 -> "Ortalama";
                case 6 -> Theme.number(result.averageTurnaroundTime());
                case 7 -> Theme.number(result.averageWaitingTime());
                case 8 -> Theme.number(result.averageResponseTime());
                default -> "";
            };
        }
        ProcessMetrics m = result.metrics().get(row);
        SimProcess p = m.process();
        return switch (column) {
            case 0 -> p.name();
            case 1 -> p.arrivalTime();
            case 2 -> p.burstTime();
            case 3 -> p.priority();
            case 4 -> m.startTime();
            case 5 -> m.completionTime();
            case 6 -> m.turnaroundTime();
            case 7 -> m.waitingTime();
            default -> m.responseTime();
        };
    }
}

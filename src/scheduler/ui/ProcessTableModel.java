package scheduler.ui;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.table.AbstractTableModel;

import scheduler.model.Limits;
import scheduler.model.SimProcess;

/**
 * Kullanıcının düzenlediği süreç tablosunun veri modeli.
 * Süreç kimlikleri her zaman satır numarasına eşittir; bu sayede renkler satır sırasını izler.
 */
final class ProcessTableModel extends AbstractTableModel {

    static final String[] TOOLTIPS = {
        "Süreç adı",
        "Varış zamanı: sürecin sisteme geldiği an",
        "Çalışma süresi: sürecin ihtiyaç duyduğu toplam CPU süresi (CPU burst)",
        "Öncelik değeri: yalnızca öncelikli zamanlamada kullanılır",
    };

    private static final String[] COLUMNS = {"Süreç", "Varış", "Çalışma", "Öncelik"};
    private static final int MAX_NAME_LENGTH = 10;

    private final List<SimProcess> rows = new ArrayList<>();

    List<SimProcess> processes() {
        return List.copyOf(rows);
    }

    void setProcesses(List<SimProcess> processes) {
        rows.clear();
        for (SimProcess p : processes) {
            rows.add(new SimProcess(rows.size(), p.name(), p.arrivalTime(), p.burstTime(), p.priority()));
        }
        fireTableDataChanged();
    }

    /** Sona, kullanılmayan ilk "P<n>" adıyla yeni bir süreç ekler. */
    void addProcess() {
        if (rows.size() >= Limits.MAX_PROCESSES) {
            return;
        }
        Set<String> names = new HashSet<>();
        for (SimProcess p : rows) {
            names.add(p.name());
        }
        int number = 1;
        while (names.contains("P" + number)) {
            number++;
        }
        int arrival = rows.isEmpty() ? 0 : Math.min(Limits.MAX_ARRIVAL, rows.get(rows.size() - 1).arrivalTime() + 1);
        rows.add(new SimProcess(rows.size(), "P" + number, arrival, 3, 1));
        fireTableRowsInserted(rows.size() - 1, rows.size() - 1);
    }

    void removeRows(int[] indexes) {
        Set<Integer> toRemove = new HashSet<>();
        for (int index : indexes) {
            toRemove.add(index);
        }
        List<SimProcess> kept = new ArrayList<>();
        for (int i = 0; i < rows.size(); i++) {
            if (!toRemove.contains(i)) {
                kept.add(rows.get(i));
            }
        }
        setProcesses(kept);
    }

    boolean isFull() {
        return rows.size() >= Limits.MAX_PROCESSES;
    }

    @Override
    public int getRowCount() {
        return rows.size();
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
    public Class<?> getColumnClass(int column) {
        return column == 0 ? String.class : Integer.class;
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return true;
    }

    @Override
    public Object getValueAt(int row, int column) {
        SimProcess p = rows.get(row);
        return switch (column) {
            case 0 -> p.name();
            case 1 -> p.arrivalTime();
            case 2 -> p.burstTime();
            default -> p.priority();
        };
    }

    /** Geçersiz değerler yok sayılır; sınır dışındaki sayılar en yakın sınıra çekilir. */
    @Override
    public void setValueAt(Object value, int row, int column) {
        if (value == null || row >= rows.size()) {
            return;
        }
        SimProcess p = rows.get(row);
        String name = p.name();
        int arrival = p.arrivalTime();
        int burst = p.burstTime();
        int priority = p.priority();

        if (column == 0) {
            // Ayırıcı karakterler CSV dosyasını bozacağı için addan çıkarılır.
            String text = value.toString().replaceAll("[,;\\t]", "").trim();
            if (text.isEmpty()) {
                return;
            }
            name = text.length() > MAX_NAME_LENGTH ? text.substring(0, MAX_NAME_LENGTH) : text;
        } else if (value instanceof Number number) {
            int n = number.intValue();
            switch (column) {
                case 1 -> arrival = clamp(n, 0, Limits.MAX_ARRIVAL);
                case 2 -> burst = clamp(n, 1, Limits.MAX_BURST);
                default -> priority = clamp(n, 0, Limits.MAX_PRIORITY);
            }
        } else {
            return;
        }

        SimProcess updated = new SimProcess(p.id(), name, arrival, burst, priority);
        if (!updated.equals(p)) {
            rows.set(row, updated);
            fireTableRowsUpdated(row, row);
        }
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}

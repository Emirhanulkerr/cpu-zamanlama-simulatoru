package scheduler.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Bir zamanlama algoritmasının çalıştırılmasıyla elde edilen sonuç:
 * Gantt şeması, süreç bazlı ölçümler, ortalamalar ve animasyon için zaman çizelgesi.
 */
public final class SimulationResult {

    private final String algorithmName;
    private final String algorithmShortName;
    private final List<SimProcess> processes;
    private final List<ProcessMetrics> metrics;
    private final List<TickSnapshot> timeline;
    private final List<GanttSegment> gantt;
    private final TickSnapshot finalSnapshot;

    private final int totalTime;
    private final int busyTime;
    private final int contextSwitches;
    private final double averageWaitingTime;
    private final double averageTurnaroundTime;
    private final double averageResponseTime;

    /**
     * @param processes girdi sırasıyla süreçler
     * @param metrics   {@code processes} ile aynı sırada ölçümler
     * @param timeline  0. zaman biriminden son birime kadar anlık görüntüler
     */
    public SimulationResult(
            String algorithmName,
            String algorithmShortName,
            List<SimProcess> processes,
            List<ProcessMetrics> metrics,
            List<TickSnapshot> timeline) {
        this.algorithmName = algorithmName;
        this.algorithmShortName = algorithmShortName;
        this.processes = List.copyOf(processes);
        this.metrics = List.copyOf(metrics);
        this.timeline = List.copyOf(timeline);
        this.gantt = buildGantt(this.timeline);
        this.totalTime = this.timeline.size();
        this.finalSnapshot = new TickSnapshot(totalTime, null, List.of(), new int[this.processes.size()]);

        int busy = 0;
        int dispatches = 0;
        for (GanttSegment segment : gantt) {
            if (!segment.isIdle()) {
                busy += segment.duration();
                dispatches++;
            }
        }
        this.busyTime = busy;
        this.contextSwitches = Math.max(0, dispatches - 1);

        this.averageWaitingTime = this.metrics.stream().mapToInt(ProcessMetrics::waitingTime).average().orElse(0);
        this.averageTurnaroundTime = this.metrics.stream().mapToInt(ProcessMetrics::turnaroundTime).average().orElse(0);
        this.averageResponseTime = this.metrics.stream().mapToInt(ProcessMetrics::responseTime).average().orElse(0);
    }

    /** Ardışık zaman birimlerinde aynı süreç çalışıyorsa bunları tek bir Gantt diliminde birleştirir. */
    private static List<GanttSegment> buildGantt(List<TickSnapshot> timeline) {
        List<GanttSegment> segments = new ArrayList<>();
        SimProcess current = null;
        int start = 0;
        for (TickSnapshot tick : timeline) {
            if (tick.time() == 0) {
                current = tick.running();
            } else if (!Objects.equals(current, tick.running())) {
                segments.add(new GanttSegment(current, start, tick.time()));
                current = tick.running();
                start = tick.time();
            }
        }
        if (!timeline.isEmpty()) {
            segments.add(new GanttSegment(current, start, timeline.size()));
        }
        return List.copyOf(segments);
    }

    public String algorithmName() {
        return algorithmName;
    }

    public String algorithmShortName() {
        return algorithmShortName;
    }

    public List<SimProcess> processes() {
        return processes;
    }

    public List<ProcessMetrics> metrics() {
        return metrics;
    }

    public List<GanttSegment> gantt() {
        return gantt;
    }

    public List<TickSnapshot> timeline() {
        return timeline;
    }

    /** Son sürecin bittiği an (simülasyonun toplam süresi). */
    public int totalTime() {
        return totalTime;
    }

    /** CPU'nun boşta geçirdiği toplam süre. */
    public int idleTime() {
        return totalTime - busyTime;
    }

    /** CPU'nun bir süreçten farklı bir sürece geçme sayısı. */
    public int contextSwitches() {
        return contextSwitches;
    }

    public double averageWaitingTime() {
        return averageWaitingTime;
    }

    public double averageTurnaroundTime() {
        return averageTurnaroundTime;
    }

    public double averageResponseTime() {
        return averageResponseTime;
    }

    /** CPU kullanım oranı (0–100). */
    public double cpuUtilization() {
        return totalTime == 0 ? 0 : 100.0 * busyTime / totalTime;
    }

    /** İş çıkarma oranı: birim zamanda tamamlanan süreç sayısı. */
    public double throughput() {
        return totalTime == 0 ? 0 : (double) processes.size() / totalTime;
    }

    /**
     * {@code time} anındaki durum. Simülasyon bittikten sonraki anlar için
     * "her şey tamamlandı" görüntüsü döner.
     */
    public TickSnapshot snapshotAt(int time) {
        if (time >= totalTime) {
            return finalSnapshot;
        }
        return timeline.get(Math.max(0, time));
    }

    /** Girdi sırasındaki {@code index}. sürecin {@code time} anındaki durumu. */
    public ProcessState stateAt(int index, int time) {
        SimProcess process = processes.get(index);
        if (time >= metrics.get(index).completionTime()) {
            return ProcessState.FINISHED;
        }
        if (time < process.arrivalTime()) {
            return ProcessState.NOT_ARRIVED;
        }
        return process.equals(snapshotAt(time).running()) ? ProcessState.RUNNING : ProcessState.READY;
    }
}

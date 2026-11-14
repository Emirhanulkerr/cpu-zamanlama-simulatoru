package scheduler.algorithm;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import scheduler.model.ProcessMetrics;
import scheduler.model.SimProcess;
import scheduler.model.SimulationResult;
import scheduler.model.TickSnapshot;

/**
 * Tüm zamanlama algoritmalarının ortak simülasyon motoru (Template Method deseni).
 *
 * <p>Motor zamanı birer birim ilerletir ve her birimde şu adımları uygular:
 * <ol>
 *   <li>O ana kadar gelmiş süreçleri hazır kuyruğuna alır.</li>
 *   <li>Çalışan sürecin kesilmesi gerekip gerekmediğine karar verir
 *       (zaman dilimi doldu mu, daha uygun bir süreç geldi mi).</li>
 *   <li>CPU boşsa hazır kuyruğundan bir süreç seçer.</li>
 *   <li>Seçilen süreci bir birim çalıştırır.</li>
 * </ol>
 *
 * <p>Algoritmalar yalnızca kendilerini birbirinden ayıran üç şeyi tanımlar:
 * {@link #criterion() seçim ölçütü}, {@link #isPreemptive() kesmeli olup olmadığı}
 * ve {@link #timeQuantum() zaman dilimi}.
 */
public abstract class Scheduler {

    /** Eşitlik durumunda uygulanan sıra: önce gelen önce, o da eşitse girdi sırası (FCFS). */
    private static final Comparator<ProcessControlBlock> ARRIVAL_ORDER =
            Comparator.comparingInt(ProcessControlBlock::arrivalTime).thenComparingInt(ProcessControlBlock::id);

    /** Algoritmanın tam adı (ör. "Round Robin (q = 2)"). */
    public abstract String name();

    /** Grafik ve tablolarda kullanılan kısa ad (ör. "RR (q=2)"). */
    public abstract String shortName();

    /**
     * Hazır kuyruğundan süreç seçme ölçütü: karşılaştırmada küçük çıkan süreç önce seçilir.
     * {@code null} dönerse kuyruk FIFO olarak işlenir (FCFS ve Round Robin).
     * Ölçüt eşit çıkarsa motor önce gelen süreci seçer.
     */
    protected Comparator<ProcessControlBlock> criterion() {
        return null;
    }

    /**
     * Kesmeli (preemptive) algoritmalarda, hazır kuyruğuna ölçüte göre çalışan süreçten
     * <em>kesin olarak</em> daha uygun bir süreç geldiğinde CPU çalışan süreçten alınır.
     */
    protected boolean isPreemptive() {
        return false;
    }

    /**
     * Bir sürecin CPU'da kesintisiz kalabileceği en uzun süre.
     * Varsayılan değer sınırsızdır; Round Robin bunu zaman dilimi (quantum) ile sınırlar.
     */
    protected int timeQuantum() {
        return Integer.MAX_VALUE;
    }

    /** Verilen süreçleri bu algoritmayla zamanlar ve sonucu döndürür. */
    public final SimulationResult simulate(List<SimProcess> processes) {
        validate(processes);

        List<ProcessControlBlock> all = new ArrayList<>();
        for (SimProcess process : processes) {
            all.add(new ProcessControlBlock(process));
        }
        List<ProcessControlBlock> arrivals = new ArrayList<>(all);
        arrivals.sort(ARRIVAL_ORDER);

        Comparator<ProcessControlBlock> criterion = criterion();
        Comparator<ProcessControlBlock> selectionOrder =
                criterion == null ? null : criterion.thenComparing(ARRIVAL_ORDER);

        List<ProcessControlBlock> readyQueue = new ArrayList<>();
        List<TickSnapshot> timeline = new ArrayList<>();
        ProcessControlBlock running = null;
        int sliceUsed = 0;      // çalışan sürecin mevcut zaman diliminde kullandığı süre
        int nextArrival = 0;
        int completed = 0;
        int time = 0;

        while (completed < all.size()) {
            // 1) Bu ana kadar gelen süreçler hazır kuyruğuna girer.
            //    Yeni gelenler, aynı anda zaman dilimi dolan süreçten ÖNCE kuyruğa alınır.
            while (nextArrival < arrivals.size() && arrivals.get(nextArrival).arrivalTime() <= time) {
                readyQueue.add(arrivals.get(nextArrival++));
            }
            if (selectionOrder != null) {
                readyQueue.sort(selectionOrder);
            }

            // 2) Kesme kararı: CPU'yu bekleyen biri varken zaman dilimi dolduysa ya da
            //    kesmeli algoritmada daha uygun bir süreç bekliyorsa çalışan süreç kuyruğa döner.
            if (running != null) {
                boolean quantumExpired = sliceUsed >= timeQuantum();
                if (readyQueue.isEmpty()) {
                    if (quantumExpired) {
                        sliceUsed = 0;  // bekleyen yok: aynı süreç yeni bir zaman dilimiyle devam eder
                    }
                } else {
                    boolean betterProcessWaiting = isPreemptive()
                            && criterion != null
                            && criterion.compare(readyQueue.get(0), running) < 0;
                    if (quantumExpired || betterProcessWaiting) {
                        readyQueue.add(running);
                        if (selectionOrder != null) {
                            readyQueue.sort(selectionOrder);
                        }
                        running = null;
                    }
                }
            }

            // 3) CPU boşsa kuyruğun başındaki süreç CPU'ya verilir (dispatch).
            if (running == null && !readyQueue.isEmpty()) {
                running = readyQueue.remove(0);
                running.dispatch(time);
                sliceUsed = 0;
            }

            timeline.add(snapshot(time, running, readyQueue, all));

            // 4) Süreç bir zaman birimi çalışır; bittiyse CPU'yu bırakır.
            if (running != null) {
                sliceUsed++;
                if (running.runOneUnit(time)) {
                    completed++;
                    running = null;
                }
            }
            time++;
        }

        List<ProcessMetrics> metrics = new ArrayList<>();
        for (ProcessControlBlock pcb : all) {
            metrics.add(ProcessMetrics.of(pcb.process(), pcb.startTime(), pcb.completionTime()));
        }
        return new SimulationResult(name(), shortName(), processes, metrics, timeline);
    }

    private static TickSnapshot snapshot(
            int time, ProcessControlBlock running, List<ProcessControlBlock> readyQueue, List<ProcessControlBlock> all) {
        List<SimProcess> ready = new ArrayList<>(readyQueue.size());
        for (ProcessControlBlock pcb : readyQueue) {
            ready.add(pcb.process());
        }
        int[] remaining = new int[all.size()];
        for (int i = 0; i < remaining.length; i++) {
            remaining[i] = all.get(i).remainingTime();
        }
        return new TickSnapshot(time, running == null ? null : running.process(), ready, remaining);
    }

    private static void validate(List<SimProcess> processes) {
        if (processes == null || processes.isEmpty()) {
            throw new IllegalArgumentException("Simülasyon için en az bir süreç gerekir.");
        }
        Set<Integer> ids = new HashSet<>();
        for (SimProcess process : processes) {
            if (!ids.add(process.id())) {
                throw new IllegalArgumentException("Süreç kimlikleri benzersiz olmalıdır: " + process.id());
            }
        }
    }
}

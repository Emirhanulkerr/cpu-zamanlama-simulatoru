package scheduler.algorithm;

import java.util.Comparator;

/**
 * Öncelikli Zamanlama (Priority Scheduling) — kesmesiz ya da kesmeli.
 *
 * <p>Hazır kuyruğundaki en yüksek öncelikli süreç seçilir. Kesmeli sürümde, çalışan
 * süreçten daha yüksek öncelikli bir süreç geldiğinde CPU ona verilir.
 * Varsayılan olarak küçük sayı yüksek önceliği gösterir; istenirse tersi seçilebilir.
 * Düşük öncelikli süreçler açlık (starvation) çekebilir.
 */
public final class PriorityScheduler extends Scheduler {

    private final boolean preemptive;
    private final boolean higherNumberIsHigherPriority;

    /**
     * @param preemptive                   {@code true} ise kesmeli çalışır
     * @param higherNumberIsHigherPriority {@code true} ise büyük sayı yüksek öncelik sayılır
     */
    public PriorityScheduler(boolean preemptive, boolean higherNumberIsHigherPriority) {
        this.preemptive = preemptive;
        this.higherNumberIsHigherPriority = higherNumberIsHigherPriority;
    }

    @Override
    public String name() {
        return preemptive ? "Öncelikli Zamanlama (kesmeli)" : "Öncelikli Zamanlama (kesmesiz)";
    }

    @Override
    public String shortName() {
        return preemptive ? "Öncelik (kesmeli)" : "Öncelik";
    }

    @Override
    protected Comparator<ProcessControlBlock> criterion() {
        Comparator<ProcessControlBlock> byPriority = Comparator.comparingInt(ProcessControlBlock::priority);
        return higherNumberIsHigherPriority ? byPriority.reversed() : byPriority;
    }

    @Override
    protected boolean isPreemptive() {
        return preemptive;
    }
}

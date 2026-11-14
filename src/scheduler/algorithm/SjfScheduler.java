package scheduler.algorithm;

import java.util.Comparator;

/**
 * SJF (Shortest Job First) — En Kısa İş Önce, kesmesiz.
 *
 * <p>CPU boşaldığında hazır kuyruğundaki en kısa çalışma süreli süreç seçilir ve
 * bitene kadar çalışır. Kesmesiz algoritmalar içinde en düşük ortalama bekleme
 * süresini verir; ancak sürekli kısa işler gelirse uzun işler açlık (starvation) çekebilir.
 */
public final class SjfScheduler extends Scheduler {

    @Override
    public String name() {
        return "SJF (En Kısa İş Önce)";
    }

    @Override
    public String shortName() {
        return "SJF";
    }

    @Override
    protected Comparator<ProcessControlBlock> criterion() {
        return Comparator.comparingInt(ProcessControlBlock::burstTime);
    }
}

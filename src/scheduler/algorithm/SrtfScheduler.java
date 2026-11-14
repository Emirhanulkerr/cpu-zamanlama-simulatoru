package scheduler.algorithm;

import java.util.Comparator;

/**
 * SRTF (Shortest Remaining Time First) — En Kısa Kalan Süre Önce; SJF'nin kesmeli hâli.
 *
 * <p>Her an kalan süresi en kısa olan süreç çalışır. Çalışan süreçten daha kısa kalan
 * süreli bir süreç geldiğinde CPU yeni gelen sürece verilir.
 * Ortalama bekleme süresi açısından en iyi sonucu veren algoritmadır.
 */
public final class SrtfScheduler extends Scheduler {

    @Override
    public String name() {
        return "SRTF (En Kısa Kalan Süre Önce)";
    }

    @Override
    public String shortName() {
        return "SRTF";
    }

    @Override
    protected Comparator<ProcessControlBlock> criterion() {
        return Comparator.comparingInt(ProcessControlBlock::remainingTime);
    }

    @Override
    protected boolean isPreemptive() {
        return true;
    }
}

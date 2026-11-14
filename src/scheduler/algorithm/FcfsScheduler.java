package scheduler.algorithm;

/**
 * FCFS (First Come First Served) — İlk Gelen İlk Çalışır.
 *
 * <p>Süreçler hazır kuyruğuna girdikleri sırayla çalışır ve CPU'yu bitene kadar bırakmaz
 * (kesmesiz). Seçim ölçütü tanımlamaz; motorun FIFO kuyruğu yeterlidir.
 * Uzun bir süreç arkasındaki kısa süreçleri bekletir (konvoy etkisi).
 */
public final class FcfsScheduler extends Scheduler {

    @Override
    public String name() {
        return "FCFS (İlk Gelen İlk Çalışır)";
    }

    @Override
    public String shortName() {
        return "FCFS";
    }
}

package scheduler.algorithm;

/**
 * Round Robin — zaman dilimli (quantum) döngüsel zamanlama.
 *
 * <p>Hazır kuyruğu FIFO'dur. Her süreç en fazla {@code quantum} birim çalışır; süresi dolan
 * süreç kuyruğun sonuna geçer ve CPU sıradaki sürece verilir. Bir zaman dilimi dolduğu anda
 * yeni bir süreç geliyorsa, yeni gelen süreç kuyruğa kesilen süreçten önce girer.
 * Her sürece adil pay verdiği için etkileşimli sistemlerde kullanılır.
 */
public final class RoundRobinScheduler extends Scheduler {

    private final int quantum;

    public RoundRobinScheduler(int quantum) {
        if (quantum <= 0) {
            throw new IllegalArgumentException("Zaman dilimi (quantum) en az 1 olmalıdır.");
        }
        this.quantum = quantum;
    }

    @Override
    public String name() {
        return "Round Robin (q = " + quantum + ")";
    }

    @Override
    public String shortName() {
        return "RR (q=" + quantum + ")";
    }

    @Override
    protected int timeQuantum() {
        return quantum;
    }
}

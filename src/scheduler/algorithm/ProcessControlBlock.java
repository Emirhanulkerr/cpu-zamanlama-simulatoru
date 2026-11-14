package scheduler.algorithm;

import scheduler.model.SimProcess;

/**
 * Süreç Kontrol Bloğu (PCB): bir sürecin simülasyon sırasındaki değişken durumu.
 * Gerçek işletim sistemlerindeki PCB'nin zamanlama ile ilgili kısmının sadeleştirilmiş hâlidir.
 */
public final class ProcessControlBlock {

    private final SimProcess process;
    private int remainingTime;
    private int startTime = -1;
    private int completionTime = -1;

    ProcessControlBlock(SimProcess process) {
        this.process = process;
        this.remainingTime = process.burstTime();
    }

    public SimProcess process() {
        return process;
    }

    public int id() {
        return process.id();
    }

    public int arrivalTime() {
        return process.arrivalTime();
    }

    public int burstTime() {
        return process.burstTime();
    }

    public int priority() {
        return process.priority();
    }

    /** Sürecin bitmesi için gereken kalan CPU süresi. */
    public int remainingTime() {
        return remainingTime;
    }

    /** CPU'yu ilk aldığı an; henüz hiç çalışmadıysa -1. */
    public int startTime() {
        return startTime;
    }

    /** Bittiği an; henüz bitmediyse -1. */
    public int completionTime() {
        return completionTime;
    }

    /** Süreç CPU'ya verildiğinde çağrılır; ilk çalışmasıysa başlangıç zamanını kaydeder. */
    void dispatch(int time) {
        if (startTime < 0) {
            startTime = time;
        }
    }

    /** Süreci bir zaman birimi çalıştırır; süreç bu birimin sonunda bittiyse {@code true} döner. */
    boolean runOneUnit(int time) {
        remainingTime--;
        if (remainingTime == 0) {
            completionTime = time + 1;
            return true;
        }
        return false;
    }
}

package scheduler.model;

/**
 * Bir sürecin simülasyon sonundaki ölçümleri.
 *
 * @param process        ölçümlerin ait olduğu süreç
 * @param startTime      CPU'yu ilk aldığı an
 * @param completionTime bittiği an
 * @param turnaroundTime dönüş süresi = bitiş − varış
 * @param waitingTime    bekleme süresi = dönüş − çalışma süresi
 * @param responseTime   yanıt süresi = ilk çalışma − varış
 */
public record ProcessMetrics(
        SimProcess process,
        int startTime,
        int completionTime,
        int turnaroundTime,
        int waitingTime,
        int responseTime) {

    public static ProcessMetrics of(SimProcess process, int startTime, int completionTime) {
        int turnaround = completionTime - process.arrivalTime();
        return new ProcessMetrics(
                process,
                startTime,
                completionTime,
                turnaround,
                turnaround - process.burstTime(),
                startTime - process.arrivalTime());
    }
}

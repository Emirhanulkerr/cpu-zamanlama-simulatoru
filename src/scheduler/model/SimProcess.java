package scheduler.model;

/**
 * Simülasyona girdi olarak verilen bir süreç (process).
 *
 * <p>Değiştirilemez (immutable) bir veri taşıyıcısıdır. Sürecin çalışma anındaki
 * durumu (kalan süre, başlangıç/bitiş zamanı) burada değil,
 * {@code scheduler.algorithm.ProcessControlBlock} içinde tutulur.
 *
 * @param id          girdi sırasındaki 0 tabanlı, benzersiz sıra numarası
 *                    (eşitlik durumunda sıralama ve renk ataması için kullanılır)
 * @param name        süreç adı (ör. "P1")
 * @param arrivalTime varış zamanı (>= 0)
 * @param burstTime   CPU'da toplam çalışma süresi (> 0)
 * @param priority    öncelik değeri (hangi yönün yüksek öncelik olduğu algoritmada seçilir)
 */
public record SimProcess(int id, String name, int arrivalTime, int burstTime, int priority) {

    public SimProcess {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Süreç adı boş olamaz.");
        }
        if (arrivalTime < 0) {
            throw new IllegalArgumentException(name + ": varış zamanı negatif olamaz.");
        }
        if (burstTime <= 0) {
            throw new IllegalArgumentException(name + ": çalışma süresi en az 1 olmalıdır.");
        }
    }
}

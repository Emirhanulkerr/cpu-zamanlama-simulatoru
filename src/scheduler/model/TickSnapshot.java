package scheduler.model;

import java.util.List;

/**
 * Simülasyonun tek bir zaman birimindeki ({@code [time, time + 1)}) anlık görüntüsü.
 * Animasyon bu görüntüleri sırayla oynatır.
 *
 * @param time       zaman birimi
 * @param running    bu birimde CPU'da çalışan süreç; CPU boştaysa {@code null}
 * @param readyQueue hazır kuyruğu; ilk eleman bir sonraki seçilecek süreçtir
 * @param remaining  her sürecin bu birimin başındaki kalan süresi (girdi sırasıyla)
 */
public record TickSnapshot(int time, SimProcess running, List<SimProcess> readyQueue, int[] remaining) {

    public TickSnapshot {
        readyQueue = List.copyOf(readyQueue);
    }

    /** Girdi sırasındaki {@code index}. sürecin kalan süresi. */
    public int remainingOf(int index) {
        return remaining[index];
    }
}

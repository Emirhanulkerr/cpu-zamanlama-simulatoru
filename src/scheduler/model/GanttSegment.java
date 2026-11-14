package scheduler.model;

/**
 * Gantt şemasındaki tek bir dilim: CPU'nun {@code [start, end)} aralığında ne yaptığı.
 *
 * @param process bu aralıkta çalışan süreç; CPU boştaysa {@code null}
 * @param start   dilimin başlangıç zamanı (dahil)
 * @param end     dilimin bitiş zamanı (hariç)
 */
public record GanttSegment(SimProcess process, int start, int end) {

    public boolean isIdle() {
        return process == null;
    }

    public int duration() {
        return end - start;
    }
}

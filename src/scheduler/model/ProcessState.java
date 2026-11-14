package scheduler.model;

/** Bir sürecin belirli bir andaki durumu. */
public enum ProcessState {
    /** Süreç henüz sisteme gelmedi. */
    NOT_ARRIVED,
    /** Süreç hazır kuyruğunda CPU bekliyor. */
    READY,
    /** Süreç şu anda CPU'da çalışıyor. */
    RUNNING,
    /** Süreç işini tamamladı. */
    FINISHED
}

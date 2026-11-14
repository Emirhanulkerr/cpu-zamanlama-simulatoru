package scheduler.model;

/** Arayüz ve dosya girdisi için kabul edilen üst sınırlar. */
public final class Limits {

    private Limits() {
    }

    public static final int MAX_PROCESSES = 30;
    public static final int MAX_ARRIVAL = 999;
    public static final int MAX_BURST = 200;
    public static final int MAX_PRIORITY = 999;
    public static final int MAX_QUANTUM = 100;
}

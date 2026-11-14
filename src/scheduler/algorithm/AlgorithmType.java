package scheduler.algorithm;

/** Simülatörün desteklediği algoritmalar ve arayüzde gösterilen açıklamaları. */
public enum AlgorithmType {

    FCFS("FCFS — İlk Gelen İlk Çalışır",
            "Süreçler varış sırasına göre çalışır ve CPU'yu bitene kadar bırakmaz. "
                    + "Basittir; ancak uzun bir iş arkasındaki kısa işleri bekletir (konvoy etkisi)."),

    SJF("SJF — En Kısa İş Önce (kesmesiz)",
            "CPU boşaldığında hazır kuyruğundaki en kısa çalışma süreli süreç seçilir. "
                    + "Ortalama beklemeyi düşürür; uzun işler açlık çekebilir."),

    SRTF("SRTF — En Kısa Kalan Süre Önce (kesmeli SJF)",
            "Her an kalan süresi en kısa süreç çalışır; daha kısa bir süreç gelirse çalışan süreç kesilir. "
                    + "En düşük ortalama bekleme süresini verir."),

    PRIORITY("Öncelikli Zamanlama (kesmesiz)",
            "CPU boşaldığında en yüksek öncelikli süreç seçilir ve bitene kadar çalışır. "
                    + "Düşük öncelikli süreçler açlık çekebilir."),

    PRIORITY_PREEMPTIVE("Öncelikli Zamanlama (kesmeli)",
            "Her an en yüksek öncelikli süreç çalışır; daha yüksek öncelikli bir süreç gelirse "
                    + "çalışan süreç kesilir ve CPU yeni sürece verilir."),

    ROUND_ROBIN("Round Robin (zaman dilimli)",
            "Her süreç sırayla en fazla q birim çalışır; süresi dolan süreç kuyruğun sonuna geçer. "
                    + "Adildir, yanıt süresi kısadır; etkileşimli sistemlerde kullanılır.");

    private final String displayName;
    private final String description;

    AlgorithmType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String displayName() {
        return displayName;
    }

    public String description() {
        return description;
    }

    /**
     * Bu türde bir zamanlayıcı oluşturur.
     *
     * @param quantum                      Round Robin zaman dilimi (diğer algoritmalar yok sayar)
     * @param higherNumberIsHigherPriority öncelik yönü (yalnızca öncelikli zamanlama kullanır)
     */
    public Scheduler create(int quantum, boolean higherNumberIsHigherPriority) {
        return switch (this) {
            case FCFS -> new FcfsScheduler();
            case SJF -> new SjfScheduler();
            case SRTF -> new SrtfScheduler();
            case PRIORITY -> new PriorityScheduler(false, higherNumberIsHigherPriority);
            case PRIORITY_PREEMPTIVE -> new PriorityScheduler(true, higherNumberIsHigherPriority);
            case ROUND_ROBIN -> new RoundRobinScheduler(quantum);
        };
    }

    @Override
    public String toString() {
        return displayName;
    }
}

package scheduler.io;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import scheduler.model.SimProcess;

/** Algoritmaların farklı davranışlarını göstermek için hazır süreç setleri. */
public final class SampleData {

    /**
     * @param title     arayüzde gösterilen başlık
     * @param processes örnek süreçler
     */
    public record Sample(String title, List<SimProcess> processes) {
    }

    private SampleData() {
    }

    public static List<Sample> all() {
        return List.of(
                // Kesmeli ve kesmesiz algoritmaların farkını gösteren genel örnek
                new Sample("Temel örnek (5 süreç)", processes(new int[][] {
                        {0, 8, 3}, {1, 4, 1}, {2, 9, 4}, {3, 5, 2}, {6, 2, 5}})),
                // FCFS'de uzun işin kısaları bekletmesi
                new Sample("Konvoy etkisi (uzun iş önde)", processes(new int[][] {
                        {0, 24, 2}, {1, 3, 1}, {2, 3, 3}})),
                // Hepsi aynı anda gelir; sırayı yalnızca öncelik belirler
                new Sample("Öncelik örneği (hepsi t = 0)", processes(new int[][] {
                        {0, 10, 3}, {0, 1, 1}, {0, 2, 4}, {0, 1, 5}, {0, 5, 2}})),
                // Süreçler arasında CPU'nun boşta kaldığı aralıklar
                new Sample("Boşta kalan CPU (geç gelenler)", processes(new int[][] {
                        {0, 3, 2}, {5, 4, 1}, {6, 2, 3}, {14, 3, 1}})),
                // Kısa etkileşimli işlerle uzun hesaplama işlerinin karışımı
                new Sample("Karışık yük (kısa ve uzun işler)", processes(new int[][] {
                        {0, 12, 4}, {1, 2, 1}, {2, 7, 3}, {3, 1, 2}, {5, 9, 5}, {8, 3, 2}, {10, 2, 1}})));
    }

    /** Rastgele 4–7 süreçlik bir set üretir; ilk süreç her zaman t = 0 anında gelir. */
    public static List<SimProcess> random(Random random) {
        int count = 4 + random.nextInt(4);
        int[][] rows = new int[count][];
        for (int i = 0; i < count; i++) {
            int arrival = i == 0 ? 0 : random.nextInt(11);
            rows[i] = new int[] {arrival, 1 + random.nextInt(10), 1 + random.nextInt(5)};
        }
        return processes(rows);
    }

    /** Her satır {varış, çalışma, öncelik} olacak şekilde P1, P2, ... adlı süreçler oluşturur. */
    public static List<SimProcess> processes(int[][] rows) {
        List<SimProcess> list = new ArrayList<>();
        for (int i = 0; i < rows.length; i++) {
            list.add(new SimProcess(i, "P" + (i + 1), rows[i][0], rows[i][1], rows[i][2]));
        }
        return List.copyOf(list);
    }
}

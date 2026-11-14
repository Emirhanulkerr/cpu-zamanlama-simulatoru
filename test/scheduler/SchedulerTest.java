package scheduler;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import scheduler.algorithm.AlgorithmType;
import scheduler.algorithm.FcfsScheduler;
import scheduler.algorithm.PriorityScheduler;
import scheduler.algorithm.RoundRobinScheduler;
import scheduler.algorithm.Scheduler;
import scheduler.algorithm.SjfScheduler;
import scheduler.algorithm.SrtfScheduler;
import scheduler.io.ProcessCsv;
import scheduler.io.SampleData;
import scheduler.model.GanttSegment;
import scheduler.model.ProcessMetrics;
import scheduler.model.SimProcess;
import scheduler.model.SimulationResult;

/**
 * Algoritmaların doğruluğunu, sonuçları elle hesaplanmış ders kitabı örnekleriyle sınar.
 * Dış kütüphane gerektirmez: {@code ./test.sh} ile çalıştırılır; hata varsa çıkış kodu 1 olur.
 */
public final class SchedulerTest {

    private static int passed;
    private static int failed;

    public static void main(String[] args) {
        fcfsConvoy();
        sjfAllArriveAtZero();
        sjfWithArrivals();
        srtfTextbook();
        priorityTextbook();
        priorityHigherNumberWins();
        priorityPreemptive();
        roundRobinTextbook();
        roundRobinArrivalBeforeRequeue();
        roundRobinKeepsRunningWhenQueueEmpty();
        idleGaps();
        tieBreakIsFcfs();
        invariantsOnRandomWorkloads();
        csvRoundTrip();

        System.out.println();
        System.out.println(passed + " kontrol geçti, " + failed + " kontrol başarısız.");
        if (failed > 0) {
            System.exit(1);
        }
    }

    // ---------------------------------------------------------------- testler

    /** Silberschatz: 24, 3, 3 → ortalama bekleme 17. */
    private static void fcfsConvoy() {
        SimulationResult r = new FcfsScheduler().simulate(procs(new int[][] {{0, 24, 0}, {0, 3, 0}, {0, 3, 0}}));
        check("FCFS gantt", gantt(r), "P1[0-24] P2[24-27] P3[27-30]");
        check("FCFS ort. bekleme", r.averageWaitingTime(), 17.0);
        check("FCFS ort. dönüş", r.averageTurnaroundTime(), 27.0);
        check("FCFS bağlam değişimi", r.contextSwitches(), 2);
    }

    /** Silberschatz: 6, 8, 7, 3 → sıra P4, P1, P3, P2; ortalama bekleme 7. */
    private static void sjfAllArriveAtZero() {
        SimulationResult r = new SjfScheduler().simulate(
                procs(new int[][] {{0, 6, 0}, {0, 8, 0}, {0, 7, 0}, {0, 3, 0}}));
        check("SJF gantt", gantt(r), "P4[0-3] P1[3-9] P3[9-16] P2[16-24]");
        check("SJF ort. bekleme", r.averageWaitingTime(), 7.0);
    }

    /** Kesmesiz SJF: çalışan süreç, daha kısa bir süreç gelse de kesilmez. */
    private static void sjfWithArrivals() {
        SimulationResult r = new SjfScheduler().simulate(
                procs(new int[][] {{0, 8, 0}, {1, 4, 0}, {2, 9, 0}, {3, 5, 0}}));
        check("SJF (varışlı) gantt", gantt(r), "P1[0-8] P2[8-12] P4[12-17] P3[17-26]");
        check("SJF (varışlı) ort. bekleme", r.averageWaitingTime(), 7.75);
    }

    /** Silberschatz: aynı veri SRTF ile → ortalama bekleme 6.5. */
    private static void srtfTextbook() {
        SimulationResult r = new SrtfScheduler().simulate(
                procs(new int[][] {{0, 8, 0}, {1, 4, 0}, {2, 9, 0}, {3, 5, 0}}));
        check("SRTF gantt", gantt(r), "P1[0-1] P2[1-5] P4[5-10] P1[10-17] P3[17-26]");
        check("SRTF ort. bekleme", r.averageWaitingTime(), 6.5);
        check("SRTF P1 yanıt süresi", r.metrics().get(0).responseTime(), 0);
        check("SRTF P1 bekleme süresi", r.metrics().get(0).waitingTime(), 9);
    }

    /** Silberschatz: öncelikler 3, 1, 4, 5, 2 → sıra P2, P5, P1, P3, P4; ortalama bekleme 8.2. */
    private static void priorityTextbook() {
        SimulationResult r = new PriorityScheduler(false, false).simulate(
                procs(new int[][] {{0, 10, 3}, {0, 1, 1}, {0, 2, 4}, {0, 1, 5}, {0, 5, 2}}));
        check("Öncelik gantt", gantt(r), "P2[0-1] P5[1-6] P1[6-16] P3[16-18] P4[18-19]");
        check("Öncelik ort. bekleme", r.averageWaitingTime(), 8.2);
    }

    /** Öncelik yönü ters çevrildiğinde sıra da tersine döner. */
    private static void priorityHigherNumberWins() {
        SimulationResult r = new PriorityScheduler(false, true).simulate(
                procs(new int[][] {{0, 10, 3}, {0, 1, 1}, {0, 2, 4}, {0, 1, 5}, {0, 5, 2}}));
        check("Öncelik (büyük sayı) gantt", gantt(r), "P4[0-1] P3[1-3] P1[3-13] P5[13-18] P2[18-19]");
    }

    /** Daha yüksek öncelikli süreç geldiğinde çalışan süreç kesilir. */
    private static void priorityPreemptive() {
        List<SimProcess> input = procs(new int[][] {{0, 5, 3}, {1, 3, 1}, {2, 2, 2}});
        SimulationResult preemptive = new PriorityScheduler(true, false).simulate(input);
        check("Öncelik (kesmeli) gantt", gantt(preemptive), "P1[0-1] P2[1-4] P3[4-6] P1[6-10]");
        SimulationResult nonPreemptive = new PriorityScheduler(false, false).simulate(input);
        check("Öncelik (kesmesiz) gantt", gantt(nonPreemptive), "P1[0-5] P2[5-8] P3[8-10]");
    }

    /** Silberschatz: 24, 3, 3 ve q = 4 → ortalama bekleme 17/3. */
    private static void roundRobinTextbook() {
        SimulationResult r = new RoundRobinScheduler(4).simulate(
                procs(new int[][] {{0, 24, 0}, {0, 3, 0}, {0, 3, 0}}));
        check("RR gantt", gantt(r), "P1[0-4] P2[4-7] P3[7-10] P1[10-30]");
        check("RR ort. bekleme", r.averageWaitingTime(), 17.0 / 3);
        check("RR bağlam değişimi", r.contextSwitches(), 3);
    }

    /** Zaman dilimi dolduğu anda gelen süreç, kuyruğa kesilen süreçten önce girer. */
    private static void roundRobinArrivalBeforeRequeue() {
        SimulationResult r = new RoundRobinScheduler(2).simulate(
                procs(new int[][] {{0, 5, 0}, {1, 3, 0}, {2, 1, 0}}));
        check("RR (varışlı) gantt", gantt(r), "P1[0-2] P2[2-4] P3[4-5] P1[5-7] P2[7-8] P1[8-9]");
        check("RR (varışlı) P3 bitiş", r.metrics().get(2).completionTime(), 5);
    }

    /** Kuyruk boşken zaman dilimi dolarsa süreç devam eder; yeni dilim o andan başlar. */
    private static void roundRobinKeepsRunningWhenQueueEmpty() {
        SimulationResult r = new RoundRobinScheduler(2).simulate(procs(new int[][] {{0, 6, 0}, {3, 2, 0}}));
        check("RR (boş kuyruk) gantt", gantt(r), "P1[0-4] P2[4-6] P1[6-8]");
    }

    /** Süreç yokken CPU boşta kalır; kullanım oranı buna göre düşer. */
    private static void idleGaps() {
        SimulationResult r = new FcfsScheduler().simulate(procs(new int[][] {{2, 2, 0}, {6, 2, 0}}));
        check("Boşta gantt", gantt(r), "-[0-2] P1[2-4] -[4-6] P2[6-8]");
        check("Boşta CPU kullanımı", r.cpuUtilization(), 50.0);
        check("Boşta süre", r.idleTime(), 4);
        check("Boşta iş çıkarma oranı", r.throughput(), 0.25);
    }

    /** Ölçüt eşitse önce gelen, o da eşitse girdide önce yazılan süreç seçilir. */
    private static void tieBreakIsFcfs() {
        List<SimProcess> input = procs(new int[][] {{0, 3, 1}, {0, 3, 1}, {0, 3, 1}});
        check("Eşitlik SJF", gantt(new SjfScheduler().simulate(input)), "P1[0-3] P2[3-6] P3[6-9]");
        check("Eşitlik SRTF", gantt(new SrtfScheduler().simulate(input)), "P1[0-3] P2[3-6] P3[6-9]");
        check("Eşitlik öncelik", gantt(new PriorityScheduler(true, false).simulate(input)),
                "P1[0-3] P2[3-6] P3[6-9]");
    }

    /** Rastgele yüklerde her algoritma için değişmezler: süreler tutarlı, Gantt boşluksuz. */
    private static void invariantsOnRandomWorkloads() {
        Random random = new Random(42);
        boolean ok = true;
        String firstProblem = "";
        for (int round = 0; round < 300 && ok; round++) {
            List<SimProcess> input = SampleData.random(random);
            int quantum = 1 + random.nextInt(5);
            int totalBurst = input.stream().mapToInt(SimProcess::burstTime).sum();
            double srtfWaiting = new SrtfScheduler().simulate(input).averageWaitingTime();

            for (AlgorithmType type : AlgorithmType.values()) {
                Scheduler scheduler = type.create(quantum, random.nextBoolean());
                SimulationResult r = scheduler.simulate(input);
                String problem = findProblem(r, input, totalBurst, srtfWaiting);
                if (problem != null) {
                    ok = false;
                    firstProblem = scheduler.name() + " → " + problem + " | girdi: " + input;
                    break;
                }
            }
        }
        check("Rastgele yüklerde değişmezler" + (ok ? "" : " (" + firstProblem + ")"), ok, true);
    }

    private static String findProblem(
            SimulationResult r, List<SimProcess> input, int totalBurst, double srtfWaiting) {
        int cursor = 0;
        int busy = 0;
        int[] executed = new int[input.size()];
        for (GanttSegment s : r.gantt()) {
            if (s.start() != cursor || s.end() <= s.start()) {
                return "Gantt dilimleri ardışık değil";
            }
            cursor = s.end();
            if (!s.isIdle()) {
                if (s.start() < s.process().arrivalTime()) {
                    return "süreç gelmeden çalıştı";
                }
                busy += s.duration();
                executed[s.process().id()] += s.duration();
            }
        }
        if (cursor != r.totalTime() || busy != totalBurst) {
            return "toplam süre tutarsız";
        }
        for (int i = 0; i < input.size(); i++) {
            ProcessMetrics m = r.metrics().get(i);
            SimProcess p = input.get(i);
            if (executed[i] != p.burstTime()) {
                return p.name() + " çalışma süresi kadar çalışmadı";
            }
            if (m.waitingTime() < 0 || m.responseTime() < 0 || m.responseTime() > m.waitingTime()) {
                return p.name() + " bekleme/yanıt süresi tutarsız";
            }
            if (m.turnaroundTime() != m.waitingTime() + p.burstTime()
                    || m.completionTime() != p.arrivalTime() + m.turnaroundTime()) {
                return p.name() + " dönüş süresi tutarsız";
            }
        }
        if (r.averageWaitingTime() < srtfWaiting - 1e-9) {
            return "ortalama bekleme SRTF'den düşük (SRTF en iyi olmalı)";
        }
        return null;
    }

    private static void csvRoundTrip() {
        List<SimProcess> parsed = ProcessCsv.parse(List.of(
                "# yorum satırı", "ad,varis,calisma,oncelik", "A, 0, 5, 2", "B;3;1", "", "C\t4\t2\t7"));
        check("CSV süreç sayısı", parsed.size(), 3);
        check("CSV ilk süreç", parsed.get(0), new SimProcess(0, "A", 0, 5, 2));
        check("CSV önceliksiz satır", parsed.get(1), new SimProcess(1, "B", 3, 1, 0));
        check("CSV sekme ayırıcı", parsed.get(2), new SimProcess(2, "C", 4, 2, 7));

        String error = "";
        try {
            ProcessCsv.parse(List.of("A,0,0,1"));
        } catch (IllegalArgumentException e) {
            error = e.getMessage();
        }
        check("CSV geçersiz çalışma süresi reddedilir", error.startsWith("Satır 1:"), true);
    }

    // ------------------------------------------------------------- yardımcılar

    private static List<SimProcess> procs(int[][] rows) {
        return SampleData.processes(rows);
    }

    /** Gantt şemasını "P1[0-4] P2[4-7]" biçiminde tek satıra dönüştürür; boşta dilimler "-" ile gösterilir. */
    private static String gantt(SimulationResult result) {
        List<String> parts = new ArrayList<>();
        for (GanttSegment s : result.gantt()) {
            parts.add((s.isIdle() ? "-" : s.process().name()) + "[" + s.start() + "-" + s.end() + "]");
        }
        return String.join(" ", parts);
    }

    private static void check(String name, Object actual, Object expected) {
        boolean equal = actual instanceof Double a && expected instanceof Double e
                ? Math.abs(a - e) < 1e-9
                : actual.equals(expected);
        if (equal) {
            passed++;
            System.out.println("  GEÇTİ   " + name);
        } else {
            failed++;
            System.out.println("  HATALI  " + name);
            System.out.println("          beklenen: " + expected);
            System.out.println("          bulunan : " + actual);
        }
    }
}

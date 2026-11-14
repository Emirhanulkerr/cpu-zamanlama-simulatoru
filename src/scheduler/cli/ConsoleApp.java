package scheduler.cli;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

import scheduler.algorithm.AlgorithmType;
import scheduler.io.ProcessCsv;
import scheduler.io.SampleData;
import scheduler.model.Limits;
import scheduler.model.SimProcess;
import scheduler.model.SimulationResult;

/**
 * Simülatörün konsol sürümü. Süreçleri CSV dosyasından, hazır örnekten ya da
 * klavyeden alır; seçilen algoritmaların Gantt şemasını ve süreç tablosunu yazdırır.
 */
public final class ConsoleApp {

    private static final String USAGE = """
            CPU Zamanlama Simülatörü — konsol modu

            Kullanım:
              --cli                          Süreçleri klavyeden sorar (etkileşimli mod)
              --cli --ornek                  Hazır örnek veriyle çalışır
              --cli --dosya <csv>            Süreçleri CSV dosyasından okur (ad,varis,calisma,oncelik)

            Seçenekler:
              --algoritma <ad>               fcfs | sjf | srtf | oncelik | oncelik-kesmeli | rr | hepsi
                                             (varsayılan: hepsi)
              --quantum <n>                  Round Robin zaman dilimi (varsayılan: 2)
              --buyuk-oncelik                Büyük sayı = yüksek öncelik
                                             (varsayılan: küçük sayı = yüksek öncelik)
              --yardim                       Bu metni gösterir
            """;

    private ConsoleApp() {
    }

    /** @return işlem başarılıysa 0, girdi hatasında 1 */
    public static int run(String[] args) {
        PrintStream out = System.out;
        String file = null;
        String algorithm = null;
        int quantum = 2;
        boolean higherNumberIsHigherPriority = false;
        boolean useSample = false;

        try {
            for (int i = 0; i < args.length; i++) {
                switch (args[i]) {
                    case "--dosya" -> file = valueOf(args, ++i, "--dosya");
                    case "--algoritma" -> algorithm = valueOf(args, ++i, "--algoritma");
                    case "--quantum" -> quantum = parseQuantum(valueOf(args, ++i, "--quantum"));
                    case "--buyuk-oncelik" -> higherNumberIsHigherPriority = true;
                    case "--ornek" -> useSample = true;
                    case "--yardim", "--help", "-h" -> {
                        out.println(USAGE);
                        return 0;
                    }
                    default -> throw new IllegalArgumentException("Bilinmeyen seçenek: " + args[i]);
                }
            }

            List<SimProcess> processes;
            if (file != null) {
                processes = ProcessCsv.read(Path.of(file));
            } else if (useSample) {
                processes = SampleData.all().get(0).processes();
            } else {
                Scanner scanner = new Scanner(System.in);
                processes = askProcesses(scanner, out);
                if (algorithm == null) {
                    algorithm = askAlgorithm(scanner, out);
                    if (algorithm.equals("rr") || algorithm.equals("hepsi")) {
                        quantum = askInt(scanner, out, "Round Robin zaman dilimi (quantum)", 1, Limits.MAX_QUANTUM);
                    }
                }
            }

            List<AlgorithmType> types = resolveAlgorithms(algorithm == null ? "hepsi" : algorithm);
            List<SimulationResult> results = new ArrayList<>();
            for (AlgorithmType type : types) {
                results.add(type.create(quantum, higherNumberIsHigherPriority).simulate(processes));
            }

            out.println();
            for (SimulationResult result : results) {
                out.println(ConsoleReport.render(result));
            }
            if (results.size() > 1) {
                out.println(ConsoleReport.comparison(results));
            }
            return 0;
        } catch (NoSuchFileException e) {
            System.err.println("Hata: dosya bulunamadı: " + e.getFile());
            return 1;
        } catch (IllegalArgumentException | IOException e) {
            System.err.println("Hata: " + e.getMessage());
            System.err.println("Kullanım için: --cli --yardim");
            return 1;
        }
    }

    private static List<AlgorithmType> resolveAlgorithms(String name) {
        return switch (name.toLowerCase(Locale.ROOT)) {
            case "fcfs" -> List.of(AlgorithmType.FCFS);
            case "sjf" -> List.of(AlgorithmType.SJF);
            case "srtf" -> List.of(AlgorithmType.SRTF);
            case "oncelik" -> List.of(AlgorithmType.PRIORITY);
            case "oncelik-kesmeli" -> List.of(AlgorithmType.PRIORITY_PREEMPTIVE);
            case "rr" -> List.of(AlgorithmType.ROUND_ROBIN);
            case "hepsi" -> List.of(AlgorithmType.values());
            default -> throw new IllegalArgumentException("Bilinmeyen algoritma: " + name
                    + " (fcfs | sjf | srtf | oncelik | oncelik-kesmeli | rr | hepsi)");
        };
    }

    private static List<SimProcess> askProcesses(Scanner scanner, PrintStream out) {
        out.println("CPU Zamanlama Simülatörü — etkileşimli mod");
        out.println("(Öncelikte küçük sayı yüksek önceliktir.)");
        out.println();
        int count = askInt(scanner, out, "Süreç sayısı", 1, Limits.MAX_PROCESSES);
        List<SimProcess> processes = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            String name = "P" + (i + 1);
            out.println();
            int arrival = askInt(scanner, out, name + " varış zamanı", 0, Limits.MAX_ARRIVAL);
            int burst = askInt(scanner, out, name + " çalışma süresi", 1, Limits.MAX_BURST);
            int priority = askInt(scanner, out, name + " öncelik", 0, Limits.MAX_PRIORITY);
            processes.add(new SimProcess(i, name, arrival, burst, priority));
        }
        return processes;
    }

    private static String askAlgorithm(Scanner scanner, PrintStream out) {
        String[] keys = {"fcfs", "sjf", "srtf", "oncelik", "oncelik-kesmeli", "rr", "hepsi"};
        out.println();
        out.println("Algoritma seçin:");
        AlgorithmType[] types = AlgorithmType.values();
        for (int i = 0; i < types.length; i++) {
            out.println("  " + (i + 1) + ") " + types[i].displayName());
        }
        out.println("  " + keys.length + ") Hepsini çalıştır ve karşılaştır");
        return keys[askInt(scanner, out, "Seçiminiz", 1, keys.length) - 1];
    }

    /** Geçerli bir tam sayı girilene kadar sormaya devam eder. */
    private static int askInt(Scanner scanner, PrintStream out, String prompt, int min, int max) {
        while (true) {
            out.print(prompt + " [" + min + "-" + max + "]: ");
            out.flush();
            if (!scanner.hasNextLine()) {
                throw new IllegalArgumentException("Girdi beklenirken akış sona erdi.");
            }
            String line = scanner.nextLine().trim();
            try {
                int value = Integer.parseInt(line);
                if (value >= min && value <= max) {
                    return value;
                }
            } catch (NumberFormatException ignored) {
                // aşağıdaki uyarıyla yeniden sorulur
            }
            out.println("  Lütfen " + min + " ile " + max + " arasında bir tam sayı girin.");
        }
    }

    private static String valueOf(String[] args, int index, String option) {
        if (index >= args.length) {
            throw new IllegalArgumentException(option + " seçeneği bir değer bekliyor.");
        }
        return args[index];
    }

    private static int parseQuantum(String text) {
        try {
            int quantum = Integer.parseInt(text);
            if (quantum >= 1 && quantum <= Limits.MAX_QUANTUM) {
                return quantum;
            }
        } catch (NumberFormatException ignored) {
            // aşağıda hata fırlatılır
        }
        throw new IllegalArgumentException("--quantum 1 ile " + Limits.MAX_QUANTUM + " arasında olmalıdır: " + text);
    }
}

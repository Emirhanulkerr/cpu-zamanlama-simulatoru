package scheduler.io;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import scheduler.model.Limits;
import scheduler.model.SimProcess;

/**
 * Süreç listesini CSV dosyasından okur ve CSV dosyasına yazar.
 *
 * <p>Biçim: her satırda {@code ad,varis,calisma,oncelik}. Ayırıcı olarak virgül, noktalı virgül
 * ya da sekme kullanılabilir. Öncelik sütunu isteğe bağlıdır (yoksa 0 alınır).
 * Boş satırlar, {@code #} ile başlayan satırlar ve başlık satırı yok sayılır.
 */
public final class ProcessCsv {

    private static final String HEADER = "ad,varis,calisma,oncelik";

    private ProcessCsv() {
    }

    public static List<SimProcess> read(Path file) throws IOException {
        return parse(Files.readAllLines(file, StandardCharsets.UTF_8));
    }

    /**
     * @throws IllegalArgumentException satırlardan biri geçersizse (mesajda satır numarası yer alır)
     */
    public static List<SimProcess> parse(List<String> lines) {
        List<SimProcess> processes = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).replace("﻿", "").trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            String[] fields = line.split("\\s*[,;\\t]\\s*");
            if (fields.length < 3) {
                throw new IllegalArgumentException(
                        "Satır " + (i + 1) + ": en az 3 alan bekleniyor (ad, varış, çalışma[, öncelik]).");
            }
            if (processes.isEmpty() && !isInteger(fields[1])) {
                continue;  // başlık satırı
            }
            try {
                String name = fields[0];
                int arrival = parseInRange(fields[1], "varış zamanı", 0, Limits.MAX_ARRIVAL);
                int burst = parseInRange(fields[2], "çalışma süresi", 1, Limits.MAX_BURST);
                int priority = fields.length > 3 ? parseInRange(fields[3], "öncelik", 0, Limits.MAX_PRIORITY) : 0;
                processes.add(new SimProcess(processes.size(), name, arrival, burst, priority));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Satır " + (i + 1) + ": " + e.getMessage(), e);
            }
        }
        if (processes.isEmpty()) {
            throw new IllegalArgumentException("Dosyada hiç süreç bulunamadı.");
        }
        if (processes.size() > Limits.MAX_PROCESSES) {
            throw new IllegalArgumentException("En fazla " + Limits.MAX_PROCESSES + " süreç desteklenir.");
        }
        return processes;
    }

    public static void write(Path file, List<SimProcess> processes) throws IOException {
        List<String> lines = new ArrayList<>();
        lines.add(HEADER);
        for (SimProcess p : processes) {
            lines.add(p.name() + "," + p.arrivalTime() + "," + p.burstTime() + "," + p.priority());
        }
        Files.write(file, lines, StandardCharsets.UTF_8);
    }

    private static int parseInRange(String text, String what, int min, int max) {
        if (!isInteger(text)) {
            throw new IllegalArgumentException(what + " tam sayı olmalıdır: \"" + text + "\"");
        }
        long value = Long.parseLong(text);
        if (value < min || value > max) {
            throw new IllegalArgumentException(what + " " + min + " ile " + max + " arasında olmalıdır: " + text);
        }
        return (int) value;
    }

    private static boolean isInteger(String text) {
        return text.matches("-?\\d{1,9}");
    }
}

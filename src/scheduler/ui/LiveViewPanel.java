package scheduler.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.swing.JComponent;

import scheduler.model.ProcessMetrics;
import scheduler.model.ProcessState;
import scheduler.model.SimProcess;
import scheduler.model.SimulationResult;
import scheduler.model.TickSnapshot;

/**
 * Seçili andaki sistem durumunu dört şerit hâlinde gösterir:
 * henüz gelmeyen süreçler → hazır kuyruğu → CPU → tamamlanan süreçler.
 * Animasyon ilerledikçe süreç kutuları şeritler arasında yer değiştirir.
 */
final class LiveViewPanel extends JComponent {

    private static final int TITLE_HEIGHT = 20;
    private static final int ARROW_WIDTH = 26;
    private static final int CPU_WIDTH = 132;
    private static final int LANE_PADDING = 8;
    private static final int CHIP_GAP = 6;
    /** Süreç kutusu boyutları {genişlik, yükseklik}; şeride sığan ilk boyut kullanılır. */
    private static final int[][] CHIP_SIZES = {{54, 42}, {54, 33}, {46, 24}};

    /** Bir şeritteki süreç kutusu: süreç ve altında yazan kısa bilgi. */
    private record Chip(SimProcess process, String detail) {
    }

    private SimulationResult result;
    private int time;

    LiveViewPanel() {
        setOpaque(false);
    }

    void setState(SimulationResult result, int time) {
        if (result == this.result && time == this.time) {
            return;
        }
        this.result = result;
        this.time = time;
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(400, 108);
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        Theme.prepare(g);

        List<Chip> notArrived = new ArrayList<>();
        List<Chip> ready = new ArrayList<>();
        List<Chip> finished = new ArrayList<>();
        Chip running = null;

        if (result != null) {
            TickSnapshot snapshot = result.snapshotAt(time);
            List<SimProcess> processes = result.processes();
            for (int i = 0; i < processes.size(); i++) {
                SimProcess p = processes.get(i);
                ProcessState state = result.stateAt(i, time);
                if (state == ProcessState.NOT_ARRIVED) {
                    notArrived.add(new Chip(p, "varış " + p.arrivalTime()));
                } else if (state == ProcessState.FINISHED) {
                    finished.add(new Chip(p, "bitiş " + result.metrics().get(i).completionTime()));
                } else if (state == ProcessState.RUNNING) {
                    running = new Chip(p, "kalan " + snapshot.remainingOf(i));
                }
            }
            // Hazır kuyruğu anlık görüntüdeki sırayla: ilk eleman bir sonraki çalışacak süreç.
            for (SimProcess p : snapshot.readyQueue()) {
                ready.add(new Chip(p, "kalan " + snapshot.remainingOf(processes.indexOf(p))));
            }
            notArrived.sort(Comparator.comparingInt(chip -> chip.process().arrivalTime()));
            List<ProcessMetrics> metrics = result.metrics();
            finished.sort(Comparator.comparingInt(
                    chip -> metrics.get(processes.indexOf(chip.process())).completionTime()));
        }

        int width = getWidth();
        int height = getHeight();
        int flexible = width - 3 * ARROW_WIDTH - CPU_WIDTH;
        int notArrivedWidth = (int) (flexible * 0.27);
        int finishedWidth = (int) (flexible * 0.31);
        int readyWidth = flexible - notArrivedWidth - finishedWidth;

        int x = 0;
        Rectangle notArrivedLane = new Rectangle(x, 0, notArrivedWidth, height);
        x += notArrivedWidth + ARROW_WIDTH;
        Rectangle readyLane = new Rectangle(x, 0, readyWidth, height);
        x += readyWidth + ARROW_WIDTH;
        Rectangle cpuLane = new Rectangle(x, 0, CPU_WIDTH, height);
        x += CPU_WIDTH + ARROW_WIDTH;
        Rectangle finishedLane = new Rectangle(x, 0, width - x, height);

        paintLane(g, notArrivedLane, "HENÜZ GELMEDİ", notArrived.size());
        paintLane(g, readyLane, "HAZIR KUYRUĞU", ready.size());
        paintLane(g, cpuLane, "CPU", -1);
        paintLane(g, finishedLane, "TAMAMLANDI", finished.size());

        paintArrow(g, notArrivedLane.x + notArrivedLane.width, height);
        paintArrow(g, readyLane.x + readyLane.width, height);
        paintArrow(g, cpuLane.x + cpuLane.width, height);

        paintChips(g, notArrivedLane, notArrived, false);
        paintChips(g, readyLane, ready, true);   // kuyruğun başı CPU'ya en yakın (sağda) durur
        paintChips(g, finishedLane, finished, false);
        paintCpu(g, cpuLane, running);
        g.dispose();
    }

    private void paintLane(Graphics2D g, Rectangle lane, String title, int count) {
        g.setFont(Theme.font(Font.BOLD, 10.5f));
        g.setColor(Theme.TEXT_SECONDARY);
        g.drawString(count > 0 ? title + "  ·  " + count : title, lane.x + 2, 12);

        RoundRectangle2D box = new RoundRectangle2D.Double(
                lane.x + 0.5, TITLE_HEIGHT + 0.5, lane.width - 1, lane.height - TITLE_HEIGHT - 1, 12, 12);
        g.setColor(Theme.SURFACE_ALT);
        g.fill(box);
        g.setColor(Theme.GRID);
        g.draw(box);
    }

    /** İki şerit arasındaki akış yönünü gösteren ok. */
    private void paintArrow(Graphics2D g, int x, int height) {
        double centerX = x + ARROW_WIDTH / 2.0;
        double centerY = TITLE_HEIGHT + (height - TITLE_HEIGHT) / 2.0;
        Path2D arrow = new Path2D.Double();
        arrow.moveTo(centerX - 4, centerY - 6);
        arrow.lineTo(centerX + 3, centerY);
        arrow.lineTo(centerX - 4, centerY + 6);
        g.setColor(Theme.AXIS);
        g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(arrow);
    }

    /**
     * Kutuları şeridin içine satır satır dizer. Sığmıyorsa önce küçük kutulara geçer,
     * yine sığmıyorsa sığanları çizip kalanların sayısını "+N" olarak yazar.
     */
    private void paintChips(Graphics2D g, Rectangle lane, List<Chip> chips, boolean fromRight) {
        int innerX = lane.x + LANE_PADDING;
        int innerY = TITLE_HEIGHT + LANE_PADDING;
        int innerWidth = lane.width - 2 * LANE_PADDING;
        int innerHeight = lane.height - TITLE_HEIGHT - 2 * LANE_PADDING;

        if (chips.isEmpty()) {
            g.setFont(Theme.font(Font.PLAIN, 12f));
            g.setColor(Theme.AXIS);
            FontMetrics metrics = g.getFontMetrics();
            g.drawString("—", lane.x + (lane.width - metrics.stringWidth("—")) / 2,
                    innerY + (innerHeight - metrics.getHeight()) / 2 + metrics.getAscent());
            return;
        }

        // Kutu boyutu kademeli küçülür: tek satır büyük kutu → iki satır alçak kutu → yalnızca adı yazan küçük kutu.
        int tier = 0;
        while (tier < CHIP_SIZES.length - 1
                && capacity(innerWidth, innerHeight, CHIP_SIZES[tier][0], CHIP_SIZES[tier][1]) < chips.size()) {
            tier++;
        }
        int chipWidth = CHIP_SIZES[tier][0];
        int chipHeight = CHIP_SIZES[tier][1];
        boolean compact = tier == CHIP_SIZES.length - 1;
        int columns = Math.max(1, (innerWidth + CHIP_GAP) / (chipWidth + CHIP_GAP));
        int capacity = Math.max(1, capacity(innerWidth, innerHeight, chipWidth, chipHeight));
        int shown = chips.size() <= capacity ? chips.size() : capacity - 1;

        // Satırlar şeridin içinde dikeyde ortalanır.
        int rows = (int) Math.ceil(Math.min(chips.size(), capacity) / (double) columns);
        int startY = innerY + (innerHeight - (rows * chipHeight + (rows - 1) * CHIP_GAP)) / 2;

        for (int i = 0; i <= shown && i < capacity; i++) {
            int column = i % columns;
            int row = i / columns;
            int chipX = fromRight
                    ? innerX + innerWidth - (column + 1) * chipWidth - column * CHIP_GAP
                    : innerX + column * (chipWidth + CHIP_GAP);
            int chipY = startY + row * (chipHeight + CHIP_GAP);
            if (i < shown) {
                Chip chip = chips.get(i);
                paintChip(g, chip.process(), compact ? null : chip.detail(), chipX, chipY, chipWidth, chipHeight);
            } else if (shown < chips.size()) {
                g.setFont(Theme.font(Font.BOLD, 11.5f));
                g.setColor(Theme.TEXT_SECONDARY);
                String more = "+" + (chips.size() - shown);
                FontMetrics metrics = g.getFontMetrics();
                g.drawString(more, chipX + (chipWidth - metrics.stringWidth(more)) / 2,
                        chipY + (chipHeight - metrics.getHeight()) / 2 + metrics.getAscent());
            }
        }
    }

    private static int capacity(int width, int height, int chipWidth, int chipHeight) {
        int columns = (width + CHIP_GAP) / (chipWidth + CHIP_GAP);
        int rows = (height + CHIP_GAP) / (chipHeight + CHIP_GAP);
        return columns * rows;
    }

    private void paintCpu(Graphics2D g, Rectangle lane, Chip running) {
        int boxWidth = lane.width - 2 * 14;
        int boxHeight = Math.min(56, lane.height - TITLE_HEIGHT - 2 * LANE_PADDING);
        int boxX = lane.x + 14;
        int boxY = TITLE_HEIGHT + (lane.height - TITLE_HEIGHT - boxHeight) / 2;

        if (running != null) {
            paintChip(g, running.process(), running.detail(), boxX, boxY, boxWidth, boxHeight);
            return;
        }
        RoundRectangle2D box = new RoundRectangle2D.Double(boxX + 0.5, boxY + 0.5, boxWidth - 1, boxHeight - 1, 10, 10);
        g.setColor(Theme.AXIS);
        g.setStroke(new BasicStroke(1.2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, new float[] {4f, 4f}, 0f));
        g.draw(box);
        g.setFont(Theme.font(Font.PLAIN, 12f));
        g.setColor(Theme.TEXT_MUTED);
        FontMetrics metrics = g.getFontMetrics();
        g.drawString("Boşta", boxX + (boxWidth - metrics.stringWidth("Boşta")) / 2,
                boxY + (boxHeight - metrics.getHeight()) / 2 + metrics.getAscent());
    }

    /** Süreç renginde yuvarlak köşeli kutu; üstte süreç adı, altta (varsa) kısa bilgi. */
    private void paintChip(Graphics2D g, SimProcess process, String detail, int x, int y, int width, int height) {
        Color color = Theme.processColor(process.id());
        Color textColor = Theme.labelOn(color);
        g.setColor(color);
        g.fill(new RoundRectangle2D.Double(x, y, width, height, 10, 10));

        boolean large = height >= 50;
        g.setFont(Theme.font(Font.BOLD, large ? 15f : 12.5f));
        FontMetrics nameMetrics = g.getFontMetrics();
        String name = Theme.ellipsize(process.name(), nameMetrics, width - 8);
        int nameX = x + (width - nameMetrics.stringWidth(name)) / 2;

        g.setColor(textColor);
        if (detail == null) {
            g.drawString(name, nameX, y + (height - nameMetrics.getHeight()) / 2 + nameMetrics.getAscent());
            return;
        }
        Font detailFont = Theme.font(Font.PLAIN, large ? 11.5f : 10.5f);
        FontMetrics detailMetrics = g.getFontMetrics(detailFont);
        int blockHeight = nameMetrics.getHeight() + detailMetrics.getHeight() - 2;
        int top = y + (height - blockHeight) / 2;
        g.drawString(name, nameX, top + nameMetrics.getAscent());

        g.setFont(detailFont);
        g.setColor(Theme.withAlpha(textColor, 225));
        String shortDetail = Theme.ellipsize(detail, detailMetrics, width - 6);
        g.drawString(shortDetail, x + (width - detailMetrics.stringWidth(shortDetail)) / 2,
                top + nameMetrics.getHeight() - 2 + detailMetrics.getAscent());
    }
}

package scheduler.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.Timer;

import scheduler.model.SimulationResult;

/**
 * Seçili algoritmanın simülasyon görünümü: oynatma kontrolleri, canlı durum,
 * Gantt şeması, özet ölçümler ve süreç tablosu.
 */
final class SimulationPanel extends JPanel {

    /** 1× hızda saniyede oynatılan zaman birimi. */
    private static final double UNITS_PER_SECOND = 2.5;
    /** Uzun simülasyonlar 1× hızda en fazla bu kadar saniye sürecek şekilde hızlandırılır. */
    private static final double MAX_DURATION_SECONDS = 24;
    private static final String[] SPEED_LABELS = {"0,5×", "1×", "2×", "4×"};
    private static final double[] SPEEDS = {0.5, 1, 2, 4};

    private final GanttChartPanel gantt = new GanttChartPanel();
    private final LiveViewPanel liveView = new LiveViewPanel();
    private final ResultsTableModel resultsModel = new ResultsTableModel();
    private final JLabel algorithmLabel = new JLabel();

    private final MetricTile waitingTile = new MetricTile("Ort. bekleme süresi", "birim",
            "Süreçlerin hazır kuyruğunda geçirdiği ortalama süre");
    private final MetricTile turnaroundTile = new MetricTile("Ort. dönüş süresi", "birim",
            "Varıştan tamamlanmaya kadar geçen ortalama süre");
    private final MetricTile responseTile = new MetricTile("Ort. yanıt süresi", "birim",
            "Varıştan CPU'yu ilk almaya kadar geçen ortalama süre");
    private final MetricTile utilizationTile = new MetricTile("CPU kullanımı", "",
            "CPU'nun dolu olduğu sürenin toplam süreye oranı");
    private final MetricTile throughputTile = new MetricTile("İş çıkarma oranı", "süreç/birim",
            "Birim zamanda tamamlanan süreç sayısı (throughput)");
    private final MetricTile switchTile = new MetricTile("Bağlam değişimi", "kez",
            "CPU'nun bir süreçten farklı bir sürece geçme sayısı");

    private final Icon playIcon = Icons.of(Icons.Shape.PLAY, Theme.SURFACE);
    private final Icon pauseIcon = Icons.of(Icons.Shape.PAUSE, Theme.SURFACE);
    private final FlatButton playButton = new FlatButton("", playIcon, FlatButton.Kind.PRIMARY);
    private final FlatButton toStartButton = iconButton(Icons.Shape.TO_START, "Başa dön");
    private final FlatButton stepBackButton = iconButton(Icons.Shape.STEP_BACK, "Bir birim geri");
    private final FlatButton stepForwardButton = iconButton(Icons.Shape.STEP_FORWARD, "Bir birim ileri");
    private final FlatButton toEndButton = iconButton(Icons.Shape.TO_END, "Sona git");
    private final JSlider slider = new JSlider(0, 1, 0);
    private final JLabel timeLabel = new JLabel("", SwingConstants.RIGHT);
    private final JComboBox<String> speedCombo = new JComboBox<>(SPEED_LABELS);

    private final Timer timer = new Timer(16, e -> advance());

    private SimulationResult result;
    private double playTime;
    private long lastTickNanos;
    private boolean updatingSlider;

    SimulationPanel() {
        super(new VStackLayout(10));
        setOpaque(false);

        add(buildControls());
        add(new Card().content(liveView));

        algorithmLabel.setFont(Theme.font(Font.PLAIN, 12f));
        algorithmLabel.setForeground(Theme.TEXT_SECONDARY);
        add(new Card("Gantt Şeması").headerRight(algorithmLabel).content(gantt));

        JPanel tiles = new JPanel(new GridLayout(1, 6, 10, 0));
        tiles.setOpaque(false);
        for (MetricTile tile : new MetricTile[] {
            waitingTile, turnaroundTile, responseTile, utilizationTile, throughputTile, switchTile}) {
            tiles.add(tile);
        }
        add(tiles);
        add(buildResultsCard(), VStackLayout.FILL);

        gantt.onSeek(time -> {
            pause();
            setPlayTime(time);
        });
        showResult(null, false);
    }

    /**
     * Yeni bir simülasyon sonucunu gösterir.
     *
     * @param result  gösterilecek sonuç; süreç yoksa {@code null}
     * @param animate {@code true} ise baştan oynatılır, değilse doğrudan son durum gösterilir
     */
    void showResult(SimulationResult result, boolean animate) {
        timer.stop();
        this.result = result;

        gantt.setResult(result);
        resultsModel.setResult(result);
        algorithmLabel.setText(result == null ? "" : result.algorithmName());
        updateTiles();

        boolean hasResult = result != null;
        for (Component control : new Component[] {
            playButton, toStartButton, stepBackButton, stepForwardButton, toEndButton, slider}) {
            control.setEnabled(hasResult);
        }

        updatingSlider = true;
        slider.setMaximum(hasResult ? result.totalTime() : 1);
        updatingSlider = false;

        if (!hasResult) {
            playTime = 0;
            liveView.setState(null, 0);
            timeLabel.setText("");
            playButton.setIcon(playIcon);
            return;
        }
        if (animate) {
            setPlayTime(0);
            play();
        } else {
            setPlayTime(result.totalTime());
        }
    }

    // ------------------------------------------------------------- oynatma

    private void play() {
        if (result == null) {
            return;
        }
        if (playTime >= result.totalTime()) {
            setPlayTime(0);  // sondayken oynat: baştan başla
        }
        lastTickNanos = System.nanoTime();
        timer.start();
        playButton.setIcon(pauseIcon);
        playButton.setToolTipText("Duraklat");
    }

    private void pause() {
        timer.stop();
        playButton.setIcon(playIcon);
        playButton.setToolTipText("Oynat");
    }

    /** Zamanlayıcının her tıklamasında, geçen gerçek süre kadar simülasyon zamanını ilerletir. */
    private void advance() {
        long now = System.nanoTime();
        double elapsedSeconds = (now - lastTickNanos) / 1e9;
        lastTickNanos = now;

        double unitsPerSecond = Math.max(UNITS_PER_SECOND, result.totalTime() / MAX_DURATION_SECONDS)
                * SPEEDS[speedCombo.getSelectedIndex()];
        setPlayTime(playTime + elapsedSeconds * unitsPerSecond);
        if (playTime >= result.totalTime()) {
            pause();
        }
    }

    private void setPlayTime(double time) {
        playTime = Math.max(0, Math.min(result.totalTime(), time));
        int wholeTime = (int) Math.floor(playTime);

        gantt.setPlayTime(playTime);
        liveView.setState(result, wholeTime);
        timeLabel.setText("t = " + wholeTime + " / " + result.totalTime());

        updatingSlider = true;
        slider.setValue(wholeTime);
        updatingSlider = false;
    }

    // ------------------------------------------------------------ arayüz

    private Card buildControls() {
        playButton.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        playButton.setToolTipText("Oynat");
        playButton.addActionListener(e -> {
            if (timer.isRunning()) {
                pause();
            } else {
                play();
            }
        });
        toStartButton.addActionListener(e -> {
            pause();
            setPlayTime(0);
        });
        stepBackButton.addActionListener(e -> {
            pause();
            setPlayTime(Math.ceil(playTime) - 1);
        });
        stepForwardButton.addActionListener(e -> {
            pause();
            setPlayTime(Math.floor(playTime) + 1);
        });
        toEndButton.addActionListener(e -> {
            pause();
            setPlayTime(result.totalTime());
        });

        slider.setOpaque(false);
        slider.setToolTipText("Zamanda gezinmek için sürükleyin");
        slider.addChangeListener(e -> {
            if (!updatingSlider && result != null) {
                pause();
                setPlayTime(slider.getValue());
            }
        });

        timeLabel.setFont(Theme.font(Font.BOLD, 12.5f));
        timeLabel.setForeground(Theme.TEXT);
        // Etiketin genişliği sabit tutulur ki sayı değiştikçe yanındaki bileşenler oynamasın.
        int labelWidth = timeLabel.getFontMetrics(timeLabel.getFont()).stringWidth("t = 0000 / 0000");
        fixSize(timeLabel, new Dimension(labelWidth, 24));

        JLabel speedLabel = new JLabel("Hız");
        speedLabel.setFont(Theme.font(Font.PLAIN, 12f));
        speedLabel.setForeground(Theme.TEXT_SECONDARY);
        speedCombo.setSelectedIndex(1);
        speedCombo.setFont(Theme.font(Font.PLAIN, 12f));
        speedCombo.setToolTipText("Oynatma hızı");
        fixSize(speedCombo, speedCombo.getPreferredSize());

        Box buttons = Box.createHorizontalBox();
        buttons.add(toStartButton);
        buttons.add(stepBackButton);
        buttons.add(Box.createHorizontalStrut(4));
        buttons.add(playButton);
        buttons.add(Box.createHorizontalStrut(4));
        buttons.add(stepForwardButton);
        buttons.add(toEndButton);

        Box right = Box.createHorizontalBox();
        right.add(timeLabel);
        right.add(Box.createHorizontalStrut(16));
        right.add(speedLabel);
        right.add(Box.createHorizontalStrut(6));
        right.add(speedCombo);

        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);
        row.add(buttons, BorderLayout.WEST);
        row.add(slider, BorderLayout.CENTER);
        row.add(right, BorderLayout.EAST);

        Card card = new Card().content(row);
        card.setBorder(BorderFactory.createEmptyBorder(9, 10, 9, 14));
        return card;
    }

    private Card buildResultsCard() {
        JTable table = new JTable(resultsModel);
        Tables.style(table, ResultsTableModel.TOOLTIPS);
        table.setRowSelectionAllowed(false);
        table.setFocusable(false);

        // Ortalama satırı kalın yazılır; süreç satırlarında adın yanında renk noktası bulunur.
        Font bold = Theme.font(Font.BOLD, 13f);
        Tables.CellRenderer numbers = new Tables.CellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, column);
                if (resultsModel.isAverageRow(row)) {
                    setFont(bold);
                    setBackground(Theme.SURFACE_ALT);
                }
                return this;
            }
        };
        Tables.ProcessNameRenderer names = new Tables.ProcessNameRenderer(
                row -> resultsModel.isAverageRow(row) ? null : Theme.processColor(row)) {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, column);
                if (resultsModel.isAverageRow(row)) {
                    setFont(bold);
                    setBackground(Theme.SURFACE_ALT);
                }
                return this;
            }
        };
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(i == 0 ? names : numbers);
        }

        JLabel formulas = new JLabel(
                "Dönüş = Bitiş − Varış   ·   Bekleme = Dönüş − Çalışma   ·   Yanıt = Başlangıç − Varış");
        formulas.setFont(Theme.font(Font.PLAIN, 11.5f));
        formulas.setForeground(Theme.TEXT_MUTED);
        return new Card("Süreç Tablosu").headerRight(formulas).content(Tables.scroll(table));
    }

    private void updateTiles() {
        if (result == null) {
            for (MetricTile tile : new MetricTile[] {
                waitingTile, turnaroundTile, responseTile, utilizationTile, throughputTile, switchTile}) {
                tile.setValue("—");
            }
            return;
        }
        waitingTile.setValue(Theme.number(result.averageWaitingTime()));
        turnaroundTile.setValue(Theme.number(result.averageTurnaroundTime()));
        responseTile.setValue(Theme.number(result.averageResponseTime()));
        utilizationTile.setValue("%" + String.format(Theme.TR, "%.1f", result.cpuUtilization()));
        throughputTile.setValue(Theme.number(result.throughput()));
        switchTile.setValue(String.valueOf(result.contextSwitches()));
    }

    private static FlatButton iconButton(Icons.Shape shape, String tooltip) {
        FlatButton button = new FlatButton("", Icons.of(shape, Theme.TEXT_SECONDARY), FlatButton.Kind.GHOST);
        button.setBorder(BorderFactory.createEmptyBorder(8, 9, 8, 9));
        button.setToolTipText(tooltip);
        return button;
    }

    private static void fixSize(Component component, Dimension size) {
        component.setPreferredSize(size);
        component.setMinimumSize(size);
        component.setMaximumSize(size);
    }
}

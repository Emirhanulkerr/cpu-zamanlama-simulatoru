package scheduler.ui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import scheduler.algorithm.AlgorithmType;
import scheduler.model.SimProcess;
import scheduler.model.SimulationResult;

/**
 * Uygulamanın ana içeriği: üstte başlık çubuğu, solda girdi paneli,
 * sağda simülasyon ya da karşılaştırma görünümü.
 */
public final class MainPanel extends JPanel {

    private static final String SIMULATION_VIEW = "simulation";
    private static final String COMPARISON_VIEW = "comparison";

    private final InputPanel input = new InputPanel();
    private final SimulationPanel simulation = new SimulationPanel();
    private final ComparisonPanel comparison = new ComparisonPanel();
    private final CardLayout viewLayout = new CardLayout();
    private final JPanel views = new JPanel(viewLayout);
    private final PillToggle simulationTab = new PillToggle("Simülasyon");
    private final PillToggle comparisonTab = new PillToggle("Karşılaştırma");

    public MainPanel() {
        super(new BorderLayout());
        setBackground(Theme.PAGE);

        views.setOpaque(false);
        views.add(simulation, SIMULATION_VIEW);
        views.add(comparison, COMPARISON_VIEW);

        JPanel body = new JPanel(new BorderLayout(12, 0));
        body.setOpaque(false);
        body.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        body.add(input, BorderLayout.WEST);
        body.add(views, BorderLayout.CENTER);

        add(buildHeader(), BorderLayout.NORTH);
        add(body, BorderLayout.CENTER);

        // Girdi her değiştiğinde sonuçlar anında güncellenir; düğme ise animasyonu baştan oynatır.
        input.onChange(() -> recompute(false));
        input.onRun(this::runSimulation);
        recompute(false);
    }

    private void runSimulation() {
        if (input.processes().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Simülasyonu başlatmak için tabloya en az bir süreç ekleyin.",
                    "Süreç tablosu boş", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        simulationTab.setSelected(true);
        viewLayout.show(views, SIMULATION_VIEW);
        recompute(true);
    }

    /** Seçili algoritmayı ve karşılaştırma için tüm algoritmaları mevcut girdiyle yeniden çalıştırır. */
    private void recompute(boolean animate) {
        List<SimProcess> processes = input.processes();
        if (processes.isEmpty()) {
            simulation.showResult(null, false);
            comparison.showResults(List.of());
            return;
        }
        int quantum = input.quantum();
        boolean higherNumberIsHigherPriority = input.higherNumberIsHigherPriority();

        simulation.showResult(
                input.algorithm().create(quantum, higherNumberIsHigherPriority).simulate(processes), animate);

        List<SimulationResult> all = new ArrayList<>();
        for (AlgorithmType type : AlgorithmType.values()) {
            all.add(type.create(quantum, higherNumberIsHigherPriority).simulate(processes));
        }
        comparison.showResults(all);
    }

    private JPanel buildHeader() {
        JLabel logo = new JLabel(Icons.logo());
        JLabel title = new JLabel("CPU Zamanlama Simülatörü");
        title.setFont(Theme.font(Font.BOLD, 17f));
        title.setForeground(Color.WHITE);
        JLabel subtitle = new JLabel("Süreç zamanlama algoritmalarının adım adım görselleştirilmesi");
        subtitle.setFont(Theme.font(Font.PLAIN, 12f));
        subtitle.setForeground(Theme.withAlpha(Color.WHITE, 175));

        JPanel titles = new JPanel(new GridLayout(2, 1, 0, 1));
        titles.setOpaque(false);
        titles.add(title);
        titles.add(subtitle);

        ButtonGroup group = new ButtonGroup();
        group.add(simulationTab);
        group.add(comparisonTab);
        simulationTab.setSelected(true);
        simulationTab.addActionListener(e -> viewLayout.show(views, SIMULATION_VIEW));
        comparisonTab.addActionListener(e -> viewLayout.show(views, COMPARISON_VIEW));

        // Görünüm seçici: yarı saydam bir kapsül içinde iki hap düğme.
        JPanel tabs = new JPanel(new GridLayout(1, 2, 2, 0)) {
            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D g = (Graphics2D) graphics.create();
                Theme.prepare(g);
                g.setColor(Theme.withAlpha(Color.WHITE, 26));
                g.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), getHeight(), getHeight()));
                g.dispose();
            }
        };
        tabs.setOpaque(false);
        tabs.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
        tabs.add(simulationTab);
        tabs.add(comparisonTab);

        Box left = Box.createHorizontalBox();
        left.add(logo);
        left.add(Box.createHorizontalStrut(12));
        left.add(titles);

        Box right = Box.createVerticalBox();
        right.add(Box.createVerticalGlue());
        right.add(tabs);
        right.add(Box.createVerticalGlue());

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.HEADER);
        header.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
        header.add(left, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);
        return header;
    }
}

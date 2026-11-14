package scheduler.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;

import scheduler.algorithm.AlgorithmType;
import scheduler.io.ProcessCsv;
import scheduler.io.SampleData;
import scheduler.model.Limits;
import scheduler.model.SimProcess;

/** Sol panel: süreç tablosu, veri yükleme düğmeleri ve algoritma ayarları. */
final class InputPanel extends JPanel {

    private final ProcessTableModel model = new ProcessTableModel();
    private final JTable table = new JTable(model);
    private final JLabel countLabel = new JLabel();
    private final JComboBox<AlgorithmType> algorithmCombo = new JComboBox<>(AlgorithmType.values());
    private final JTextArea description = new JTextArea();
    private final JSpinner quantumSpinner = new JSpinner(new SpinnerNumberModel(2, 1, Limits.MAX_QUANTUM, 1));
    private final JComboBox<String> priorityCombo = new JComboBox<>(
            new String[] {"Küçük sayı = yüksek öncelik", "Büyük sayı = yüksek öncelik"});
    private final FlatButton addButton =
            new FlatButton("Ekle", Icons.of(Icons.Shape.PLUS, Theme.TEXT_SECONDARY), FlatButton.Kind.SECONDARY);
    private final Random random = new Random();

    private Runnable changeListener = () -> { };
    private Runnable runListener = () -> { };

    InputPanel() {
        super(new VStackLayout(12));
        setOpaque(false);
        setPreferredSize(new Dimension(346, 0));
        add(buildProcessCard(), VStackLayout.FILL);
        add(buildAlgorithmCard());

        model.setProcesses(SampleData.all().get(0).processes());
        updateProcessCount();
        updateDescription();

        model.addTableModelListener(e -> {
            updateProcessCount();
            changeListener.run();
        });
        algorithmCombo.addActionListener(e -> {
            updateDescription();
            changeListener.run();
        });
        quantumSpinner.addChangeListener(e -> changeListener.run());
        priorityCombo.addActionListener(e -> changeListener.run());
    }

    /** Süreçler ya da algoritma ayarları her değiştiğinde çağrılır. */
    void onChange(Runnable listener) {
        this.changeListener = listener;
    }

    /** "Simülasyonu Başlat" düğmesine basıldığında çağrılır. */
    void onRun(Runnable listener) {
        this.runListener = listener;
    }

    /** Tablodaki süreçler; yarım kalmış bir hücre düzenlemesi varsa önce onu tamamlar. */
    List<SimProcess> processes() {
        if (table.isEditing()) {
            table.getCellEditor().stopCellEditing();
        }
        return model.processes();
    }

    AlgorithmType algorithm() {
        return (AlgorithmType) algorithmCombo.getSelectedItem();
    }

    int quantum() {
        return (Integer) quantumSpinner.getValue();
    }

    boolean higherNumberIsHigherPriority() {
        return priorityCombo.getSelectedIndex() == 1;
    }

    // ------------------------------------------------------------ süreç kartı

    private Card buildProcessCard() {
        Tables.style(table, ProcessTableModel.TOOLTIPS);
        table.getColumnModel().getColumn(0).setCellRenderer(new Tables.ProcessNameRenderer(Theme::processColor));
        table.getColumnModel().getColumn(0).setPreferredWidth(118);
        table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        table.setSurrendersFocusOnKeystroke(true);
        // Başka bir yere tıklandığında düzenlenen hücre kendiliğinden kaydedilir.
        table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellEditor(new TextCellEditor(i > 0));
        }

        FlatButton removeButton = new FlatButton("Sil", FlatButton.Kind.SECONDARY);
        FlatButton clearButton = new FlatButton("Temizle", FlatButton.Kind.SECONDARY);
        FlatButton randomButton = new FlatButton("Rastgele Üret", FlatButton.Kind.SECONDARY);
        FlatButton sampleButton = new FlatButton("Örnek Veri",
                Icons.of(Icons.Shape.CHEVRON_DOWN, Theme.TEXT_SECONDARY), FlatButton.Kind.SECONDARY).iconTrailing();
        FlatButton loadButton = new FlatButton("CSV Yükle…", FlatButton.Kind.SECONDARY);
        FlatButton saveButton = new FlatButton("CSV Kaydet…", FlatButton.Kind.SECONDARY);

        addButton.setToolTipText("Tablonun sonuna yeni bir süreç ekler");
        removeButton.setToolTipText("Seçili süreçleri siler");
        clearButton.setToolTipText("Tüm süreçleri siler");
        randomButton.setToolTipText("Rastgele 4–7 süreç üretir");
        sampleButton.setToolTipText("Algoritmaların farklı davranışlarını gösteren hazır örnekler");

        addButton.addActionListener(e -> {
            stopEditing();
            model.addProcess();
            int last = model.getRowCount() - 1;
            table.getSelectionModel().setSelectionInterval(last, last);
            table.scrollRectToVisible(table.getCellRect(last, 0, true));
        });
        removeButton.addActionListener(e -> {
            stopEditing();
            int[] selected = table.getSelectedRows();
            if (selected.length == 0) {
                JOptionPane.showMessageDialog(this, "Silmek için tablodan bir ya da birkaç süreç seçin.",
                        "Süreç seçilmedi", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            model.removeRows(selected);
        });
        clearButton.addActionListener(e -> {
            stopEditing();
            model.setProcesses(List.of());
        });
        randomButton.addActionListener(e -> {
            stopEditing();
            model.setProcesses(SampleData.random(random));
        });
        sampleButton.addActionListener(e -> showSampleMenu(sampleButton));
        loadButton.addActionListener(e -> loadCsv());
        saveButton.addActionListener(e -> saveCsv());

        JPanel buttons = new JPanel(new VStackLayout(8));
        buttons.setOpaque(false);
        buttons.add(row(addButton, removeButton, clearButton));
        buttons.add(row(randomButton, sampleButton));
        buttons.add(row(loadButton, saveButton));

        JPanel body = new JPanel(new BorderLayout(0, 10));
        body.setOpaque(false);
        body.add(Tables.scroll(table), BorderLayout.CENTER);
        body.add(buttons, BorderLayout.SOUTH);

        countLabel.setFont(Theme.font(Font.PLAIN, 11.5f));
        countLabel.setForeground(Theme.TEXT_MUTED);
        return new Card("Süreçler").headerRight(countLabel).content(body);
    }

    private static JPanel row(FlatButton... buttons) {
        JPanel row = new JPanel(new GridLayout(1, buttons.length, 8, 0));
        row.setOpaque(false);
        for (FlatButton button : buttons) {
            row.add(button);
        }
        return row;
    }

    private void showSampleMenu(FlatButton anchor) {
        JPopupMenu menu = new JPopupMenu();
        for (SampleData.Sample sample : SampleData.all()) {
            JMenuItem item = new JMenuItem(sample.title());
            item.addActionListener(e -> {
                stopEditing();
                model.setProcesses(sample.processes());
            });
            menu.add(item);
        }
        menu.show(anchor, 0, anchor.getHeight() + 2);
    }

    private void loadCsv() {
        stopEditing();
        FileDialog dialog = new FileDialog(ownerFrame(), "CSV dosyasından süreç yükle", FileDialog.LOAD);
        // Süzgeç macOS ve Linux'ta, dosya adı kalıbı ise Windows'ta dikkate alınır.
        dialog.setFilenameFilter((directory, name) -> isCsv(name) || name.toLowerCase(Locale.ROOT).endsWith(".txt"));
        if (System.getProperty("os.name", "").startsWith("Windows")) {
            dialog.setFile("*.csv");
        }
        dialog.setVisible(true);
        if (dialog.getFile() == null) {
            return;
        }
        try {
            model.setProcesses(ProcessCsv.read(Path.of(dialog.getDirectory(), dialog.getFile())));
        } catch (IOException | IllegalArgumentException e) {
            showError("Dosya yüklenemedi", e);
        }
    }

    private void saveCsv() {
        List<SimProcess> processes = processes();
        if (processes.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Kaydedilecek süreç yok.",
                    "Süreç tablosu boş", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        FileDialog dialog = new FileDialog(ownerFrame(), "Süreçleri CSV olarak kaydet", FileDialog.SAVE);
        dialog.setFile("surecler.csv");
        dialog.setVisible(true);
        if (dialog.getFile() == null) {
            return;
        }
        String fileName = isCsv(dialog.getFile()) ? dialog.getFile() : dialog.getFile() + ".csv";
        try {
            ProcessCsv.write(Path.of(dialog.getDirectory(), fileName), processes);
        } catch (IOException e) {
            showError("Dosya kaydedilemedi", e);
        }
    }

    private static boolean isCsv(String fileName) {
        return fileName.toLowerCase(Locale.ROOT).endsWith(".csv");
    }

    private void showError(String title, Exception e) {
        String message = e instanceof NoSuchFileException missing ? "Dosya bulunamadı: " + missing.getFile()
                : e instanceof IOException ? "Dosyaya erişilemedi: " + e.getMessage()
                : e.getMessage();
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
    }

    private Frame ownerFrame() {
        return SwingUtilities.getWindowAncestor(this) instanceof Frame frame ? frame : null;
    }

    private void stopEditing() {
        if (table.isEditing()) {
            table.getCellEditor().stopCellEditing();
        }
    }

    private void updateProcessCount() {
        int count = model.getRowCount();
        countLabel.setText(count == 0 ? "süreç yok" : count + " süreç");
        addButton.setEnabled(!model.isFull());
    }

    // -------------------------------------------------------- algoritma kartı

    private Card buildAlgorithmCard() {
        algorithmCombo.setFont(Theme.font(Font.PLAIN, 12.5f));
        algorithmCombo.setMaximumRowCount(AlgorithmType.values().length);

        description.setFont(Theme.font(Font.PLAIN, 11.5f));
        description.setForeground(Theme.TEXT_SECONDARY);
        description.setLineWrap(true);
        description.setWrapStyleWord(true);
        description.setEditable(false);
        description.setFocusable(false);
        description.setOpaque(false);
        description.setBorder(BorderFactory.createEmptyBorder());
        // Genişliği yerleşim belirler; yükseklik en uzun açıklamaya (4 satır) göre sabittir.
        int lineHeight = description.getFontMetrics(description.getFont()).getHeight();
        description.setPreferredSize(new Dimension(10, 4 * lineHeight));

        quantumSpinner.setFont(Theme.font(Font.PLAIN, 12.5f));
        quantumSpinner.setPreferredSize(new Dimension(70, quantumSpinner.getPreferredSize().height));
        quantumSpinner.setToolTipText("Round Robin'de bir sürecin kesintisiz çalışabileceği en uzun süre");
        priorityCombo.setFont(Theme.font(Font.PLAIN, 12.5f));
        priorityCombo.setToolTipText("Öncelikli zamanlamada hangi sayının daha yüksek öncelik sayılacağı");

        FlatButton runButton = new FlatButton("Simülasyonu Başlat",
                Icons.of(Icons.Shape.PLAY, Theme.SURFACE), FlatButton.Kind.PRIMARY);
        runButton.setFont(Theme.font(Font.BOLD, 13.5f));
        runButton.setBorder(BorderFactory.createEmptyBorder(11, 12, 11, 12));
        runButton.setToolTipText("Seçili algoritmayı baştan itibaren adım adım oynatır");
        runButton.addActionListener(e -> runListener.run());

        JPanel body = new JPanel(new VStackLayout(10));
        body.setOpaque(false);
        body.add(algorithmCombo);
        body.add(description);
        body.add(setting("Round Robin zaman dilimi (q)", quantumSpinner));
        body.add(setting("Öncelik yönü", priorityCombo));
        body.add(runButton);
        return new Card("Zamanlama Algoritması").content(body);
    }

    private static JPanel setting(String labelText, Component control) {
        JLabel label = new JLabel(labelText);
        label.setFont(Theme.font(Font.PLAIN, 12f));
        label.setForeground(Theme.TEXT_SECONDARY);
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);
        row.add(label, BorderLayout.CENTER);
        row.add(control, BorderLayout.EAST);
        return row;
    }

    private void updateDescription() {
        description.setText(algorithm().description());
    }
}

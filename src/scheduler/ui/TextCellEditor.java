package scheduler.ui;

import java.awt.Color;
import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicTextFieldUI;

/**
 * Süreç tablosunun hücre düzenleyicisi. Tek tıklamayla açılır ve mevcut değeri seçili getirir.
 * Sayı sütunlarında yalnızca tam sayı kabul eder; geçersiz girdide hücre kırmızı çerçeveyle
 * işaretlenir ve düzenleme bitmez.
 */
final class TextCellEditor extends DefaultCellEditor {

    private final Border normalBorder = frame(Theme.ACCENT);
    private final Border invalidBorder = frame(Theme.DANGER);
    private final JTextField field;
    private final boolean integer;
    private Object value;

    /** @param integer {@code true} ise yalnızca tam sayı kabul eder ve değeri {@link Integer} olarak verir */
    TextCellEditor(boolean integer) {
        super(new JTextField());
        this.integer = integer;
        this.field = (JTextField) getComponent();
        field.setUI(new BasicTextFieldUI());
        field.setHorizontalAlignment(integer ? SwingConstants.CENTER : SwingConstants.LEFT);
        field.setBackground(Theme.SURFACE);
        field.setForeground(Theme.TEXT);
        field.setCaretColor(Theme.TEXT);
        field.setSelectionColor(Theme.ACCENT_SOFT);
        field.setSelectedTextColor(Theme.TEXT);
        setClickCountToStart(1);
    }

    private static Border frame(Color color) {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color, 2), BorderFactory.createEmptyBorder(0, 6, 0, 6));
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object cellValue, boolean isSelected, int row, int column) {
        super.getTableCellEditorComponent(table, cellValue, isSelected, row, column);
        value = null;
        field.setFont(table.getFont());
        field.setBorder(normalBorder);
        SwingUtilities.invokeLater(field::selectAll);
        return field;
    }

    @Override
    public boolean stopCellEditing() {
        String text = field.getText().trim();
        if (integer) {
            try {
                value = Integer.valueOf(text);
            } catch (NumberFormatException e) {
                field.setBorder(invalidBorder);
                return false;
            }
        } else {
            value = text;
        }
        return super.stopCellEditing();
    }

    @Override
    public Object getCellEditorValue() {
        return value;
    }
}

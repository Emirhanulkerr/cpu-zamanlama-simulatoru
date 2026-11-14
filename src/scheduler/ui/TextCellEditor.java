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

/**
 * Süreç tablosunun hücre düzenleyicisi. Tek tıklamayla açılır ve mevcut değeri seçili getirir.
 * Sayı sütunlarında yalnızca tam sayı kabul eder; geçersiz girdide hücre kırmızı çerçeveyle
 * işaretlenir ve düzenleme bitmez.
 */
final class TextCellEditor extends DefaultCellEditor {

    private static final Border NORMAL = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.ACCENT, 2), BorderFactory.createEmptyBorder(0, 6, 0, 6));
    private static final Border INVALID = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xD03B3B), 2), BorderFactory.createEmptyBorder(0, 6, 0, 6));

    private final JTextField field;
    private final boolean integer;
    private Object value;

    /** @param integer {@code true} ise yalnızca tam sayı kabul eder ve değeri {@link Integer} olarak verir */
    TextCellEditor(boolean integer) {
        super(new JTextField());
        this.integer = integer;
        this.field = (JTextField) getComponent();
        field.setHorizontalAlignment(integer ? SwingConstants.CENTER : SwingConstants.LEFT);
        setClickCountToStart(1);
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object cellValue, boolean isSelected, int row, int column) {
        super.getTableCellEditorComponent(table, cellValue, isSelected, row, column);
        value = null;
        field.setFont(table.getFont());
        field.setBorder(NORMAL);
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
                field.setBorder(INVALID);
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

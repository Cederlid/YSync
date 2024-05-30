import dev.ytterate.ysync.SyncAction;

import javax.swing.*;
import java.awt.*;

public class CheckBoxListCellRenderer extends JPanel implements ListCellRenderer<Object> {
    JCheckBox checkBox = new JCheckBox();

    public CheckBoxListCellRenderer() {
        this.add(checkBox);
        setLayout(new BorderLayout());
        add(checkBox, BorderLayout.CENTER);
    }

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {

        setComponentOrientation(list.getComponentOrientation());
        setFont(list.getFont());
        setBackground(list.getBackground());
        setForeground(list.getForeground());
        checkBox.setSelected(isSelected);

        if (value instanceof SyncAction) {
            checkBox.setText(value.toString());
        } else {
            checkBox.setText((value == null) ? "" : value.toString());
        }

        return this;
    }
}

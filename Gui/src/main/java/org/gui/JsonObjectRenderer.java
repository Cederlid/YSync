import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;

public class JsonObjectRenderer extends JPanel implements ListCellRenderer<JSONObject>{
    private final JLabel sourceLabel;
    private final JLabel destinationLabel;

    public JsonObjectRenderer() {
        setLayout(new BorderLayout(5,5));
        sourceLabel = new JLabel();
        destinationLabel = new JLabel();
        add(sourceLabel, BorderLayout.WEST);
        add(destinationLabel, BorderLayout.CENTER);
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends JSONObject> list, JSONObject value, int index, boolean isSelected, boolean cellHasFocus) {
        sourceLabel.setText("Source: " + value.getString("source") + " - ");
        destinationLabel.setText("Destination: " + value.getString("destination"));

        if (isSelected){
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
        } else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }
        return this;
    }














}

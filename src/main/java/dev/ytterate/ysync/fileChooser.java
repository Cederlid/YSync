package dev.ytterate.ysync;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class fileChooser extends JFrame {

    public static void main(String[] args) {
        fileChooser fileChooser = new fileChooser();
        fileChooser.createWindow();
    }

    private void createWindow() {
        JFrame frame = new JFrame("File reader");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        createUi(frame);
        frame.setSize(600, 240);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void createUi(final JFrame frame) {
        JPanel panel = new JPanel();
        LayoutManager layout = new FlowLayout();
        panel.setLayout(layout);

        JButton button = new JButton("Choose file");
        JButton submitBtn = new JButton("Click");
        JLabel label = new JLabel();
        JTextField nameInput = new JTextField();
        nameInput.setColumns(10);

        JLabel nameLabel = new JLabel("Hello!");

        nameInput.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateLabel();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateLabel();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateLabel();
            }

            private void updateLabel() {
                nameLabel.setText(nameInput.getText() + ", Hello!");
            }

        });

        submitBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JLabel imageLabel = new JLabel();

                ImageIcon icon = createImageIcon("/funny.jpeg");
                if (icon != null) {
                    imageLabel.setIcon(icon);

                    int option = JOptionPane.showOptionDialog(
                            submitBtn,
                            new Object[]{imageLabel, panel},
                            "Welcome back",
                            JOptionPane.DEFAULT_OPTION,
                            JOptionPane.INFORMATION_MESSAGE,
                            icon,
                            new Object[]{},
                            null);

                    if (option == JOptionPane.OK_OPTION) {

                    }
                } else {
                          JOptionPane.showMessageDialog(submitBtn, "Image not found!");
                }
            }
        });

        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setAcceptAllFileFilterUsed(true);
                fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Png files only", "png"));
                fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Jpg files only", "jpg"));

                int option = fileChooser.showOpenDialog(frame);

                if (option == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();
                    label.setText("You have selected: " + file.getAbsolutePath());
                } else {
                    label.setText("Open command canceled");
                }
            }
        });

        JPanel panel2 = new JPanel();
        panel2.add(nameLabel);
        panel.add(button);
        panel.add(submitBtn);
        panel.add(nameInput);
        panel.add(label);
        frame.getContentPane().add(panel, BorderLayout.CENTER);
        frame.getContentPane().add(panel2, BorderLayout.SOUTH);
    }

    private ImageIcon createImageIcon(String path) {
        java.net.URL imgURL = getClass().getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    }

}



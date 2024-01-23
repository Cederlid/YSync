package dev.ytterate.ysync;

import javax.imageio.ImageIO;
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
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class FileChooser extends JFrame {
    File file = null;

    public static void main(String[] args) {
        FileChooser fileChooser = new FileChooser();
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
        LayoutManager layout = new GridLayout(5,1);
        panel.setLayout(layout);

        JButton button = new JButton("Choose file");
        JButton submitBtn = new JButton("Submit");
        JLabel label = new JLabel();
        JTextField nameInput = new JTextField();
        nameInput.setColumns(40);

        panel.add(button);
        panel.add(nameInput);
        panel.add(submitBtn);
        panel.add(label);
        frame.getContentPane().add(panel);

        JLabel nameLabel = new JLabel("Hello!");
        JPanel panel2 = new JPanel();
        panel2.add(nameLabel);
        frame.getContentPane().add(panel2, BorderLayout.SOUTH);

        dynamicNameListener(nameInput, nameLabel);

        addPopupListener(submitBtn, panel2);

        addDirectoryListener(frame, button, label);

    }

    private static void dynamicNameListener(JTextField nameInput, JLabel nameLabel) {
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
    }

    private void addDirectoryListener(JFrame frame, JButton button, JLabel label) {
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                fileChooser.setAcceptAllFileFilterUsed(true);

                int option = fileChooser.showOpenDialog(frame);

                if (option == JFileChooser.APPROVE_OPTION) {
                    file = fileChooser.getSelectedFile();
                    label.setText("You have selected: " + file.getAbsolutePath());
                } else {
                    label.setText("Open command canceled");
                }
            }
        });
    }

    private void addPopupListener(JButton submitBtn, JPanel panel2) {
        submitBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                if (file != null) {
                    ImageIcon thumbnail = createThumbnail(file);
                    if (thumbnail != null) {

                        int optionDialog = JOptionPane.showOptionDialog(
                                submitBtn,
                                new Object[]{panel2},
                                "Welcome back",
                                JOptionPane.DEFAULT_OPTION,
                                JOptionPane.INFORMATION_MESSAGE,
                                thumbnail,
                                new Object[]{},
                                null);
                        if (optionDialog == JOptionPane.OK_OPTION) {

                        }
                    } else {
                        JOptionPane.showMessageDialog(submitBtn, "Thumbnail generation failed!");
                    }
                }
            }
        });
    }

    private ImageIcon createThumbnail(File file) {
        try {
            BufferedImage originalImage = ImageIO.read(file);

            int thumbnailWidth = 100;
            int thumbnailHeight = 100;

            BufferedImage thumbnail = new BufferedImage(thumbnailWidth, thumbnailHeight, BufferedImage.TYPE_INT_ARGB);
            thumbnail.getGraphics().drawImage(originalImage.getScaledInstance(thumbnailWidth, thumbnailHeight, Image.SCALE_SMOOTH), 0, 0, null);
            System.out.println("dubbel");
            return new ImageIcon(thumbnail);
        } catch (IOException e) {
            System.err.println("Thumbnail generation error: " + e.getMessage());
            return null;
        }

    }
}



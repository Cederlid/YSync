package dev.ytterate.ysync;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MyUi extends JFrame {
    private JPanel mainPanel;
    private JLabel nameLabel;
    private JTextField nameInput;
    private JButton submitBtn;
    private JLabel imageLabel;

    public static void main(String[] args) {
        MyUi myUi = new MyUi();
        myUi.settings();
    }

    public MyUi() {
        imageLabel = new JLabel();
        submitBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showCustomDialog();
            }
        });

    }

    private void settings() {
        setContentPane(mainPanel);
        setTitle("Welcome!");
        setBounds(700, 400, 400, 200);
        setVisible(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private void showCustomDialog() {
        JPanel panel = new JPanel();
        panel.add(new JLabel(nameInput.getText() + " , Hello!"));
        panel.add(imageLabel);

        ImageIcon icon = new ImageIcon("funny.jpeg");
        int option = JOptionPane.showOptionDialog(
                submitBtn,
                panel,
                "Welcome back",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                icon,
                new Object[] {},
                null);

        if (option == JOptionPane.OK_OPTION) {
        }
    }

}



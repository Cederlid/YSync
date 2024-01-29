package dev.ytterate.ysync;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class FileChooser extends JFrame {
    private File file1 = null;
    private File file2 = null;
    private JPanel jPanel = null;
    private JPanel jPanel2 = null;
    private JTree tree;
    private JTree tree2;


    public static void main(String[] args) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.createWindow();
    }

    private void createWindow() {
        JFrame frame = new JFrame("File reader");
        createUi(frame);
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void createUi(final JFrame frame) {
        JPanel panel = new JPanel();
        LayoutManager layout = new FlowLayout();
        panel.setLayout(layout);

        JButton button = new JButton("Choose file");
        JButton button2 = new JButton("Choose file 2");
        JButton submitBtn = new JButton("Submit");
        JLabel label = new JLabel("", JLabel.CENTER);

        panel.add(button);
        panel.add(button2);
        panel.add(submitBtn);
        panel.add(label);
        frame.getContentPane().add(panel, BorderLayout.NORTH);

        addPopupListener(frame, submitBtn, label);
        addDirectoryListener(frame, button, label);
        addDirectoryListener2(frame, button2, label);

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
                    file1 = fileChooser.getSelectedFile();
                } else {
                    label.setText("Open command canceled");
                }
            }
        });

    }

    private void addDirectoryListener2(JFrame frame, JButton button2, JLabel label) {
        button2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                fileChooser.setAcceptAllFileFilterUsed(true);

                int option = fileChooser.showOpenDialog(frame);

                if (option == JFileChooser.APPROVE_OPTION) {
                    file2 = fileChooser.getSelectedFile();
                } else {
                    label.setText("Open command canceled");
                }
            }
        });

    }

    private void addPopupListener(JFrame frame, JButton submitBtn, JLabel label) {
        submitBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                if (file1 != null && file2 != null) {
                    showPopUp();
                }
            }

            private void showPopUp() {
                DefaultMutableTreeNode root1 = new DefaultMutableTreeNode(file1.getName());
                DefaultMutableTreeNode root2 = new DefaultMutableTreeNode(file2.getName());


                populateTree(root1, file1);
                populateTree(root2, file2);

                tree = new JTree(root1);
                tree2 = new JTree(root2);

                tree.addTreeExpansionListener(new FileTreeExpansionListner());
                tree2.addTreeExpansionListener(new FileTreeExpansionListner());

                JScrollPane scrollPane = new JScrollPane(tree);
                JScrollPane scrollPane2 = new JScrollPane(tree2);

                jPanel = new JPanel(new BorderLayout());
                jPanel.add(scrollPane, BorderLayout.CENTER);
                jPanel2 = new JPanel(new BorderLayout());
                jPanel2.add(scrollPane2, BorderLayout.CENTER);

                compareFilesInDirectories(file1, file2);

                JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, jPanel, jPanel2);
                splitPane.setResizeWeight(0.5);

                frame.getContentPane().removeAll();
                frame.getContentPane().add(splitPane);
                frame.revalidate();
                frame.repaint();
            }


        });
    }
    private void populateTree(DefaultMutableTreeNode parentNode, File directory) {
        File[] filesInDirectory = directory.listFiles();

        if (filesInDirectory != null) {
            for (File file : filesInDirectory) {
                DefaultMutableTreeNode node = new DefaultMutableTreeNode(file.getName());
                parentNode.add(node);

                if (file.isDirectory()) {
                    populateTree(node, file);
                }
            }
        }
    }

    private void compareFilesInDirectories(File dir1, File dir2){
        File [] files1 = dir1.listFiles();
        File [] files2 = dir2.listFiles();

        if (files1 != null && files2 != null){
            DefaultListModel<String> differencesModel = new DefaultListModel<>();

            for (File file1 : files1){
                boolean found = false;

                for (File file2 : files2){
                    if (file1.getName().equals(file2.getName()) && file1.length() == file2.length()) {
                        found = true;
                        break;
                    }
                }
                if (!found){
                    differencesModel.addElement("File: " + file1.getName() + " - in directory: " + dir1.getName());
                }
            }

            for (File file2 : files2){
                boolean found = false;

                for (File file1 : files1){
                    if (file1.getName().equals(file2.getName()) && file1.length() == file2.length()){
                        found = true;
                        break;
                    }
                }

                if (!found){
                    differencesModel.addElement("File: " + file2.getName() + " - in directory: " + dir2.getName());
                }
            }

            JList<String> differencesList = new JList<>(differencesModel);
            JScrollPane differencesScrollPane = new JScrollPane(differencesList);
            differencesScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

            jPanel.add(differencesScrollPane, BorderLayout.SOUTH);
        }
    }






}



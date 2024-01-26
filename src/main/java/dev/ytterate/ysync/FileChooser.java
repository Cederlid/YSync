package dev.ytterate.ysync;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;

public class FileChooser extends JFrame {
    private File file = null;
    private File file2 = null;
    private JPanel jPanel = null;
    private JTree tree;

    public static void main(String[] args) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.createWindow();
    }

    private void createWindow() {
        JFrame frame = new JFrame("File reader");
        // frame.setLayout(new GridLayout(3,1));
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
        //JTextField nameInput = new JTextField();
        //nameInput.setColumns(40);

        panel.add(button);
        //panel.add(nameInput);
        panel.add(button2);
        panel.add(submitBtn);
        panel.add(label);
        frame.getContentPane().add(panel, BorderLayout.NORTH);

        JLabel nameLabel = new JLabel("Hello!");
//        JPanel panel2 = new JPanel();
//        panel2.add(nameLabel);
//        frame.getContentPane().add(panel2, BorderLayout.SOUTH);

        // dynamicNameListener(nameInput, nameLabel);

        addPopupListener(frame, submitBtn, label);

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
                    //label.setText("You have selected: " + file.getAbsolutePath());

                    //  JOptionPane.showMessageDialog(frame, popupPanel1, "Files in Directory", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    label.setText("Open command canceled");
                }
            }


//            private void listFiles(File directory, StringBuilder fileList, int indentationLevel) {
//                File [] filesInDirectory = directory.listFiles();
//
//                if (filesInDirectory != null){
//                    for (File file : filesInDirectory){
//                        for (int i = 0; i < indentationLevel; i++) {
//                            fileList.append("&nbsp;&nbsp;");
//                        }
//                        if (file.isDirectory()){
//                            fileList.append("+ ").append(file.getName()).append("<br>");
//                            listFiles(file, fileList, indentationLevel + 1);
//                        } else {
//                            fileList.append("- ").append(file.getName()).append("<br>");
//                        }
//                    }
//                }
//
//            }
        });

    }

    private void addPopupListener(JFrame frame, JButton submitBtn, JLabel label) {
        submitBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                if (file != null) {
                    showPopUp();
                }
            }

            private void showPopUp() {
                DefaultMutableTreeNode root = new DefaultMutableTreeNode(file.getName());

                populateTree(root, file);

                tree = new JTree(root);

                tree.addTreeExpansionListener(new FileTreeExpansionListner());

//                    StringBuilder fileList = new StringBuilder("<html><body>");
//
//                    listFiles(file, fileList, 0);
//
//                    fileList.append("</body></html>");
//                    JLabel fileListLabel = new JLabel(fileList.toString());
//                    fileListLabel.setHorizontalAlignment(JLabel.CENTER);

                JScrollPane scrollPane = new JScrollPane(tree);
                scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

                jPanel = new JPanel(new BorderLayout());
                jPanel.add(scrollPane, BorderLayout.CENTER);

                JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, label, jPanel);
                splitPane.setResizeWeight(0.5);

                frame.getContentPane().removeAll();
                frame.getContentPane().add(splitPane);
                frame.revalidate();
                frame.repaint();
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


        });
    }

}



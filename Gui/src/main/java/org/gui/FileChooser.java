import dev.ytterate.ysync.CheckBoxListCellRenderer;
import dev.ytterate.ysync.ContinueCallback;
import dev.ytterate.ysync.FileComparison;
import dev.ytterate.ysync.MisMatchAction;
import dev.ytterate.ysync.SyncAction;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class FileChooser extends JFrame implements ContinueCallback {
    private File file1 = null;
    private File file2 = null;
    private final JLabel errorLabel = new JLabel();
    private FileComparison fileComparison;

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

        JButton button = new JButton("Choose the source directory");
        JButton button2 = new JButton("Choose the destination directory");
        JButton submitBtn = new JButton("Submit");
        JButton saveBtn = new JButton("Save");
        JLabel label = new JLabel("", JLabel.CENTER);

        panel.add(button);
        panel.add(button2);
        panel.add(submitBtn);
        panel.add(saveBtn);
        panel.add(label);
        frame.getContentPane().add(panel, BorderLayout.NORTH);

        addPopupListener(submitBtn, saveBtn);
        addDirectoryListener(frame, button, label);
        addDirectoryListener2(frame, button2, label);

    }

    private ActionListener createDirectoryActionListener(JFrame frame, JLabel label, boolean isFirstButton) {
        return e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            fileChooser.setAcceptAllFileFilterUsed(true);

            int option = fileChooser.showOpenDialog(frame);

            if (option == JFileChooser.APPROVE_OPTION) {
                File targetFile = fileChooser.getSelectedFile();
                if (isFirstButton) {
                    file1 = targetFile;
                } else {
                    file2 = targetFile;
                }
            } else {
                label.setText("Open command canceled");
            }
        };
    }

    private void addDirectoryListener(JFrame frame, JButton button, JLabel label) {
        button.addActionListener(createDirectoryActionListener(frame, label, true));
    }

    private void addDirectoryListener2(JFrame frame, JButton button2, JLabel label) {
        button2.addActionListener(createDirectoryActionListener(frame, label, false));
    }

    private void addPopupListener(JButton submitBtn, JButton saveBtn) {
        submitBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (file1 != null && file2 != null) {
                    try {
                        startCopying();
                    } catch (IOException ex) {
                        errorLabel.setText("Error: " + ex.getMessage());
                    }
                }
            }

            private void startCopying() throws IOException {

                copyFilesInOneDirection(file1, file2).thenApply(result -> {
                    try {
                        copyFilesInOneDirection(file2, file1).thenApply(r -> {
                            JOptionPane.showMessageDialog(null, "Copy is complete!");
                            return null;
                        });
                        return null;
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        });

        saveBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (file1 != null && file2 != null) {
                    try {
                        writeDirectoriesToJson(file1.getAbsolutePath(), file2.getAbsolutePath());
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                } else {
                    errorLabel.setText("Please choose both source and destination directories first.");
                }
            }
        });
    }

    void writeDirectoriesToJson(String source, String destination) throws IOException {
        try {
            File jsonFile = new File("/Users/wijdancederlid/Desktop/config.json");
            JSONArray jsonArray;
            if (jsonFile.exists()) {
                String jsonContent = FileUtils.readFileToString(jsonFile, StandardCharsets.UTF_8);
                jsonArray = new JSONArray(jsonContent);
            } else {
                jsonArray = new JSONArray();
            }
            JSONObject directoryObject = new JSONObject();
            directoryObject.put("source", source);
            directoryObject.put("destination", destination);

            jsonArray.put(directoryObject);
            try (FileWriter fileWriter = new FileWriter(jsonFile)) {
                fileWriter.write(jsonArray.toString(4));
            }

            JOptionPane.showMessageDialog(null, "Directories saved to config.json");
        } catch (IOException e) {
            errorLabel.setText("Error: " + e.getMessage());
        }
    }



private CompletableFuture<Void> copyFilesInOneDirection(File dir1, File dir2) throws IOException {
    List<String> emptyList = new ArrayList<>();
    fileComparison = new FileComparison(dir1, dir2, this, emptyList, emptyList);
    return fileComparison.compareAndCopyFiles();
}

@Override
public CompletableFuture<Boolean> onGotMisMatches(java.util.List<SyncAction> syncActions) throws IOException {
    DefaultListModel<SyncAction> misMatchModel = new DefaultListModel<>();
    for (SyncAction action : syncActions) {
        if (action.isMisMatch()) {
            misMatchModel.addElement(action);
        }
    }

    JFrame dialogFrame = new JFrame("Choose actions to overwrite");
    dialogFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

    JPanel panel = new JPanel();
    panel.setLayout(new BorderLayout());

    JList<SyncAction> mismatchList = new JList<>(misMatchModel);
    mismatchList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    mismatchList.setCellRenderer(new CheckBoxListCellRenderer());

    JScrollPane scrollPane = new JScrollPane(mismatchList);
    panel.add(scrollPane, BorderLayout.CENTER);

    JButton continueButton = new JButton("Continue");
    CompletableFuture<Boolean> completableFuture = new CompletableFuture<>();
    continueButton.addActionListener(e -> {
        for (int i = 0; i < misMatchModel.size(); i++) {
            SyncAction action = misMatchModel.getElementAt(i);
            if (action.isMisMatch()) {
                if (mismatchList.isSelectedIndex(i)) {
                    ((MisMatchAction) action).confirm();
                }
            }
        }
        try {
            completableFuture.complete(true);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        dialogFrame.dispose();
    });


    JPanel buttonPanel = new JPanel();
    buttonPanel.add(continueButton);

    panel.add(buttonPanel, BorderLayout.SOUTH);
    dialogFrame.getContentPane().add(panel);

    dialogFrame.pack();
    dialogFrame.setVisible(true);

    return completableFuture;
}
}



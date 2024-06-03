import dev.ytterate.ysync.ContinueCallback;
import dev.ytterate.ysync.FileComparison;
import dev.ytterate.ysync.MisMatchAction;
import dev.ytterate.ysync.SyncAction;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
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
    private JPanel jsonPanel;
    private Icon fileAddedIcon;
    private Icon copyIcon;
    private Icon cancelIcon;

    public static void main(String[] args) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.createWindow();
    }

    private void createWindow() {
        JFrame frame = new JFrame("File reader");
        createUi(frame);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setMinimumSize(new Dimension(600, 600));
        frame.pack();
        frame.setVisible(true);
    }

    private void createUi(final JFrame frame) {
        fileAddedIcon = scaleIcon(new ImageIcon("Gui/src/main/resources/images/custom-icon.png"), 50, 50);
        copyIcon = scaleIcon(new ImageIcon("Gui/src/main/resources/images/icon-copy.png"), 50, 50);
        cancelIcon = scaleIcon(new ImageIcon("Gui/src/main/resources/images/cancel-icon.png"), 50, 50);
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());

        JButton button = new JButton("Choose the source directory");
        JButton button2 = new JButton("Choose the destination directory");
        button.setPreferredSize((new Dimension(400, 30)));
        button2.setPreferredSize((new Dimension(400, 30)));
        JButton submitBtn = new JButton("Submit");
        JButton saveBtn = new JButton("Save");
        JButton jsonSyncBtn = new JButton("Sync from Json file");
        jsonPanel = new JPanel();
        jsonPanel.setLayout(new BoxLayout(jsonPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(jsonPanel);

        addDirectoryListener(frame, button, true);
        addDirectoryListener2(frame, button2,false);

        buttonPanel.add(button);
        buttonPanel.add(button2);
        buttonPanel.add(submitBtn);
        buttonPanel.add(saveBtn);
        buttonPanel.add(jsonSyncBtn);
        panel.add(buttonPanel, BorderLayout.NORTH);
        panel.add(scrollPane);
        frame.getContentPane().add(panel);

        addPopupListener(submitBtn, saveBtn, jsonSyncBtn);
        showJsonContent();
    }

    private ImageIcon scaleIcon(ImageIcon icon, int width, int height) {
        Image image = icon.getImage();
        BufferedImage scaledImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics2D = scaledImage.createGraphics();
        graphics2D.drawImage(image, 0, 0, width, height, null);
        graphics2D.dispose();
        return new ImageIcon(scaledImage);
    }

    private ActionListener createDirectoryActionListener(JFrame frame, JButton button, boolean isFirstButton) {
        return e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            fileChooser.setAcceptAllFileFilterUsed(true);

            int option = fileChooser.showOpenDialog(frame);

            if (option == JFileChooser.APPROVE_OPTION) {
                File targetFile = fileChooser.getSelectedFile();
                if (isFirstButton) {
                    file1 = targetFile;
                    button.setText("Source: " + file1.getPath());
                    frame.pack();
                } else {
                    file2 = targetFile;
                    button.setText("Destination: " + file2.getPath());
                    frame.pack();
                }
            } else {
                JOptionPane.showMessageDialog(null, "The action have been cancelled!", "Choose directories", JOptionPane.INFORMATION_MESSAGE, cancelIcon);

            }
        };
    }

    private void addDirectoryListener(JFrame frame, JButton button, boolean isFirstButton) {
        button.addActionListener(createDirectoryActionListener(frame, button,true));
    }

    private void addDirectoryListener2(JFrame frame, JButton button2, boolean isFirstButton) {
        button2.addActionListener(createDirectoryActionListener(frame, button2,false));
    }

    private void addPopupListener(JButton submitBtn, JButton saveBtn, JButton jsonSyncBtn) {
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
                            JOptionPane.showMessageDialog(null, "Copy is complete!", "Success", JOptionPane.INFORMATION_MESSAGE, copyIcon);
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
                        showJsonContent();
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                } else {
                    errorLabel.setText("Please choose both source and destination directories first.");
                }
            }
        });

        jsonSyncBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                File jsonFile = new File(System.getProperty("user.home") + File.separator + "Desktop" + File.separator + "JsonSyncFile.json");
                if (jsonFile.exists() && jsonFile.isFile()) {
                    try {
                        List<String[]> directories = readDirectoriesFromJson(jsonFile);
                        for (String[] directory : directories) {
                            String source = directory[0];
                            String destination = directory[1];
                            copyFilesInOneDirection(new File(source), new File(destination))
                                    .thenAccept(result -> {
                                        try {
                                            copyFilesInOneDirection(new File(source), new File(destination));
                                            JOptionPane.showMessageDialog(null, "Synchronization complete!", "Success", JOptionPane.INFORMATION_MESSAGE);
                                        } catch (IOException ex) {
                                            throw new RuntimeException(ex);
                                        }
                                    }).exceptionally(ex -> {
                                        errorLabel.setText("Error synchronizing directories: " + ex.getMessage());
                                        return null;
                                    });
                        }
                    } catch (IOException ex) {
                        errorLabel.setText("Error reading JSON file: " + ex.getMessage());
                    }
                } else {
                    errorLabel.setText("The JSON file does not exist or is not a file.");
                }
            }
        });


    }

    private List<String[]> readDirectoriesFromJson(File jsonFile) throws IOException {
        List<String[]> directories = new ArrayList<>();

        String jsonContent = FileUtils.readFileToString(jsonFile, StandardCharsets.UTF_8);

        JSONArray jsonArray = new JSONArray(jsonContent);

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject directoryObject = jsonArray.getJSONObject(i);

            String source = directoryObject.getString("source");
            String destination = directoryObject.getString("destination");

            directories.add(new String[]{source, destination});
        }

        return directories;
    }

    private List<JSONObject> readJsonArrayFromFile(File jsonFile) throws IOException{
        List<JSONObject> jsonObjects = new ArrayList<>();

        String jsonContent = FileUtils.readFileToString(jsonFile, StandardCharsets.UTF_8);
        JSONArray jsonArray = new JSONArray(jsonContent);

        for (int i = 0; i < jsonArray.length(); i++) {
            jsonObjects.add(jsonArray.getJSONObject(i));
        }
        return jsonObjects;
    }
    void writeDirectoriesToJson(String source, String destination) throws IOException {
        try {
            File jsonFile = new File(System.getProperty("user.home") + File.separator + "Desktop" + File.separator + "JsonSyncFile.json");
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
            JOptionPane.showMessageDialog(null, "Directories saved to JsonSyncFile.json", "Success", JOptionPane.INFORMATION_MESSAGE, fileAddedIcon);
        } catch (IOException e) {
            errorLabel.setText("Error: " + e.getMessage());
        }
    }

    private void showJsonContent() {
        jsonPanel.removeAll();
        try {
            List<JSONObject> jsonArray = readJsonArrayFromFile(new File(System.getProperty("user.home") + File.separator + "Desktop" + File.separator + "JsonSyncFile.json"));
            DefaultListModel<JSONObject> listModel = new DefaultListModel<>();
            for (JSONObject jsonObject : jsonArray){
                listModel.addElement(jsonObject);
            }

            JList<JSONObject> jsonList = new JList<>(listModel);
            jsonList.setCellRenderer(new JsonObjectRenderer());
            jsonList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            jsonList.setLayoutOrientation(JList.VERTICAL);

            jsonPanel.setLayout(new BorderLayout());
            JScrollPane scrollPane = new JScrollPane(jsonList);
            jsonPanel.add(scrollPane, BorderLayout.CENTER);

            jsonPanel.revalidate();
            jsonPanel.repaint();
        } catch (IOException e) {
            errorLabel.setText("Error: " + e.getMessage());
        }
    }

    private JButton syncButton(String[] sourceAndDest) {
        JButton syncBtn = new JButton("Sync");
        syncBtn.addActionListener(e -> {
            try {
                copyFilesInOneDirection(new File(sourceAndDest[0]), new File(sourceAndDest[1]))
                        .thenAccept(result -> {
                            try {
                                copyFilesInOneDirection(new File(sourceAndDest[1]), new File(sourceAndDest[0]));
                                JOptionPane.showMessageDialog(null, "Synchronization complete!", "Success", JOptionPane.INFORMATION_MESSAGE);
                            } catch (IOException ex) {
                                errorLabel.setText("Error synchronizing directories: " + ex.getMessage());
                            }
                        });
            } catch (IOException ex) {
                errorLabel.setText("Error starting synchronization: " + ex.getMessage());
            }
        });
        return syncBtn;
    }


    private CompletableFuture<Void> copyFilesInOneDirection(File dir1, File dir2) throws IOException {
        List<String> emptyList = new ArrayList<>();
        fileComparison = new FileComparison(dir1, dir2, this, emptyList, emptyList);
        return fileComparison.compareAndCopyFiles();
    }

    @Override
    public CompletableFuture<Boolean> onGotMisMatches(java.util.List<SyncAction> syncActions) {
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



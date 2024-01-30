package dev.ytterate.ysync;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

public class FileComparison {
    void compareFilesOneWay(File dir1, File[] files1, File[] files2, ArrayList<String> differencesModel) {
        for (File file1 : files1) {
            boolean found = false;

            for (File file2 : files2) {
                if (file2.getName().equals(file1.getName())) {
                    found = true;

                    if (file1.isDirectory()) {
                        compareFilesOneWay(file1, file1.listFiles(), file2.listFiles(), differencesModel);
                    } else if (file1.lastModified() > file2.lastModified()) {
                        differencesModel.add("File: " + file1.getName() + " - in directory: " + dir1.getName() + " - last modified: " + new Date(file1.lastModified()));
                    }

                    break;
                }
            }
            if (!found) {
                differencesModel.add("File: " + file1.getName() + " - in directory: " + dir1.getName() + " - last modified: " + new Date(file1.lastModified()));
            }
        }
    }
}

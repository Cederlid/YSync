package dev.ytterate.ysync.cmd;

import com.beust.jcommander.Parameter;

import java.util.ArrayList;
import java.util.List;

public class CommandLineArgs {

    @Parameter(description = "Source and destination directories", required = true)
    public List<String> directories = new ArrayList<>();
    @Parameter(names = {"--copy", "-c"}, description = "Files to copy")
    public List<String> filesToCopy = new ArrayList<>();

    @Parameter(names = {"--ignore", "-i"}, description = "Files to ignore")
    public List<String> ignoredFiles = new ArrayList<>();

    @Parameter(names = {"--delete", "-D"}, description = "Delete files in destination directory if they don't exist in source directory")
    public boolean deleteFiles;

}

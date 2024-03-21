package dev.ytterate.ysync.cmd;

import com.beust.jcommander.Parameter;

import java.util.ArrayList;
import java.util.List;

public class CommandLineArgs {

    @Parameter(description = "Source and destination directories", required = true)
    public List<String> directories = new ArrayList<>();

    @Parameter(names = "--dryrun", description = "Show the list of mismatches, but doesn't copy any files")
    public List<String> showMismatchesWithoutCopy = new ArrayList<>();

    @Parameter(names = "--report", description = "Show the list of mismatches, but skip the user input and just copies everything else")
    public List<String> showMismatchesAndCopyNoUserInput = new ArrayList<>();

    @Parameter(names = {"--copy", "-c"}, description = "Files to copy")
    public List<String> filesToCopy = new ArrayList<>();

    @Parameter(names = {"--ignore", "-i"}, description = "Files to ignore")
    public List<String> ignoredFiles = new ArrayList<>();
    @Parameter(names = {"--delete", "-D"}, description = "Delete files in destination directory if they don't exist in source directory")
    public boolean deleteFiles;

}

package dev.ytterate.ysync.cmd;

import com.beust.jcommander.Parameter;

import java.util.ArrayList;
import java.util.List;

public class CommandLineArgs {

    @Parameter(names = {"--source", "-s"}, description = "Source directory", required = true)
    public String sourceDirectory;

    @Parameter(names = {"--destination", "-d"}, description = "Destination directory", required = true)
    public String destinationDirectory;

    @Parameter(names = {"--copy", "-c"}, description = "Files to copy")
    public List<String> filesToCopy = new ArrayList<>();

    @Parameter(names = {"--ignore", "-i"}, description = "Files to ignore")
    public List<String> ignoredFiles = new ArrayList<>();

}

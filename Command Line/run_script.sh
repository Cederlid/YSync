#!/bin/bash

java -Dfile.encoding=UTF-8 \
     -Dsun.stdout.encoding=UTF-8 \
     -Dsun.stderr.encoding=UTF-8 \
     -classpath "/Users/wijdancederlid/Ytterate/ysync/out/production/Command Line:/Users/wijdancederlid/Ytterate/ysync/out/production/Library:/Users/wijdancederlid/.m2/repository/commons-io/commons-io/2.15.1/commons-io-2.15.1.jar:/Users/wijdancederlid/.m2/repository/org/json/json/20231013/json-20231013.jar:/Users/wijdancederlid/.m2/repository/com/beust/jcommander/1.82/jcommander-1.82.jar:/Users/wijdancederlid/Library/Application Support/JetBrains/Toolbox/apps/IDEA-U/ch-0/233.13135.103/IntelliJ IDEA.app/Contents/lib/idea_rt.jar" \
     dev.ytterate.ysync.cmd.Main \
     -s /Users/wijdancederlid/Desktop/Directory1 \
     -d /Users/wijdancederlid/Desktop/Directory3 \
     "$@"
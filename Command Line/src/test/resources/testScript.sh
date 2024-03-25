#!/bin/bash
pwd
ls
rm -rf /Users/wijdancederlid/Desktop/Directory1/
rm -rf /Users/wijdancederlid/Desktop/Directory3/
cp -r /Users/wijdancederlid/Desktop/backup/Directory1 /Users/wijdancederlid/Desktop/
cp -r /Users/wijdancederlid/Desktop/backup/Directory3 /Users/wijdancederlid/Desktop/

cd ../../..
pwd
./run_script.sh /Users/wijdancederlid/Desktop/Directory1/ /Users/wijdancederlid/Desktop/Directory3/
cd -


#!/bin/bash
#set -x
BASH_FILE="${BASH_SOURCE[0]}"
SCRIPT_DIR=$(dirname "$BASH_FILE")

FILE_DIR="$SCRIPT_DIR"
BACKUP_DIR="$SCRIPT_DIR"/masterDir

rm -rf "$FILE_DIR/Directory1/"
rm -rf "$FILE_DIR/Directory3/"
cp -r "$BACKUP_DIR/Directory1" "$FILE_DIR"
cp -r "$BACKUP_DIR/Directory3" "$FILE_DIR"

"$SCRIPT_DIR/../../../../run_script.sh" "$@" "$FILE_DIR/Directory1/" "$FILE_DIR/Directory3/"
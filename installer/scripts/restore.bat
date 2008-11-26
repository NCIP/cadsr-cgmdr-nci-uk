@echo off

call "$INSTALL_PATH/bin/backup.bat" -ouri=xmldb:exist:/// -u %1 -p %2 -r %3

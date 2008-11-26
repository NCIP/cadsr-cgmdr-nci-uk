#!/bin/bash

set +v

# will be set by the installer
if [ -z "$EXIST_HOME" ]; then
	EXIST_HOME="%{INSTALL_PATH}"
fi

if [ -z "$JAVA_HOME" ]; then
    JAVA_HOME="%{JDKPath}"
fi

"%{INSTALL_PATH}/bin/backup.sh" -ouri=xmldb:exist:/// -u $1 -p $2 -r $3

exit 0
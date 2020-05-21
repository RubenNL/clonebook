#!/bin/bash
/usr/bin/rsync --exclude WEB-INF --exclude META-INF src/main/webapp/* pi3:ipassFolder &

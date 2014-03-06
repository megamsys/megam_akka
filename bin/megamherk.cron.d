# This is a cron that wakes up and runs every 10 minutes. 
# The cron activates as cleanup script herk_stash 
#
# Megam.  http://www.gomegam.com
#
# Copyright 2014, Megam systems
#
MAILTO=""
* * * * * root @@MEGAM_HOME@@/bin/herk_stash

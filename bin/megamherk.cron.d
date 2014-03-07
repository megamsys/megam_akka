# This is a cron that wakes up and runs every 10 minutes. 
# The cron activates a cleanup script herk_stash 
#
# Megam.  http://www.gomegam.com
#
# Copyright 2014, Megam systems
#
MAILTO=""
*/10 * * * * root @@MEGAM_HOME@@/bin/herk_stash

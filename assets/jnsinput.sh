# ! /system/bin/sh
# Script to start "jnsinput" on the device, which has a very rudimentary
# shell.
#
export CLASSPATH=/mnt/sdcard/jnsinput/jnsinput.jar; 
exec app_process /system/bin  com.blueocean.jnsinput.JNSInputServer "$@"

#
# An example...
#
alias ll='ls -lias'
alias lll='ls -liash'

parse_git_branch() {
     git branch 2> /dev/null | sed -e '/^[^*]/d' -e 's/* \(.*\)/ (\1)/'
}
# IP_ADDR=$(hostname -I | awk '{$1=$1;print}' | sed 's/ /, /g')
IP_ADDR=$(hostname -I | awk '{ print $1 }')
PS1="\[\033[01;32m\]\u@\h\[\033[00m\] [IP:${IP_ADDR}]:\[\033[01;34m\]\w\[\033[33m\]\$(parse_git_branch)\[\033[00m\] $ "
#
~/sysinfo.sh
#
#THIS MUST BE AT THE END OF THE FILE FOR SDKMAN TO WORK!!!
export SDKMAN_DIR="/home/pi/.sdkman"
[[ -s "/home/pi/.sdkman/bin/sdkman-init.sh" ]] && source "/home/pi/.sdkman/bin/sdkman-init.sh"

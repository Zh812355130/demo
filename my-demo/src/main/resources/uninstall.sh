#!/bin/bash

install_path=/home/huan/nginx

printf "\n<<<<<< stop nginx service \n"
systemctl stop nginx
systemctl disable nginx

if [ -f /etc/systemd/system/nginx.service ];then
        rm -f /etc/systemd/system/nginx.service
fi
systemctl daemon-reload

printf "\n<<<<< remove file \n"
rm -f /etc/profile.d/nginx.sh
source /etc/profile

rm -rf ${install_path}
printf "<<<<< uninstall completed\n"

unset install_path
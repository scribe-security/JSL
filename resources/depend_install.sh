#!/bin/sh
set -x

if [ ! $(which  jq) ] || [ ! $(which  bash) ]  || [ ! $(which  git) ]; then

if which apk; then 
    echo "APK found installing depend"
    apk add bash
    apk add jq
    apk add git
    # echo 'http://dl-cdn.alpinelinux.org/alpine/v3.9/main' >> /etc/apk/repositories
    # echo 'http://dl-cdn.alpinelinux.org/alpine/v3.9/community' >> /etc/apk/repositories
    # apk add mongodb yaml-cpp=0.6.2-r2
    # echo "127.0.0.1 mongodb" >> /etc/hosts
    exit 0
fi



if which apt-get; then 
    who

    if which curl; then
        JQ=/usr/bin/jq
        curl https://stedolan.github.io/jq/download/linux64/jq > $JQ && chmod +x $JQ
    fi

    if which sudo; then 
        echo "SUDO APT-GET found installing depend"
        sudo apt-get install -y bash git
    else
        echo "APT-GET found installing depend"
        apt-get install -y bash jq git
    fi
    exit 0
fi


if which apt; then 
    who
    if which sudo; then 
        echo "SUDO APT found installing depend"
        sudo apt install -y bash jq git
    else
        echo "APT found installing depend"
        apt install -y bash jq git
    fi
    exit 0
fi

fi
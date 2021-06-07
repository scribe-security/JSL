#!/bin/sh
set -x

if [ ! $(which  jq) ] || [ ! $(which  bash) ]  || [ ! $(which  git) ] || [ ! $(which  diff) ] || [ ! $(which  tar) ] || [ ! $(which  stat) ]; then
    if which apk; then 
        echo "APK found installing depend"
        apk add bash
        apk add jq
        apk add git
        apk add diffutils
        apk add tar
        apk add coreutils
        exit 0
    fi



    if which apt-get; then 
        who
        echo "APT-GET found installing depend"
        apt-get update
        apt-get install -y bash jq git diffutils tar coreutils
        exit 0
    fi


    if which apt; then 
        echo "APT found installing depend"
        apt update
        apt install -y bash git js diffutils tar coreutils
        exit 0
    fi
fi
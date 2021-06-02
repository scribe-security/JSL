#!/bin/sh

if which apk; then 
    echo "APK found installing depend"
    apk add bash
    apk add jq
    apk add git
    # echo 'http://dl-cdn.alpinelinux.org/alpine/v3.9/main' >> /etc/apk/repositories
    # echo 'http://dl-cdn.alpinelinux.org/alpine/v3.9/community' >> /etc/apk/repositories
    # apk add mongodb yaml-cpp=0.6.2-r2
    # echo "127.0.0.1 mongodb" >> /etc/hosts
fi
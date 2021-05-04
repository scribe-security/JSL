#!/bin/bash
# - DO NOT THIS SCRIPT IN ACTUAL PRODUCT  (only for internal testing)
# NOTE: INPUT is places in jenkins shell step -  Token Recognition and parameter expantion will run from input if found.
# I have tried to prevent this (see weird sed in docker_inspect) but have not found a solution - because this is just a base script for our own learning process
# i have let it go 
set -x
tempfile() {
    tempprefix=$(basename "$0")
    mktemp /tmp/${tempprefix}.XXXXXX
}

docker_inspect()
{
    REGEX=$2
    docker image inspect  $(docker image ls |  awk "{print \$1}" | egrep $REGEX)|  sed -e 's/\$(/\\\\%(/g' | jq -n '.IMAGES = inputs' | \
        jq '.JOB_NAME += "'${JOB_NAME}'"' | \
        jq '.BUILD_TAG += "'${BUILD_TAG}'"' | \
        jq '.GITHUB_REPO += "'${GITHUB_REPO}'"' | \
        jq '.STAGE_NAME += "'${STAGE_NAME}'"'

    exit 0
}

git_history()
{
    WORKDIR=$(pwd)
    HISTORY_TMP=$(tempfile)
    trap 'rm -f $HISTORY_TMP' EXIT
    if git -C . rev-parse 2> /dev/null; then
        # HISTORY=$(git --no-pager log \
        # --pretty=format:'{%n  "commit": "%H",%n  "abbreviated_commit": "%h",%n  "tree": "%T",%n  "abbreviated_tree": "%t",%n  "parent": "%P",%n  "abbreviated_parent": "%p",%n  "refs": "%D",%n  "encoding": "%e",%n  "subject": "%s",%n  "sanitized_subject_line": "%f" ,%n  "commit_notes": "%N",%n  "author": {%n    "name": "%aN",%n    "email": "%aE",%n    "date": "%aD"%n  },%n  "commiter": {%n    "name": "%cN",%n    "email": "%cE",%n    "date": "%cD"%n  }  %n},' | \
        # sed "$ s/,$//" | \
        # sed ':a;N;$!ba;s/\r\n\([^{]\)/\\n\1/g' | \
        # awk 'BEGIN { print("[") } { print($0) } END { print("]") }')

        git --no-pager log \
            --pretty=format:'{%n  "commit": "%H",%n  "abbreviated_commit": "%h",%n  "tree": "%T",%n  "abbreviated_tree": "%t",%n  "parent": "%P",%n  "abbreviated_parent": "%p",%n  "refs": "%D",%n  "encoding": "%e",%n  "subject": "%s",%n  "sanitized_subject_line": "%f" ,%n  "commit_notes": "%N",%n  "author": {%n    "name": "%aN",%n    "email": "%aE",%n    "date": "%aD"%n  },%n  "commiter": {%n    "name": "%cN",%n    "email": "%cE",%n    "date": "%cD"%n  }  %n},' | \
            sed "$ s/,$//" | \
            sed ':a;N;$!ba;s/\r\n\([^{]\)/\\n\1/g' | \
            awk 'BEGIN { print("[") } { print($0) } END { print("]") }' > $HISTORY_TMP

        jq -n --arg REPODIR "$WORKDIR" --slurpfile HISTORY $HISTORY_TMP '{REPODIR: $REPODIR, HISTORY: $HISTORY}' | \
        jq '.JOB_NAME += "'${JOB_NAME}'"' | \
        jq '.BUILD_TAG += "'${BUILD_TAG}'"' | \
        jq '.GITHUB_REPO += "'${GITHUB_REPO}'"' | \
        jq '.STAGE_NAME += "'${STAGE_NAME}'"'
    fi
    exit 0
}

add_os_envs() {
    export OS=$(lsb_release -s -i -c -r)
    export UPTIME=$(uptime)
    export ARCHITECTURE=$(uname -m)
    export KERNEL=$(uname -s -r)
    export VENDOR=$(cat /sys/class/dmi/id/chassis_vendor)
    export PRODUCT_NAME=$(cat /sys/class/dmi/id/product_name)
    export SERIAL_NUM=$(cat /sys/class/dmi/id/product_serial)
    export PROCESSOR_NAME=$(awk -F':' '/^model name/ {print $2}' /proc/cpuinfo | uniq | sed -e 's/^[ \t]*//')
    export HOST_IP=$(hostname -i)
}

env()
{
    add_os_envs
    SAMPLE_NAME=$1
    jq -n env
    exit 0
}

hash_files()
{
    SAMPLE_NAME=$1
    WORKDIR=$(pwd)
    find . -type f -name "*" | 
    while read line; do 
        jq -n --arg name "$(basename "$line")" --arg HASH "$(sha256sum $line | awk '{ print $1 }')" --arg path "$line" '{name: $name, path: $path, hash: $HASH}'
    done | jq -n '.files |= [inputs]' | jq '.WORKDIR += "'${WORKDIR}'"' | \
        jq '.JOB_NAME += "'${JOB_NAME}'"' | \
        jq '.BUILD_TAG += "'${BUILD_TAG}'"' | \
        jq '.GITHUB_REPO += "'${GITHUB_REPO}'"' | \
        jq '.STAGE_NAME += "'${STAGE_NAME}'"'
    exit 0
}

opt=$1
REGEX=$2

JOB_NAME=$(sed 's/ /_/g' <<< "$JOB_NAME")
BUILD_TAG=$(sed 's/ /_/g' <<< "$BUILD_TAG")
GITHUB_REPO=$(sed 's/ /_/g' <<< "$GITHUB_REPO")
STAGE_NAME=$(sed 's/ /_/g' <<< "$STAGE_NAME")

case $opt
in
    env) env $SAMPLE_NAME;;
    hash_files) hash_files $SAMPLE_NAME;;
    git_history) git_history $SAMPLE_NAME;;
    docker_inspect) docker_inspect $SAMPLE_NAME $REGEX;;
    *) echo "Nothing to do"
       exit 1;;
esac
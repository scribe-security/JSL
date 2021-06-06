#!/bin/bash
# - DO NOT THIS SCRIPT IN ACTUAL PRODUCT
# Library shoudl be used only pipeline analysis purposes

tempfile() {
    tempprefix=$(basename "$0")
    mktemp /tmp/${tempprefix}.XXXXXX
}

docker_inspect()
{
    # NOTE: Base Token Recognition and parameter expantion is run on input (script injection vulnrability).
    # I have tried to prevent this (see weird sed in docker_inspect) but have not found a better solution yet - go implementation will prevent such weird logic and such
    SAMPLE_NAME=$1
    REGEX=$1
    docker image inspect  $(docker image ls |  awk "{print \$1}" | egrep $REGEX)|  sed -e 's/\$(/\\\\%(/g' | jq -n '.IMAGES = inputs' | \
        jq '.SAMPLE_NAME += "'${SAMPLE_NAME}'"' | \
        jq '.JOB_NAME += "'${JOB_NAME}'"' | \
        jq '.BUILD_TAG += "'${BUILD_TAG}'"' | \
        jq '.GIT_URL += "'${GIT_URL}'"' | \
        jq '.STAGE_NAME += "'${STAGE_NAME}'"'

    exit 0
}

git_history()
{
    SAMPLE_NAME=$1
    WORKDIR=$(pwd)
    HISTORY_TMP=$(tempfile)
    trap 'rm -f $HISTORY_TMP' EXIT
    if git -C . rev-parse 2> /dev/null; then
        git --no-pager log \
            --pretty=format:'{%n  "commit": "%H",%n  "abbreviated_commit": "%h",%n  "tree": "%T",%n  "abbreviated_tree": "%t",%n  "parent": "%P",%n  "abbreviated_parent": "%p",%n  "refs": "%D",%n  "encoding": "%e",%n  "subject": "%s",%n  "sanitized_subject_line": "%f" ,%n  "commit_notes": "%N",%n  "author": {%n    "name": "%aN",%n    "email": "%aE",%n    "date": "%aD"%n  },%n  "commiter": {%n    "name": "%cN",%n    "email": "%cE",%n    "date": "%cD"%n  }  %n},' | \
            sed "$ s/,$//" | \
            sed ':a;N;$!ba;s/\r\n\([^{]\)/\\n\1/g' | \
            awk 'BEGIN { print("[") } { print($0) } END { print("]") }' > $HISTORY_TMP

        jq -n --arg REPODIR "$WORKDIR" --slurpfile HISTORY $HISTORY_TMP '{REPODIR: $REPODIR, HISTORY: $HISTORY}' | \
        jq '.SAMPLE_NAME += "'${SAMPLE_NAME}'"' | \
        jq '.JOB_NAME += "'${JOB_NAME}'"' | \
        jq '.BUILD_TAG += "'${BUILD_TAG}'"' | \
        jq '.GIT_URL += "'${GIT_URL}'"' | \
        jq '.STAGE_NAME += "'${STAGE_NAME}'"'
    fi
    return 0
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
    return 0
}

# 2DO should we blacklist '.git?'
hash_files()
{
    SAMPLE_NAME=$1
    WORKDIR=$(pwd)
    find . -type f -name "*" -not -path "*/.git/*" -not -path "*/samples/*" | 
    while read line; do 
        jq -n --arg name "$(basename "$line")" --arg HASH "$(sha256sum $line | awk '{ print $1 }')" --arg path "$line" '{name: $name, path: $path, hash: $HASH}'
    done | jq -n '.files |= [inputs]' | jq '.WORKDIR += "'${WORKDIR}'"' | \
        jq '.SAMPLE_NAME += "'${SAMPLE_NAME}'"' | \
        jq '.JOB_NAME += "'${JOB_NAME}'"' | \
        jq '.BUILD_TAG += "'${BUILD_TAG}'"' | \
        jq '.GIT_URL += "'${GIT_URL}'"' | \
        jq '.STAGE_NAME += "'${STAGE_NAME}'"'

    return 0
}

sample_diff()
{
    SAMPLE_NAME=$1
    PREV_SAMPLE_STATE=$2
    diff --exclude=diff* -U2 samples/$PREV_SAMPLE_STATE samples/$SAMPLE_NAME
}

write_sample_state(){
    SAMPLE_NAME=$1
    echo $SAMPLE_NAME > samples/samples_state.txt
    SAMPLE_STATE=$SAMPLE_NAME
}

read_sample_state(){
    SAMPLE_NAME=$1
    SAMPLE_STATE=`cat samples/samples_state.txt || echo $SAMPLE_NAME` 
}

# set -x

opt=$1
SAMPLE_NAME=$2

# JOB_NAME=job_stab
# BUILD_TAG=build_tag_stab
# GIT_URL=git_url_stab
# STAGE_NAME=stage_name_stab
# mkdir -p "samples/$STAGE_NAME/$SAMPLE_NAME/"

sample_by_type()
{
    opt=$1
    SAMPLE_NAME=$2
    PREV_SAMPLE_STATE=$3
    JOB_NAME=$(sed 's/ /_/g' <<< "$JOB_NAME")
    BUILD_TAG=$(sed 's/ /_/g' <<< "$BUILD_TAG")
    GIT_URL=$(sed 's/ /_/g' <<< "$GIT_URL")
    STAGE_NAME=$(sed 's/ /_/g' <<< "$STAGE_NAME")

    case $opt
    in
        env) env $SAMPLE_NAME;;
        hash_files) hash_files $SAMPLE_NAME;;
        git_history) git_history $SAMPLE_NAME;;
        docker_inspect) docker_inspect $SAMPLE_NAME;;
        diff) sample_diff $SAMPLE_NAME $PREV_SAMPLE_STATE;;
        all)
             env $SAMPLE_NAME > "samples/$SAMPLE_NAME/env.json"
             git_history $SAMPLE_NAME  > "samples/$SAMPLE_NAME/git_history.json"
             hash_files $SAMPLE_NAME  > "samples/$SAMPLE_NAME/hash_files.json"
             sample_diff $SAMPLE_NAME $PREV_SAMPLE_STATE > "samples/$SAMPLE_NAME/diff.json"
        ;;
        *) echo "Nothing to do"
        exit 1;;
    esac
} 

mkdir -p samples/$SAMPLE_NAME 2> /dev/null
if [ "$opt" == "all" ]; then
    sample_by_type $opt $SAMPLE_NAME $SAMPLE_STATE
    exit 0
fi

read_sample_state $SAMPLE_NAME
sample_by_type $opt $SAMPLE_NAME $SAMPLE_STATE> "samples/$SAMPLE_NAME/$opt.json"
write_sample_state $SAMPLE_NAME

rm -rf "samples/$SAMPLE_NAME/all.json"
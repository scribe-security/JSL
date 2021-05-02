#!/bin/bash
# - DO NOT THIS SCRIPT IN ACTUAL PRODUCT  (only for internal testing)
# NOTE: INPUT is places in jenkins shell step -  Token Recognition and parameter expantion will run from input if found.
# I have tried to prevent this (see weird sed in docker_inspect) but have not found a solution - because this is just a base script for our own learning process
# i have let it go 

docker_inspect()
{
    SAMPLE_NAME=$1
    REGEX=$2
    docker image inspect  $(docker image ls |  awk "{print \$1}" | egrep $REGEX)|  sed -e 's/\$(/\\\\%(/g' | jq -n '.IMAGES = inputs' | jq '.SAMPLE += "'${SAMPLE_NAME}'"'
    exit 0
}

git_history()
{
    SAMPLE_NAME=$1
    WORKDIR=$(pwd)
    if git -C . rev-parse 2> /dev/null; then
        HISTORY=$(git --no-pager log \
        --pretty=format:'{%n  "commit": "%H",%n  "abbreviated_commit": "%h",%n  "tree": "%T",%n  "abbreviated_tree": "%t",%n  "parent": "%P",%n  "abbreviated_parent": "%p",%n  "refs": "%D",%n  "encoding": "%e",%n  "subject": "%s",%n  "sanitized_subject_line": "%f" ,%n  "commit_notes": "%N",%n  "author": {%n    "name": "%aN",%n    "email": "%aE",%n    "date": "%aD"%n  },%n  "commiter": {%n    "name": "%cN",%n    "email": "%cE",%n    "date": "%cD"%n  }  %n},' | \
        sed "$ s/,$//" | \
        sed ':a;N;$!ba;s/\r\n\([^{]\)/\\n\1/g' | \
        awk 'BEGIN { print("[") } { print($0) } END { print("]") }')
        jq -n --arg REPODIR "$WORKDIR" --argjson HISTORY "$HISTORY" '{REPODIR: $REPODIR, HISTORY: $HISTORY}' | jq '.SAMPLE += "'${SAMPLE_NAME}'"'
    fi
    exit 0
}

env()
{
    SAMPLE_NAME=$1
    jq -n env  | jq '.SAMPLE += "'${SAMPLE_NAME}'"'
    exit 0
}

hash_files()
{
    SAMPLE_NAME=$1
    WORKDIR=$(pwd)
    find . -type f -name "*" | 
    while read line; do 
        jq -n --arg name "$(basename "$line")" --arg HASH "$(sha256sum $line | awk '{ print $1 }')" --arg path "$line" '{name: $name, path: $path, hash: $HASH}'
    done | jq -n '.files |= [inputs]' | jq '.WORKDIR += "'${WORKDIR}'"' | jq '.SAMPLE += "'${SAMPLE_NAME}'"'
    exit 0
}

opt=$1
SAMPLE_NAME=$2
REGEX=$3
case $opt
in
    env) env $SAMPLE_NAME;;
    hash_files) hash_files $SAMPLE_NAME;;
    git_history) git_history $SAMPLE_NAME;;
    docker_inspect) docker_inspect $SAMPLE_NAME $REGEX;;
    *) echo "Nothing to do"
       exit 1;;
esac
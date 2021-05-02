#!groovy
import jenkins.pipeline.lib.Constants


def MongoDBScript(String script) {
    OUT = sh(script: """#!/bin/bash
    set -x
    mongo mongodb://mongodb <<EOF
    use scribe
    ${script}
EOF""".stripIndent(),returnStdout: true)
    echo "Mongo script out: ${OUT}"
    return OUT
}

def ListWorkSpaceFiles() {
    return findFiles(glob: '**/*.sh')
}

def InitMongoDb() {
    MongoDBScript('''
    db.createCollection("env")
    db.createCollection("files")
    db.createCollection("git_history")
    db.createCollection("docker_inspect")
    '''
    )
}

def DeleteAll() {
    MongoDBScript('''
    db.env.remove({})
    db.files.remove({})
    db.git_history.remove({})
    db.docker_inspect.remove({})
    '''
    )
}

def HashFiles(String sample_name) {
    FILE_JSON = sh(script: """bash collect_scribe_info.sh hash_files ${sample_name}""",returnStdout: true)
    echo "FILE_JSON: ${FILE_JSON}"
    MongoDBScript("""
    db.files.insertOne(${FILE_JSON})"""
    )
}

def Env(String sample_name) {
    ENV = sh(script: """bash collect_scribe_info.sh env ${sample_name}""",returnStdout: true)
    echo "ENV: ${ENV}"
    MongoDBScript("""
    db.env.insertOne(${ENV})"""
    )
}

def GitHistory(String sample_name) {
    HISTORY = sh(script: """bash collect_scribe_info.sh git_history ${sample_name}""",returnStdout: true)
    echo "HISTORY: ${HISTORY}"
    MongoDBScript("""
    db.git_history.insertOne(${HISTORY})"""
    )
}

def DockerInspect(String sample_name, String docker_regex) {
    INSPECT = sh(script: """bash collect_scribe_info.sh docker_inspect ${sample_name} ${docker_regex}""",returnStdout: true)
    echo "INSPECT: ${INSPECT}"
    MongoDBScript("""
    db.docker_inspect.insertOne(${INSPECT})"""
    )
}

def Sample(String sample_name, String docker_regex) {
    echo "Running sample funnction "
    // GitHistory(sample_name)
    // HashFiles(sample_name)
    // Env(sample_name)
    // DockerInspect(sample_name, docker_regex)
}

def call(args) {
    echo "Running sample call"
    Sample("Test", "Test")
    def sample = args
    if (args == null || (args instanceof String && args.trim().isEmpty())) {
        sample = Constants.DEFAULT_MAINTAINER_NAME
    }
    echo "Project maintained by $sample"
}
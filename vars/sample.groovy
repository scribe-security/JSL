#!groovy
import jenkins.pipeline.lib.Constants
import groovy.transform.SourceURI
import java.nio.file.Path
import java.nio.file.Paths

class ScriptSourceUri {
    @SourceURI
    static URI uri
}

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
    MongoDBScript("""
    db.env.remove({ JOB_NAME: "${JOB_NAME}" })
    db.files.remove({ JOB_NAME: "${JOB_NAME}" })
    db.git_history.remove({ JOB_NAME: "${JOB_NAME}" })
    db.docker_inspect.remove({ JOB_NAME: "${JOB_NAME}" })
    """
    )
}

def HashFiles(String name) {
    FILE_JSON = sh(script: """bash collect_scribe_info.sh hash_files ${name}""",returnStdout: true)
    echo "FILE_JSON: ${FILE_JSON}"
}

def Env(String name) {
    ENV = sh(script: """bash collect_scribe_info.sh env ${name}""",returnStdout: true)
    echo "ENV: ${ENV}"
}

def GitHistory(String name) {
    HISTORY = sh(script: """bash collect_scribe_info.sh git_history ${name}""",returnStdout: true)
    echo "HISTORY: ${HISTORY}"
}

def DockerInspect(String name, String docker_regex) {
    INSPECT = sh(script: """bash collect_scribe_info.sh docker_inspect ${name}""",returnStdout: true)
    echo "INSPECT: ${INSPECT}"
}

def Sample(String name, String docker_regex) {
    GitHistory(name)
    HashFiles(name)
    Env(name)
    // DockerInspect(docker_regex)
}

def call(String name, String docker_regex= "*", Boolean delete_samples = false, Boolean depend_install = true) {
    echo "Sampling  $name $docker_regex $delete_samples $depend_install"

    // Path scriptLocation = Paths.get(ScriptSourceUri.uri)
    // def script_path = scriptLocation.getParent().getParent().resolve('resources').toString()
    // echo "script_path: ${script_path}"
    writeFile file:'collect_scribe_info.sh', text:libraryResource("collect_scribe_info.sh")

    if (depend_install == true) {
        echo "Trying to install script depends"
        DEPEND_INSTALL = sh(script: libraryResource("depend_install.sh"),returnStdout: true)
        echo "DEPEND_INSTALL: ${DEPEND_INSTALL}"
    }
    
    // env.STAGE_NAME
    // env.GITHUB_REPO
    // env.JOB_NAME
    // env.BUILD_TAG  

    echo "Running sample funnction $stage_name"
    Sample(name, docker_regex)
}
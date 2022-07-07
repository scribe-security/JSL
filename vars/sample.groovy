#!groovy
import jenkins.pipeline.lib.Constants
import groovy.transform.SourceURI
import java.nio.file.Path
import java.nio.file.Paths

class ScriptSourceUri {
    @SourceURI
    static URI uri
}

def ListJson(String name) {
    def json_files
    dir ("""samples/${name}""") {
      json_files = findFiles(glob: """*.json""")
    }
    return json_files
}


def PublishSample(String name) {
    def file_list = []
    for (f in  ListJson(name)) {
      if (! f.directory) {
        echo """Publishing ${f.name} ${f.path} ${f.directory} ${f.length} ${f.lastModified}"""
        file_list.add(f.path)
      }
    }
    def obj_list_files
    obj_list_files = ListJson(name)
    echo """File list ${file_list} ${obj_list_files}"""

    publishHTML (target : [allowMissing: false,
        alwaysLinkToLastBuild: true,
        keepAll: true,
        reportDir: """samples/${name}""",
        reportFiles: obj_list_files.join(','),
        reportName: name])
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

def DockerInspect(String name) {
    INSPECT = sh(script: """bash collect_scribe_info.sh docker_inspect ${name}""",returnStdout: true)
    echo "INSPECT: ${INSPECT}"
}

def Diff(String name) {
    DIFF = sh(script: """bash collect_scribe_info.sh diff ${name}""",returnStdout: true)
    echo "Diff: ${DIFF}"
}

def Sample(String name) {
    ALL = sh(script: """bash collect_scribe_info.sh all ${name}""",returnStdout: true)
    echo "ALL: ${ALL}"
}

def ReadDiff(String name) {
    prev = "test_sample"
    INSPECT = sh(script: """bash collect_scribe_info.sh diff ${name} ${prev}""",returnStdout: true)
}


// def call(String target,
//   String verbose,
//   String config,
//   String format,
//   String output_directory,
//   String output_file,
//   String name,
//   String[] env,
//   String[] label,
//   String filter_regex,
//   String collect_regex,
//   Boolean force,
//   String attest_config,
//   String attest_name,
//   String attest_default,
//   Boolean scribe_enable,
//   String scribe_url,
//   String scribe_loginurl,
//   String scribe_audience,
//   String context_dir) {
//     echo "Bom  - $target"
// }

def call(String target, Boolean install_enable = true, Boolean publish_enable = true) {
    echo "Sampling  Sample name: $target, dependency install: $install_enable, publish result: $publish_enable"
    writeFile file:'collect_scribe_info.sh', text:libraryResource("collect_scribe_info.sh")

    if (install_enable == true) {
        echo "Trying to install script depends"
        DEPEND_INSTALL = sh(script: libraryResource("depend_install.sh"),returnStdout: true)
        echo "DEPEND_INSTALL: ${DEPEND_INSTALL}"
    }

    Sample(target)
    if (publish_enable == true) {
        PublishSample(target)
    }
}
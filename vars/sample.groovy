#!groovy
import groovy.transform.SourceURI
import java.nio.file.Path
import java.nio.file.Paths

class ScriptSourceUri {
    @SourceURI
    static URI uri
}

def FindFiles(String directory, String ext="json") {
    def files
    dir (directory) {
      files = findFiles(glob: """**/*.${ext}""")
    }
    return files
}

def PublishReport(String name="scribe", String directory="scribe") {
    def file_list = []
    try {
      for (f in  FindFiles(directory)) {
        if (! f.directory) {
          echo """Publishing ${f.name} ${f.path} ${f.directory} ${f.length} ${f.lastModified}"""
          file_list.add(f.path)
        }
      }

      publishHTML (target : [allowMissing: false,
          alwaysLinkToLastBuild: true,
          keepAll: true,
          reportDir: directory,
          reportFiles: file_list.join(','),
          reportName: name])
    }
    catch (Exception e) {
      echo 'Exception occurred: ' + e.toString()
    }
}

def call(String target,
        Integer verbose = 2,
        String config = "",
        String format = "",
        String output_directory = "",
        String output_file = "",
        String name = "",
        String[] env = [],
        String[] label  = [],
        String filter_regex = "",
        String collect_regex = "",
        Boolean force = false,
        String attest_config = "",
        String attest_name = "",
        String attest_default = "",
        Boolean scribe_enable = false,
        String scribe_url = "",
        String scribe_loginurl = "",
        String scribe_audience = "",
        String context_dir = ""

) {
    def command = []
    
    command.add("bomber")
    command.add(target)
    command.add("--verbose")
    command.add(verbose.toString())
    
    if (config != "") {
        command.add("--config")
        command.add(config)
    }

    if (format != "") {
        command.add("--format")
        command.add(format)
    }
    if (output_directory != "") {
        command.add("--output-directory")
        command.add(output_directory)
    }
    if (output_file != "") {
        command.add("--output-file")
        command.add(output_file)
    }
    if (name != "") {
        command.add("--name")
        command.add(name)
    }
        //     String[] env = [],
        // String[] label  = [],

    if (filter_regex != "") {
        command.add("--filter-regex")
        command.add(filter_regex)
    }
    if (collect_regex != "") {
        command.add("--collect-regex")
        command.add(collect_regex)
    }

    if (force) {
        command.add("-f")
    }

    if (attest_config != "") {
        command.add("--attest.config")
        command.add(attest_config)
    }
    if (attest_name != "") {
        command.add("--attest.name")
        command.add(attest_name)
    }
    if (attest_default != "") {
        command.add("--attest.default")
        command.add(attest_default)
    }
    if (collect_regex != "") {
        command.add("--collect-regex")
        command.add(collect_regex)
    }    

    if (scribe_enable) {
        command.add("-E")
    }

    if (scribe_url != "") {
        command.add("--scribe.url")
        command.add(scribe_url)
    }    
    if (scribe_loginurl != "") {
        command.add("--scribe.loginurl")
        command.add(scribe_loginurl)
    }
    if (scribe_audience != "") {
        command.add("--scribe.auth0.audience")
        command.add(scribe_audience)
    }  
    if (context_dir != "") {
        command.add("--context_dir")
        command.add(context_dir)
    }

    command.add("--context-type")
    command.add("jenkins")
   
    def commandStr = command.join(' ')
    echo "Command  - $command"
    def out_data = sh(script: "${commandStr}",returnStdout: true)
    println out_data
}
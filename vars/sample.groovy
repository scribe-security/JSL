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

def call(Map conf) {
    println conf
    def command = []
    
    command.add("bomber")
    command.add("bom")

    command.add(conf.target)
    command.add("--verbose")
    command.add(conf.verbose.toString())
    
    if (conf.config != null ) {
        command.add("--config")
        command.add(conf.config)
    }

    if (conf.format != null) {
        command.add("--format")
        command.add(conf.format)
    }

    command.add("--output-directory")
    if (conf.output_directory != null) {
        command.add(conf.output_directory)
    } else {
        command.add("scribe/bomber")
    }

    if (conf.output_file != null) {
        command.add("--output-file")
        command.add(conf.output_file)
    }
    if (conf.name != null) {
        command.add("--name")
        command.add(conf.name)
    }
        //     String[] env = [],
        // String[] label  = [],

    if (conf.filter_regex != null) {
        command.add("--filter-regex")
        command.add(conf.filter_regex)
    }
    if (conf.collect_regex != null) {
        command.add("--collect-regex")
        command.add(conf.collect_regex)
    }

    if (conf.force == true ) {
        command.add("-f")
    }

    if (conf.attest_config != null) {
        command.add("--attest.config")
        command.add(conf.attest_config)
    }
    if (conf.attest_name != null ) {
        command.add("--attest.name")
        command.add(conf.attest_name)
    }
    if (conf.attest_default != null ) {
        command.add("--attest.default")
        command.add(conf.attest_default)
    }
    if (conf.collect_regex != null ) {
        command.add("--collect-regex")
        command.add(conf.collect_regex)
    }    

    if (conf.scribe_enable == true) {
        command.add("-E")
    }

    if (conf.scribe_url != null) {
        command.add("--scribe.url")
        command.add(conf.scribe_url)
    }    
    if (conf.scribe_loginurl != null ) {
        command.add("--scribe.loginurl")
        command.add(conf.scribe_loginurl)
    }
    if (conf.scribe_audience != null ) {
        command.add("--scribe.auth0.audience")
        command.add(conf.scribe_audience)
    }  
    if (conf.context_dir != null ) {
        command.add("--context_dir")
        command.add(conf.context_dir)
    }

    command.add("--context-type")
    command.add("jenkins")
   
    def commandStr = command.join(' ')
    echo "Command  - $command"
    def out_data = sh(script: "${commandStr}",returnStdout: true)
    println out_data
}
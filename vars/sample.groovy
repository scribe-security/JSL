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
    def file_list = []
    echo "Bom  - $target"
        // Integer verbose = 2,

    if (config != "") {
        file_list.add("--config")
        file_list.add(config)
    }
    if (format != "") {
        file_list.add("--format")
        file_list.add(format)
    }
    if (output_directory != "") {
        file_list.add("--output-directory")
        file_list.add(output_directory)
    }
    if (output_file != "") {
        file_list.add("--output-file")
        file_list.add(output_file)
    }
    if (name != "") {
        file_list.add("--name")
        file_list.add(name)
    }
        //     String[] env = [],
        // String[] label  = [],

    if (filter_regex != "") {
        file_list.add("--filter-regex")
        file_list.add(filter_regex)
    }
    if (collect_regex != "") {
        file_list.add("--collect-regex")
        file_list.add(collect_regex)
    }

    if (force) {
        file_list.add("-f")
    }

    if (attest_config != "") {
        file_list.add("--attest.config")
        file_list.add(attest_config)
    }
    if (attest_name != "") {
        file_list.add("--attest.name")
        file_list.add(attest_name)
    }
    if (attest_default != "") {
        file_list.add("--attest.default")
        file_list.add(attest_default)
    }
    if (collect_regex != "") {
        file_list.add("--collect-regex")
        file_list.add(collect_regex)
    }    

    if (scribe_enable) {
        file_list.add("-E")
    }

    if (scribe_url != "") {
        file_list.add("--scribe.url")
        file_list.add(scribe_url)
    }    
    if (scribe_loginurl != "") {
        file_list.add("--scribe.loginurl")
        file_list.add(scribe_loginurl)
    }
    if (scribe_audience != "") {
        file_list.add("--scribe.auth0.audience")
        file_list.add(scribe_audience)
    }  
    if (context_dir != "") {
        file_list.add("--context_dir")
        file_list.add(context_dir)
    }    

    file_list.add("--context.type")
    file_list.add("jenkins")
   
    echo "Params  - $file_list"
}
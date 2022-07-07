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
        String verbose = "",
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
    echo "Bom  - $target"
    Sample(target)
}
#!groovy
import java.nio.file.Path
import java.nio.file.Paths

def call(Map conf) {
    def command = []
    
    command.add("valint")
    command.add("report")

    command.add(sprintf("--verbose=%d", conf.verbose))
    
    if (conf.config != null ) {
        command.add(sprintf("--config=%s", conf.config))

    }

    command.add("--output-directory")
    if (conf.output_directory != null) {
        command.add(conf.output_directory)
    } else {
        command.add("scribe/bomber")
    }

    if (conf.output_file != null) {
        command.add(sprintf("--output_file=%s", conf.output_file))
    }

    if (conf.scribe_enable == true) {
        command.add("-E")
    }

    if (conf.scribe_url != null) {
        command.add(sprintf("--scribe.url=%s", conf.scribe_url))
    }

    if (conf.scribe_clientid != null) {
        command.add(sprintf("--scribe.clientid=%s", conf.scribe_clientid))
    } 

    if (conf.scribe_clientsecret != null) {
        command.add(sprintf("--scribe.clientsecret=%s", conf.scribe_clientsecret))
    }    

    if (conf.scribe_loginurl != null ) {
        command.add(sprintf("--scribe.loginurl=%s", conf.scribe_loginurl))
    }

    if (conf.scribe_audience != null ) {
        command.add(sprintf("--scribe.auth.audience=%s", conf.scribe_audience))
    }

    if (conf.context_dir != null ) {
        command.add(sprintf("--context_dir=%s", conf.context_dir))
    }

    if (conf.section != null) {
        command.add(sprintf("--section=%s", conf.section.join(',')))
    }

    if (conf.integrity != null) {
        command.add(sprintf("--integrity=%s", conf.integrity.join(',')))
    }

    command.add("--context-type")
    command.add("jenkins")
   
    def commandStr = command.join(' ')
    def out_data = sh(script: "${commandStr}",returnStdout: true)
    println out_data
}
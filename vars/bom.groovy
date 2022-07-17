#!groovy
import java.nio.file.Path
import java.nio.file.Paths

def call(Map conf) {
    def command = []
    
    command.add("gensbom")
    command.add("bom")

    command.add(conf.target)
    command.add(sprintf("--verbose=%d", conf.verbose))
    
    if (conf.config != null ) {
        command.add(sprintf("--config=%s", conf.config))

    }

    if (conf.format != null) {
        command.add(sprintf("--format=%s", conf.format))
    }

    command.add("--output-directory")
    if (conf.output_directory != null) {
        command.add(conf.output_directory)
    } else {
        command.add("scribe/gensbom")
    }

    if (conf.output_file != null) {
        command.add(sprintf("--output_file=%s", conf.output_file))
    }
    if (conf.name != null) {
        command.add(sprintf("--name=%s", conf.name))  
    }

    if (conf.env != null) {
        command.add(sprintf("--env=%s", conf.env.join(',')))
    }

    if (conf.label != null) {
        command.add(sprintf("--label=%s", conf.label.join(',')))
    }

    if (conf.filter_regex != null) {
        command.add(sprintf("--filter-regex=%s", conf.filter_regex))
    }

    if (conf.collect_regex != null) {
        command.add(sprintf("--collect-regex=%s", conf.collect_regex))
    }

    if (conf.force == true ) {
        command.add("-f")
    }

    if (conf.attest_config != null) {
        command.add(sprintf("--attest.config=%s", conf.attest_config))
    }

    if (conf.attest_name != null ) {
        command.add(sprintf("--attest.name=%s", conf.attest_name))
    }

    if (conf.attest_default != null ) {
        command.add(sprintf("--attest.default=%s", conf.attest_default))
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
        command.add(sprintf("--scribe.auth0.audience=%s", conf.scribe_audience))
    }  

    if (conf.context_dir != null ) {
        command.add(sprintf("--context_dir=%s", conf.context_dir))
    }

    command.add("--context-type")
    command.add("jenkins")
   
    def commandStr = command.join(' ')
    def out_data = sh(script: "${commandStr}",returnStdout: true)
    println out_data
}
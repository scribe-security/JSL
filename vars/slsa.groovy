#!groovy
import java.nio.file.Path
import java.nio.file.Paths

def call(Map conf) {
    def command = []

    command.add("valint")
    command.add("slsa")
    command.add(conf.target)

    if (conf.verbose != null) {
        command.add(sprintf("--verbose=%d", conf.verbose))
    }

    if (conf.config != null) {
        command.add(sprintf("--config=%s", conf.config))

    }

    if (conf.format != null) {
        command.add(sprintf("--format=%s", conf.format))
    }

    command.add("--output-directory")
    if (conf.output_directory != null) {
        command.add(conf.output_directory)
    } else {
        command.add("scribe/valint")
    }

    if (conf.output_file != null) {
        command.add(sprintf("--output-file=%s", conf.output_file))
    }
    
    if (conf.product_key != null) {
        command.add(sprintf("--product-key=%s", conf.product_key))  
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

    if (conf.scribe_client_id != null) {
        command.add(sprintf("--scribe.client-id=%s", conf.scribe_client_id))
    } 

    if (conf.scribe_client_secret != null) {
        command.add(sprintf("--scribe.client-secret=%s", conf.scribe_client_secret))
    }    

    if (conf.scribe_login_url != null ) {
        command.add(sprintf("--scribe.login-url=%s", conf.scribe_login_url))
    }

    if (conf.scribe_audience != null ) {
        command.add(sprintf("--scribe.auth.audience=%s", conf.scribe_audience))
    }  

    if (conf.context_dir != null ) {
        command.add(sprintf("--context-dir=%s", conf.context_dir))
    }

    command.add("--context-type")
    command.add("jenkins")
   
    def commandStr = command.join(' ')
    println "${commandStr}"
    def out_data = sh(script: "${commandStr}",returnStdout: true)
    println out_data
}
#!groovy
import java.nio.file.Path
import java.nio.file.Paths

def call(Map conf) {
    def command = []
    
    command.add("gensbom")
    command.add("verify")

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

    if (conf.inputformat != null) {
        command.add(sprintf("--inputformat=%s", conf.inputformat))
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

    if (conf.filter_regex != null) {
        command.add(sprintf("--filter-regex=%s", conf.filter_regex))
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

    if (conf.context_dir != null ) {
        command.add(sprintf("--context-dir=%s", conf.context_dir))
    }

    command.add("--context-type")
    command.add("jenkins")
   
    def commandStr = command.join(' ')
    def out_data = sh(script: "${commandStr}",returnStdout: true)
    println out_data
}
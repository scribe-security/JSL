#!groovy
import jenkins.pipeline.lib.Constants
import groovy.transform.SourceURI
import java.nio.file.Path
import java.nio.file.Paths

def tar_samples(String dir, String tar="samples.tar"){
    sh "tar cvf ${tar} ${dir}"
    return tar
}


def sendSamples(String recipients, String attachment="samples.tar") {
    def jobName = currentBuild.fullDisplayName
    emailext body: '''${SCRIPT, template="groovy-html.template"}''',
        attachLog: true,
        attachmentsPattern: attachment,
        mimeType: 'text/html',
        subject: "[Scribe] sample ${jobName}",
        to: "${recipients}"
}

def call(String recipients="scribe-samples@scribesecurity.com", String dir="samples", String tar="samples.tar") {
    echo "Sending sample to $recipients"
    // writeFile file:'scribe-groovy-html.template', text:libraryResource("scribe-groovy-html.template")
    def attachment = tar_samples(dir,tar)
    sendSamples(recipients, attachment)
    // sh 'rm -rf scribe-groovy-html.template'
}
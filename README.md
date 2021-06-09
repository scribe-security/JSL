# Scribe sampling jenkins shared library
Sampling JSL is a library that samples the pipeline state for analysis purposes.
Library should be as lean simple as can be and provide a simplified outline of the CI/CD process

## Dependencies

### Jenkins plugin dependencies
Select Manage Jenkins -> Manage Plugins-> Available
Search for required plugin, install plugins and restart jenkins
* html-publisher - allows library to publish result.
* pipeline-utility-steps - using findFiles to search for sample results.
* email extension plugin - used to send all samples to service email.

### Sample script dependencies
* Git
* Bash
* jq
* tar

### Auto install
Library dependencies auto install can be used via `install_enable` param.
User running sample needs sufficient permissions for this to succeeded.

### Manual dependency install
If you are in control of the image the library will be using you may
customize it add the dependencies to image.

## Setup mail ext plugin
* Manage Jenkins -> global configuration 
* SMTP server -> set `smtp.gmail.com`
* SMTP port -> set `587`
* SMTP Username -> mail username to login to smtp with
* SMTP Password -> Select use TLS.
* Set `System Admin e-mail address` to jenkins admin mail

## Setup sampletest
* New Item -> set name `sampletest` -> select Pipeline  -> OK
* Select `Discard old builds` -> Max builds ~ 5
* Pipleline -> Definition -> Select `Pipeline script from SCM`
* SCM -> select `Git` -> Set `https://github.com/scribe-security/kubernetes-jenkins-boilerplate.git`
* Credintials -> Add -> Set access to private SCM -> Set id `GitHubCredId`
* Run job - sampletest sends a mail sent to `alice-tester@scribesecurity.com`

## Using a docker image
TBD

## Sample 
Samples data are stored in JSON format,
A single sample is a group of the following data
* Directory data - collect the current directory files information (hash, names, etc)
* Environment data - current environment when run.
* Git data - current git history

Samples can be bundled and reported to jenkins.

### API
**Sample data to sample dir**
```
def sample(String name, Boolean install_enable = true, Boolean publish_enable = true)
```
* name - name of sample to report
* install_enable - if true will try and look and install script dependencies.
* publish_enable - if true will publish attach sample to job (htmlPublish plugin).

```
sample("first_sample")
sample("first_sample", false, false)
sample("first_sample", true, false)
```

**Send sample dir to mail**
```
def sendSamples(String recipients="scribe-samples@scribesecurity.com", String dir="samples", String tar="samples.tar") {
```
* recipients - send samples to mail
* dir - sample directory to attach to mail
* tar - name of tar to attach

```
sendSamples("somemail@somedomain.com")
```

### Kubernetes JNLP
When using the vanilla JNLP to run the sample you will need to run as root and enable the auto install api.
PodTemplate example:
```
metadata:
  labels:
    some-label: some-label-value
spec:
  containers:
  - name: jnlp
    securityContext:
      allowPrivilegeEscalation: false
      runAsUser: 0
    env:
    - name: CONTAINER_ENV_VAR
      value: jnlp
```

## Jenkinsfile declarative usage
### Library step example
```
library identifier: 'scribe-shared-library@master', retriever: modernSCM(
     [$class       : 'GitSCMSource',
      remote       : 'https://github.com/scribe-security/sampling.git',
      credentialsId: '<GIT_HUB_CRED_ID>'])
...
...

    stage('Busybox') {
       steps {
        sample("Pre-busybox")
        ...
        sample("Post-busybox")
       }
    }
    post {
      always {
        sendSamples("somemail@somedomain.com")
        }
    }
```


### JNLP - Library step example 
#### Pod template
```
metadata:
  labels:
    some-label: some-label-value
spec:
  containers:set -g default-terminal "screen-256color"

  - name: jnlp
    securityContext:
      allowPrivilegeEscalation: false
      runAsUser: 0
    env:
    - name: CONTAINER_ENV_VAR
      value: jnlp
  - name: busybox
    image: busybox
    command:
    - cat
    tty: true
    env:
    - name: CONTAINER_ENV_VAR
      value: busybox
```

#### Jenkinsfile
```
library identifier: 'scribe-shared-library@master', retriever: modernSCM(
     [$class       : 'GitSCMSource',
      remote       : 'https://github.com/scribe-security/sampling.git',
      credentialsId: '<GIT_HUB_CRED_ID>'])

pipeline {
  agent {
    kubernetes {
      yamlFile <path KubernetesPod.yaml>
    }
  }
  stages {
    stage('Busybox') {
       steps {
        sample("Pre-busybox")
        container('busybox') {
            sh 'help'
          }        
        sample("Post-busybox")
       }
    }
  }
  post {
    always {
      sendSamples("somemail@somedomain.com")
    }
  }

```

GIT_HUB_CRED_ID: Credentials needed if jsl repo is currently private.

---
title: JSL
author: mikey strauss - Scribe
date: June 30, 2022
geometry: margin=2cm
---
# Scribe Jenkins shared library
Scribe offers Jenkins shared library for embedding evidence collecting and integrity verification to your pipeline. \
Library wraps scribe provided CLI tools.
* Gensbom - gitHub Action for SBOM Generation (Scribe) 
* Valint - validate supply chain integrity tool


# Installing
(Jenkins documentation)[https://www.jenkins.io/doc/book/pipeline/shared-libraries/]

## Reference in Jenkinsfile
Add the following at the very top of your Jenkinsfile.
```
library identifier: 'JSL@master', retriever: modernSCM(
     [$class       : 'GitSCMSource',
      remote       : 'https://github.com/scribe-security/JSL.git'])
```
## Global Shared Libraries
(See Jenkins documentation)[https://www.jenkins.io/doc/book/pipeline/shared-libraries/]
Global Shared Libraries section.

![Scribe global JSL](./imgs/jsl_install.png?raw=true "Install scribe JSL")


### Using global libraries
(See Jenkins documentation)[(Jenkins documentation)[https://www.jenkins.io/doc/book/pipeline/shared-libraries/]]

Add directive with selected name.
```
@Library('scribe-jsl') _
```

# API
## Gensbom - bom
Function invokes a containerized `gensbom` sub command `bom` based on the CLI.
```
def bom(Map conf)
```

### Usage
```
 bom( target: "busybox:latest", 
      verbose: 2,
      )
```

### Map arguments
```YAML
  type:
    description: 'Target source type options=[docker,docker-archive, oci-archive, dir, registry]'
    default: registry
  target:
    description: 'Target object name format=[<image:tag>, <dir_path>]'
    required: true
  verbose:
    description: 'Increase verbosity (-v = info, -vv = debug)'
    default: 0
  config:
    description: 'Application config file'
  format:
    description: 'Sbom formatter, options=[cyclonedx-json cyclonedx-xml attest-cyclonedx-json statement-cyclonedx-json]'
    default: cyclonedxjson
  output-directory:
    description: 'Report output directory'
    default: ./scribe/gensbom
  output-file:
    description: 'Output result to file'
  name:
    description: 'Custom/project name'
  label:
    description: 'Custom label'
  env:
    description: 'Custom env'
  filter-regex:
    description: 'Filter out files by regex'
    default: .*\.pyc,\.git/.*
  collect-regex:
    description: 'Collect files content by regex'
  force:
    description: 'Force overwrite cache'
    default: false
  attest-config:
    description: 'Attestation config map'
  attest-name:
    description: 'Attestation config name (default "gensbom")'
  attest-default:
    description: 'Attestation default config, options=[sigstore sigstore-github x509]'
    default: sigstore-github
  scribe-enable:
    description: 'Enable scribe client'
    default: false
  scribe-clientid:
    description: 'Scribe client id' 
  scribe-clientsecret:
    description: 'Scribe access token' 
  scribe-url:
    description: 'Scribe url' 
  context-dir:
    description: 'Context dir' 
```

## Gensbom - verify
Function invokes a containerized `gensbom` sub command `verify` based on the CLI.
```
def verify(Map conf)
```

### Usage
```
 verify(target: "busybox:latest", 
      verbose: 2,
      )
```

### Map arguments
```YAML
  type:
    description: 'Target source type options=[docker,docker-archive, oci-archive, dir, registry]'
    default: registry
  target:
    description: 'Target object name format=[<image:tag>, <dir_path>]'
    required: true
  verbose:
    description: 'Increase verbosity (-v = info, -vv = debug)'
    default: 0
  config:
    description: 'Application config file'
  inputformat:
    description: 'Sbom input formatter, options=[attest-cyclonedx-json] (default "attest-cyclonedx-json")'
    default: attest-cyclonedx-json
  output-directory:
    description: 'report output directory'
    default: ./scribe/bomber
  output-file:
    description: 'Output result to file'
  filter-regex:
    description: 'Filter out files by regex'
    default: .*\.pyc,\.git/.*
  attest-config:
    description: 'Attestation config map'
  attest-name:
    description: 'Attestation config name (default "bomber")'
  attest-default:
    description: 'Attestation default config, options=[sigstore sigstore-github x509]'
```

## Valint - report
Function invokes a containerized `valint` sub command `report` based on the CLI.
```
def report(Map conf)
```

### Usage
```YAML
 report(target: "busybox:latest", 
      verbose: 2,
       scribe-enable: true,
      scribe-clientid: ${{ inputs.clientid }}
      scribe-clientsecret: ${{ inputs.clientsecret }}
      )
```

### Map arguments
```YAML
  verbose:
    description: 'Increase verbosity (-v = info, -vv = debug)'
    default: 0
  config:
    description: 'Application config file'
  output-directory:
    description: 'Output directory path'
    default: ./scribe/valint
  output-file:
    description: 'Output file path'
  scribe-enable:
    description: 'Enable scribe client'
    default: false
  scribe-clientid:
    description: 'Scribe client id' 
  scribe-clientsecret:
    description: 'Scribe access token' 
  scribe-url:
    description: 'Scribe url' 
  scribe-loginurl:
    description: 'Scribe auth login url' 
  scribe-audience:
    description: 'Scribe auth audience' 
  context-dir:
    description: 'Context dir' 
  section:
    description: 'Select report sections'
  integrity:
    description: 'Select report integrity'
```

## Publish report
Function allows you to publish the evidence and report to your Jenkins job.
```
def publish(String name="scribe", String directory="scribe") {
```

### API plugin dependencies
Select Manage Jenkins -> Manage Plugins-> Available
Search for required plugin, install plugins and restart jenkins
* html-publisher - allows library to publish result.
* pipeline-utility-steps - allow library to find result.

![Install plugins](./imgs/plugin_install.png?raw=true "Install plugins")


### Usage
```YAML
publish()
```

# Integrations

## Scribe service integration
Scribe provides a set of services to store,verify and manage the supply chain integrity. \
Following are some integration examples.
Scribe integrity flow - upload evidence using `gensbom` and download the integrity report using `valint`. \
You may collect evidence anywhere in your workflows. 

<details>
  <summary>  Scribe integrity report - full pipeline </summary>

Full workflow example of a workflow, upload evidence using gensbom and download report using valint.
Finally attaching reports and evidence to your pipeline run.

```YAML
pipeline {
  agent {
    kubernetes {
      yamlFile 'jenkins/k8s/jsl-scribe-test/KubernetesPod.yaml'
    }
  }
  stages {
    stage('checkout-bom') {
      steps {        
        container('git') {
          sh 'git clone -b v1.0.0-alpha.4 --single-branch https://github.com/mongo-express/mongo-express.git mongo-express-scm'
        }
        
        container('bomber') {
          withCredentials([usernamePassword(credentialsId: 'scribe-staging-auth-id', usernameVariable: 'SCRIBE_CLIENT_ID', passwordVariable: 'SCRIBE_CLIENT_SECRET')]) {
            bom(target: "dir:mongo-express-scm", 
                   verbose: 3,
                   scribe_enable: true,
                   scribe_clientid: "$SCRIBE_CLIENT_ID",
                   scribe_clientsecret: "$SCRIBE_CLIENT_SECRET",
                   )
          }
        }
      }
    }
    
    # Build stages ...omitted

    stage('image-bom') {
      steps {
        container('bomber') {
           withCredentials([usernamePassword(credentialsId: 'scribe-staging-auth-id', usernameVariable: 'SCRIBE_CLIENT_ID', passwordVariable: 'SCRIBE_CLIENT_SECRET')]) {  
            bom(target: "mongo-express:1.0.0-alpha.4", 
                verbose: 3,
                scribe_enable: true,
                scribe_clientid: "$SCRIBE_CLIENT_ID",
                scribe_clientsecret: "$SCRIBE_CLIENT_SECRET",
                )
          }
        }
      }
    }

    stage('download-report') {
      steps {
        container('valint') {
           withCredentials([usernamePassword(credentialsId: 'scribe-staging-auth-id', usernameVariable: 'SCRIBE_CLIENT_ID', passwordVariable: 'SCRIBE_CLIENT_SECRET')]) {  
            report(
                verbose: 3,
                scribe_enable: true,
                scribe_clientid: "$SCRIBE_CLIENT_ID",
                scribe_clientsecret: "$SCRIBE_CLIENT_SECRET",
            )
          }
          publish()            
        }
      }
    }
  }
}
```
Example uses Jenkins over k8s plugin, 
Pod template defined
```YAML
metadata:
  labels:
    some-label: jsl-scribe-test
spec:
  containers:
  - name: jnlp
    env:
    - name: CONTAINER_ENV_VAR
      value: jnlp
  - name: bomber
    image: scribesecuriy.jfrog.io/scribe-docker-public-local/bomber:latest 
    command:
    - cat
    tty: true
  - name: valint
    image: scribesecuriy.jfrog.io/scribe-docker-public-local/valint:latest
    command:
    - cat
    tty: true
  - name: git
    image: alpine/git
    command:
      - cat
    tty: true
```
</details>

## Gensbom integration
<details>
  <summary>  Public registry image </summary>

Create SBOM from remote `busybox:latest` image, skip if found by cache.

```YAML
bom( target: "busybox:latest", 
    verbose: 2,
    )
``` 
</details>

<details>
  <summary>  Private registry image </summary>

Custom private registry, skip cache (using `Force`), output verbose (debug level) log output.
```YAML
bom(target: "scribesecuriy.jfrog.io/scribe-docker-local/stub_remote:latest", 
    verbose: 2,
    force: true
    )
```
</details>

<details>
  <summary>  Custom SBOM metadata </summary>

Custom metadata added to sbom
Data will be included in signed payload when output is an attestation.
```YAML
sh 'export test_env=test_env_value`
bom(target: "busybox:latest", 
    verbose: 3,
    force: true,
    name: name_value
    env: test_env
    label: test_label
    )
```
</details>


<details>
  <summary> Save SBOM as artifact </summary>

Using action `output_path` you can access the generated SBOM and store it as an artifact.
```YAML
bom(target: "busybox:latest", 
    verbose: 2,
    force: true
)
publish()
``` 
</details>

<details>
  <summary> Docker archive image </summary>

Create SBOM from local `docker save ...` output.
```YAML
sh 'docker build . -t stub_local'
sh 'docker save  -o stub_local.tar stub_local'
bom(target: "docker-archive:./stub_local.tar", 
    verbose: 2
)
``` 
</details>

<details>
  <summary> OCI archive image </summary>

Create SBOM from local oci archive.

```YAML
sh 'skopeo copy --override-os linux docker://stub_local oci-archive:stub_oci_local.tar`
bom(target: "oci-archive:./oci_stub_local.tar", 
    verbose: 2
)
``` 
</details>

<details>
  <summary> Directory target </summary>

Create SBOM from local directory. \
Note directory must be mapped to working dir for  actions to access (containerized action).

```YAML
sh '''
mkdir testdir \
echo "test" > testdir/test.txt'''
bom(target: "dir:./testdir", 
    verbose: 2
)
```

</details>


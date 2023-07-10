---
title: JSL
author: mikey strauss - Scribe
date: June 30, 2022
geometry: margin=2cm
---
# Scribe Jenkins shared library
Scribe offers Jenkins a shared library for embedding evidence collecting and integrity verification to your pipeline. \
Library wraps scribe provided CLI tools.
* Valint - gitHub Action for SBOM Generation and validation of supply chain integrity tool


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

Add directive with the selected name.
```
@Library('scribe-jsl') _
```

# API
## Valint - bom
Function invokes a containerized `valint` sub command `bom` based on the CLI.
```
def bom(Map conf)
```

### Usage
```
 bom( target: "busybox:latest"
      )
```

### Map arguments
```YAML
  type:
    description: 'Target source type options=[docker,docker-archive, oci-archive, dir, registry, git]'
    default: registry
  target:
    description: 'Target object name format=[<image:tag>, <dir path>, <git url>]'
    required: true
  verbose:
    description: 'Log verbosity level [-v,--verbose=1] = info, [-vv,--verbose=2] = debug'
    default: 0
  config:
    description: 'Application config file'
  format:
    description: 'Evidence format, options=[cyclonedx-json cyclonedx-xml attest-cyclonedx-json statement-cyclonedx-json]'
    default: cyclonedxjson
  output_directory:
    description: 'Output directory path'
    default: ./scribe/valint
  output_file:
    description: 'Output result to file'
  product_key:
    description: 'Custom/project product-key'
  label:
    description: 'Custom label'
  env:
    description: 'Custom env'
  filter_regex:
    description: 'Filter out files by regex'
    default: .*\.pyc,\.git/.*
  attach_regex:
    description: 'Collect files content by regex'
  force:
    description: 'Force overwrite cache'
    default: false
  attest_config:
    description: 'Attestation config map'
  attest_name:
    description: 'Attestation config name (default "valint")'
  attest_default:
    description: 'Attestation default config, options=[sigstore sigstore-github x509]'
    default: sigstore-github
  scribe_enable:
    description: 'Enable scribe client'
    default: false
  scribe_client_id:
    description: 'Scribe client id' 
  scribe_client_secret:
    description: 'Scribe access token' 
  scribe_url:
    description: 'Scribe url' 
  context_dir:
    description: 'Context dir' 
```

## Valint - verify
The function invokes a containerized `valint` sub command `verify` based on the CLI.
```
def verify(Map conf)
```

### Usage
```
 verify(target: "busybox:latest"
      )
```

### Map arguments
```YAML
  type:
    description: 'Target source type options=[docker,docker-archive, oci-archive, dir, registry, git]'
    default: registry
  target:
    description: 'Target object name format=[<image:tag>, <dir path>, <git url>]'
    required: true
  verbose:
    description: 'Log verbosity level [-v,--verbose=1] = info, [-vv,--verbose=2] = debug'
    default: 0
  config:
    description: 'Application config file'
  inputformat:
    description: 'Sbom input formatter, options=[attest-cyclonedx-json] (default "attest-cyclonedx-json")'
    default: attest-cyclonedx-json
  output_directory:
    description: 'Output directory path'
    default: ./scribe/valint
  output_file:
    description: 'Output result to file'
  filter_regex:
    description: 'Filter out files by regex'
    default: .*\.pyc,\.git/.*
  attest_config:
    description: 'Attestation config map'
  attest_name:
    description: 'Attestation config name (default "valint")'
  attest_default:
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
      scribe_enable: true,
      scribe_client_secret: ${{ inputs.client-secret }}
      )
```

### Map arguments
```YAML
  verbose:
    description: 'Log verbosity level [-v,--verbose=1] = info, [-vv,--verbose=2] = debug'
    default: 0
  config:
    description: 'Application config file'
  output_directory:
    description: 'Output directory path'
    default: ./scribe/valint
  output_file:
    description: 'Output file path'
  scribe_enable:
    description: 'Enable scribe client'
    default: false
  scribe_client_id:
    description: 'Scribe client id' 
  scribe_client_secret:
    description: 'Scribe access token' 
  scribe_url:
    description: 'Scribe url' 
  scribe_login_url:
    description: 'Scribe auth login url' 
  scribe_audience:
    description: 'Scribe auth audience' 
  context_dir:
    description: 'Context dir' 
  section:
    description: 'Select report sections'
  integrity:
    description: 'Select report integrity'
```

## Publish report
The function allows you to publish the evidence and report to your Jenkins job.
```
def publish(String name="scribe", String directory="scribe") {
```

### API plugin dependencies
Select Manage Jenkins -> Manage Plugins-> Available
Search for the required plugin, install plugins and restart Jenkins
* html-publisher - allows the library to publish results.
* pipeline-utility-steps - allow the library to find results.

![Install plugins](./imgs/plugin_install.png?raw=true "Install plugins")


### Usage
```YAML
publish()
```

# Integrations

## Scribe service integration
Scribe provides a set of services to store, verify and manage the supply chain integrity. \
Following are some integration examples.
Scribe integrity flow - upload evidence and download the integrity report using `valint`. \
You may collect evidence anywhere in your workflows. 

<details>
  <summary>  Scribe integrity report - full pipeline (k8s) </summary>

Full workflow example of a workflow, upload evidence and download report using Valint.
Finally, attach reports and evidence to your pipeline run.

```YAML
library identifier: 'JSL@master', retriever: modernSCM(
     [$class       : 'GitSCMSource',
      remote       : 'https://github.com/scribe-security/JSL.git'])

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
        
        container('valint') {
          withCredentials([usernamePassword(credentialsId: 'scribe-staging-auth-id', passwordVariable: 'SCRIBE_CLIENT_SECRET')]) {
            bom(target: "dir:mongo-express-scm",
                scribe_enable: true,
                scribe_client_secret: "$SCRIBE_CLIENT_SECRET",
                )
          }
        }
      }
    }
    
    # Build stages ...omitted

    stage('image-bom') {
      steps {
        container('valint') {
           withCredentials([usernamePassword(credentialsId: 'scribe-staging-auth-id', passwordVariable: 'SCRIBE_CLIENT_SECRET')]) {  
            bom(target: "mongo-express:1.0.0-alpha.4",
                scribe_enable: true,
                scribe_client_secret: "$SCRIBE_CLIENT_SECRET",
                )
          }
        }
      }
    }

    stage('download-report') {
      steps {
        container('valint') {
           withCredentials([usernamePassword(credentialsId: 'scribe-staging-auth-id', passwordVariable: 'SCRIBE_CLIENT_SECRET')]) {  
            report(
                scribe_enable: true,
                scribe_client_secret: "$SCRIBE_CLIENT_SECRET",
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

<details>
  <summary>  Scribe integrity report - full pipeline (docker) </summary>

Full workflow example of a workflow, upload evidence and download report using Valint.
Finally, attach reports and evidence to your pipeline run.

```javascript
library identifier: 'JSL@master', retriever: modernSCM(
     [$class       : 'GitSCMSource',
      remote       : 'https://github.com/scribe-security/JSL.git'])

pipeline {
  agent any
  stages {
    stage('checkout') {
      steps {
          cleanWs()
          sh 'git clone -b v1.0.0-alpha.4 --single-branch https://github.com/mongo-express/mongo-express.git mongo-express-scm'
      }
    }

    stage('sbom') {
        agent {
            docker {
                image 'scribesecuriy.jfrog.io/scribe-docker-public-local/valint:latest'
                reuseNode true
                args "--entrypoint="
            }
        }
        steps {
            withCredentials([usernamePassword(credentialsId: 'scribe-staging-auth-id', passwordVariable: 'SCRIBE_CLIENT_SECRET')]) {
            bom(target: "dir:mongo-express-scm",
                product_key: "testing",
                scribe_enable: true,
                scribe_client_secret: "$SCRIBE_CLIENT_SECRET",
                scribe_url: "https://api.staging.scribesecurity.com",
                scribe_login_url: "https://scribesecurity-staging.us.auth0.com",
                scribe_audience: "api.staging.scribesecurity.com",
                )
           }
        }
    }

    stage('image-bom') {
        agent {
            docker {
                image 'scribesecuriy.jfrog.io/scribe-docker-public-local/valint:latest'
                reuseNode true
                args "--entrypoint="
            }
        }
        steps {
            withCredentials([usernamePassword(credentialsId: 'scribe-staging-auth-id', passwordVariable: 'SCRIBE_CLIENT_SECRET')]) {
            bom(target: "mongo-express:1.0.0-alpha.4",
                product_key: "testing",
                scribe_enable: true,
                scribe_client_secret: "$SCRIBE_CLIENT_SECRET",
                scribe_url: "https://api.staging.scribesecurity.com",
                scribe_login_url: "https://scribesecurity-staging.us.auth0.com",
                scribe_audience: "api.staging.scribesecurity.com",
                )
           }
        }
    }
    stage('download-report') {
        agent {
            docker {
                image 'scribesecuriy.jfrog.io/scribe-docker-public-local/valint:latest'
                reuseNode true
                args "--entrypoint="
            }
        }
        steps {
           withCredentials([usernamePassword(credentialsId: 'scribe-staging-auth-id', passwordVariable: 'SCRIBE_CLIENT_SECRET')]) {  
            report(
                scribe_enable: true,
                scribe_client_secret: "$SCRIBE_CLIENT_SECRET",
                scribe_url: "https://api.staging.scribesecurity.com",
                scribe_login_url: "https://scribesecurity-staging.us.auth0.com",
                scribe_audience: "api.staging.scribesecurity.com",
            )
          }   
      }
    }
  }
}
```
</details>

<details>
  <summary>  Scribe integrity report - full pipeline (binary) </summary>

Full workflow example of a workflow, upload evidence and download report using Valint.
Finally, attach reports and evidence to your pipeline run.

```javascript
```
</details>


## Valint integration
<details>
  <summary>  Public registry image </summary>

Create SBOM for remote `busybox:latest` image, skip if found by the cache.

```YAML
bom( target: "busybox:latest", 
    force: true
    )
``` 
</details>

<details>
  <summary>  Private registry image </summary>

Custom private registry, skip cache (using `Force`), output verbose (debug level) log output.
```YAML
bom(target: "scribesecuriy.jfrog.io/scribe-docker-local/stub_remote:latest",
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
    force: true,
    name: name_value
    env: test_env
    label: test_label
    )
```
</details>


<details>
  <summary>  Save as artifact (SBOM, SLSA) </summary>

Using input variable `output_directory` or `output_file` to export evidence as an artifact.

> Use input variable `format` to select between supported formats.

```YAML
bom(target: "busybox:latest",
    force: true
)
publish()
``` 
</details>

<details>
  <summary> Docker archive image </summary>

Create SBOM for local `docker save ...` output.
```YAML
sh 'docker build . -t stub_local'
sh 'docker save  -o stub_local.tar stub_local'
bom(target: "docker-archive:./stub_local.tar"
)
``` 
</details>

<details>
  <summary> OCI archive image </summary>

Create SBOM for the local OCI archive.

```YAML
sh 'skopeo copy --override-os linux docker://stub_local oci-archive:stub_oci_local.tar`
bom(target: "oci-archive:./oci_stub_local.tar"
)
``` 
</details>

<details>
  <summary> Directory target </summary>

Create SBOM for a local directory. \
Note directory must be mapped to working dir for actions to access (containerized action).

```YAML
sh '''
mkdir testdir \
echo "test" > testdir/test.txt'''
bom(target: "dir:./testdir"
)
```

</details>


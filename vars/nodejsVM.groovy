def call(Map configMap){
    pipeline{
        agent {
            // run on the AGENT-1 node
            node {
                label 'AGENT-1'
            }
        }
        // parameters section, this section is used to define the parameters that can be used in the pipeline
        parameters {
            booleanParam(name: 'Deploy', defaultValue: 'false', description: 'do you want to deploy the application?')
        }
        // define the environment variables canbe accesed globally ,the following are additional to existing environment variables
        // we use ansiColor plugin to print the logs in color
        options {
            ansiColor('xterm')
            timeout(time: 1, unit: 'HOURS')
            disableConcurrentBuilds()
        }
        // we need to get version from the application for this we use pipeline, for this we will use pipeline utilities plugin
        // this can be used across pipeline
        environment {
            packageVersion = ''
            nexusURL = '172.31.92.207:8081'
        }
        //build stages
        
        stages {
            stage('Get Version') {
                steps {
                    script {
                        def packageJSON = readJSON file: 'package.json' 
                        packageVersion = packageJSON.version
                        echo "application version is ${packageVersion}"
                    }
                }
            }
            stage('Install dependencies') {
                steps {
                    sh """
                    echo 'install dependencies'
                    npm install
                    """
                }
            }
            stage('unit tests') {
                steps {
                    sh """
                    echo 'running unit tests'
                    """
                }
            }
            stage('sonar scan') {
                steps {
                        sh """
                            sonar-scanner
                        """
                }
            }
            stage('Build') {
                steps {
                    sh """
                    ls -lart
                    zip -q -r catalogue.zip ./* -x ".git" -x "*.zip"
                    ls -lart

                    """
                }
            }
            stage('publish artifacts') {
                steps {
                        nexusArtifactUploader(
                        nexusVersion: 'nexus3',
                        protocol: 'http',
                        nexusUrl: "${nexusURL}",
                        groupId: 'com.roboshop',
                        version: "${packageVersion}",
                        repository: 'catalogue',
                        credentialsId: 'nexus-auth',
                        artifacts: [
                            [artifactId: 'catalogue',
                            classifier: '',
                            file: 'catalogue.zip',
                            type: 'zip']
                        ])
                }
                
            }
            stage('Deploy') {
                steps {
                    when {
                        expression {
                            params.Deploy == true
                        }
                    }
                    script{
                        build job: 'catalogue-deploy',wait: true,
                        parameters: [
                            string(name: 'version', value: "${packageVersion}"),
                            string(name: 'environment', value: 'dev')]
                    }
                }
            }
        }


        // post section
        post {
            always {
                echo 'This will always run irrespective of status of the pipeline'
                // you need to delete workspace after the build because we are using the same workspace for all the builds
                deleteDir()
            }
            failure {
                echo 'This will run only if the pipeline is failed, We use thsi for alerting the team' 
            }
            success {
                echo 'This will run only if the pipeline is successful'
            }
        }
    }
}
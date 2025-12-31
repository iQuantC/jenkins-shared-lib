def call(Map params = [:]) {
    pipeline {
        agent any
        tools {
		    nodejs 'NodeJS'
	    }
        stages {
            stage('Checkout') {
                steps {
                    checkout scm
                }
            }
            stage('Build') {
                steps {
                    sh 'npm install'
                    echo 'Build completed'
                }
            }
            stage('Test') {
                steps {
                    sh 'npm test'
                    echo 'Tests passed'
                }
            }
            stage('Build Docker Image') {
                steps {
                    script {
                        // Build Docker image
                        def imageName = "my-node-app:${env.BUILD_NUMBER}"
                        sh "docker build -t ${imageName} ."
                        echo 'Docker Image Build Completed'
                    }
                }
            }
        }
        post {
            success {
                echo 'Pipeline succeeded!'
            }
            failure {
                echo 'Pipeline failed!'
            }
        }
    }
}

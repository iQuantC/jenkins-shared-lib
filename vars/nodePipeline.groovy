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
            stage('Deploy') {
                steps {
                    script {
                        // Build Docker image
                        //def imageName = "my-node-app:${env.BUILD_NUMBER}"
                        //sh "docker build -t ${imageName} ."
                        
                        // Simulate deployment: Run the container (in production, push to registry and deploy to Kubernetes/EC2)
                        //sh "docker stop my-node-app || true"
                        //sh "docker rm my-node-app || true"
                        //sh "docker run -d -p 3000:3000 --name my-node-app ${imageName}"
                        
                        //echo 'Deployment completed. App running at http://localhost:3000'
                        echo 'Deployment soon'
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

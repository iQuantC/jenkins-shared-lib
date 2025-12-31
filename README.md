# End-to-End CI/CD Pipeline with Jenkins Shared Libraries for a Simple NodeJS Application
This guide walks you through creating a simple end-to-end CI/CD pipeline using Jenkins with shared libraries for a basic Node.js application. The pipeline will handle building, testing, and deploying the app. 

## Why Jenkins Shared Library is Useful
Implementing CI/CD pipelines using Jenkins Shared Libraries provides significant advantages, especially as your organization grows beyond a single application or team. Some of the advantages are:

1. Code reusability across multiple projects.
2. All pipelines follow same best practices since they call the same shared functions ensuring consistency.
3. When you need to make changes to the pipeline, you update only the shared library in one place ensuring centralized maintenance and updates.
4. Scalability: Without shared libraries, maintaining individual pipelines become a nightmare for a company running say 50 - 1000+ microservices. etc.

   
## Project GOAL
1. The Node.js app is a basic Express.js server.
2. Jenkins Shared Libraries: Reusable pipeline code (Groovy scripts) stored in a separate GitHub repo, allowing modular pipelines across projects.
3. CI: Automatically build and run tests.
4. CD: Automatically build a Docker image, run and deploy it.
5. Triggers: Webhooks from GitHub to Jenkins for automatic builds on push.

## Requirements
1. Jenkins Server
2. Create two (2) Git Repositories: nodejs-app (for the app) & jenkins-shared-lib (for the shared library)

### Set up Jenkins Server
Be sure that you have created a Personal Access Token (PAT) on DockerHub and run the following command on your terminal:
```sh
docker login -u <dockerHub_username>
```
At the password prompt, enter the PAT you created earlier.

Run the command below to create the Jenkins Server Container:

```sh
docker run -d --name jenkins-dind \
-p 8080:8080 -p 50000:50000 \
-v /var/run/docker.sock:/var/run/docker.sock \
-v $(which docker):/usr/bin/docker \
-u root \
-e DOCKER_GID=$(getent group docker | cut -d: -f3) \
jenkins/jenkins:lts
```

Check the running Jenkins container:
```sh
docker ps
```

Log into the Jenkins Container:
```sh
docker exec -it jenkins-dind bash
```

Run the following Bash Commands in Jenkins Container Terminal:
```sh
groupadd -for -g $DOCKER_GID docker
usermod -aG docker jenkins
exit
```

On your browser, open the Jenkins UI using the address:
```sh
127.0.0.1:8080
or 
localhost:8080
```

To get the Inital Admin Password, run the following command: 
```sh
docker logs -f jenkins-dind
```

Paste Initial Admin Password & Install Suggested Plugins Create an Admin User Account with username & password, email, etc.


Update system, Install NodeJS & NPM:
```sh
docker exec -it jenkins-dind bash

# Run the ff commands on the Jenkins container terminal
apt update -y
apt install nodejs npm -y

node --version
npm --version
exit
```

Restart Jenkins Container:
```sh
docker restart jenkins-dind
```

### Install Jenkins Plugins
In the Jenkins UI, click on the gear icon and go to Manage Jenkins. 
Click on Plugins, Available plugins, and search and install the following:

1. NodeJS: You may restart the Jenkins container. Next, navigate to Tools > NodeJS Installations - Name: NodeJS, and install automatically. Apply & Save. You may need to add "tools {nodejs 'NodeJS'}" to the Jenkinsfile.

2. Pipeline (Usually pre-installed)

3. Git Plugin (Usually pre-installed) 



### Configure Jenkins to Use the Shared Library
1. In Jenkins UI, go to gear icon (a.k.a Manage Jenkins) > Under System Configure, go to System > Scroll down to "Global Trusted Pipeline Libraries". 
2. Add a new library: 
    1. Name:                node-shared-lib (or any name)
    2. Default version:     main (your GitHub branch name)
    3. Retrieval method:    Modern SCM > Git
    4. Project repository:  https://github.com/iQuantC/jenkins-shared-lib.git 
    5. (If private repo):   Add credentials etc.
3. Apply and Save.


### Create the Jenkinsfile in the App Repository
This file will reference the shared library from the nodejs-app repository.
Back in the nodejs-app directory, create a Jenkinsfile like this: 

```sh
# this loads the shared library
@Library('node-shared-lib') _ 

# this calls the shared pipeline function from the shared library repository
nodePipeline() 
```

### Set up the Pipeline in Jenkins
1. In Jenkins UI: click on 'New Item' > Pipeline > Name: Nodejs-App-Pipeline > click OK.
2. In the Pipeline section: 
    1. Definition:          Pipeline script from SCM
    2. SCM:                 Git
    3. Repo URL:            https://github.com/iQuantC/nodejs-app.git 
    4. Branches to build:   */main
    5. Script Path:         Jenkinsfile
3. Click apply and save.

### Configure GitHub Webhook for Auto-Triggers
1. In nodejs-app GitHub repo: click Settings > Webhooks > Add webhook. 
2. Payload URL:     Your jenkins server must be reachable on the public internet. Unfortunately, localhost is not so we will use the tool NGROK to expose Jenkins server on the public internet for our case. Here are the steps to follow:

To install NGROK on Linux: 
```sh
curl -sSL https://ngrok-agent.s3.amazonaws.com/ngrok.asc | sudo tee /etc/apt/trusted.gpg.d/ngrok.asc > /dev/null
echo "deb https://ngrok-agent.s3.amazonaws.com buster main" | sudo tee /etc/apt/sources.list.d/ngrok.list
sudo apt update
sudo apt install ngrok
```

To verify installation:
```sh
ngrok help
```

Create the Ngrok Tunnnel for Jenkins port 8080:
```sh
ngrok http 8080
```
From the output:
1. Check the line for Forwarding https://****** -> http://localhost:8080
2. Copy the https://******** address and test it on your browser. It should load your Jenkins dashboard. 


3. Thus, instead of payroll url of http://localhost:8080/github-webhook/, we will use https://92f8f2d8f6f2.ngrok-free.app/github-webhook/ (replace the IP_Address if your Jenkins server is remote)
4. Content type:    application/json
5. Events:          Just the push event
6. Active:          Yes
7. Add Webhook.

Do not forget to change all http://localhost:8080 to https://92f8f2d8f6f2.ngrok-free.app in the Jenkins set up.


### Configure Jenkins & GitHub Integration

#### A. Create a Classic Personal Access Token on GitHub On your GitHub Account,
1. Click on the User Account
2. Click on Settings
3. Developer settings, and select Personal access tokens and Click Tokens (classic)
4. Generate new token, and select Generate new token (classic) Note: jenkins-shared-lib, Expiration: 90 days, and Scopes (select the following): repo, admin:repo_hook (For Webhooks) Generate token & save it somewhere safe


#### B. Add the GitHub Personal Access Token to Jenkins Credentials On the Jenkins UI,
1. Click on Manage Jenkins
2. Click Credentials
3. Under System, click global, and Add Credentials
4. Select Kind: "Username with password", Scope: Global, Password: "Paste the jenkins-shared-lib" here, ID: "jenkins-shared-lib", and Description: "jenkins-shared-lib". Create.


### Run and Test the Pipeline
1. In Jenkins, go to the Nodejs-App-Pipeline job we created earlier. 
2. Click on 'Build Now'. 


# Please LIKE, COMMENT & SUBSCRIBE !!!

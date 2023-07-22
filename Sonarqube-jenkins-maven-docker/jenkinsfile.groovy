pipeline{
    
    agent {
        label "Nodes1"
    }
    tools{
        git "git3"
        maven "Maven3" 
    }
   
    stages{
        
        stage('Checkout code'){
            steps{
             checkout changelog: false, poll: false, scm: scmGit(branches: [[name: '*/main']], extensions: [], userRemoteConfigs: [[url: 'https://github.com/NgomSysOps/new_devops_pro']])
            }
        }
        
        stage('Build project with maven'){
            steps{
                sh 'mvn clean install'
            }
        }
        
        stage('building docker image'){
            steps{
                sh 'docker build -t ngomansible/my_private_repo:v1stable .'
            }
        }
        
        stage('Push image to DockerHub'){
            steps{
                withCredentials([string(credentialsId: 'DockerToken', variable: 'docker_cred')]) {
                   sh 'docker login -u ngomansible -p ${docker_cred}' 
                }
                sh 'docker push ngomansible/my_private_repo:v1stable'
            }
        }
        
        stage('Deploy to kubernetes'){
            steps{
                withKubeConfig(caCertificate: '', clusterName: '', contextName: '', credentialsId: 'k8s-id', namespace: '', restrictKubeConfigAccess: false, serverUrl: '') {
                    sh 'kubectl apply -f myDeployment.yml'
                }
            }
        }
    }
}
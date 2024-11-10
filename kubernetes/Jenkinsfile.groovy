pipeline {
    tools {
        jdk 'jdk17'
    }

    environment {
        JAVA_HOME = 'tool jdk17'
        GIT_REPO = 'https://github.com/maruhxn/todomon-server'
        DOCKER_HUB_REPO = 'maruhan/todomon-app'
        DOCKER_HUB_CREDENTIALS = credentials('maruhan-docker-hub')
    }

    agent any

    stages {
        stage('Git pull') {
            steps {
                git branch: 'dev', url: "${GIT_REPO}"
            }
        }

        stage('Build') {
            steps {
                sh "./gradlew :todomon-core:clean -x :todomon-core:test :todomon-core:build"
            }
        }

        stage('Navigate to Core Directory and Build Docker Image') {
            steps {
                // 'todomon/todomon-core' 디렉토리로 이동하여 Docker 이미지 빌드
                dir('todomon-core') {
                    sh 'echo "Navigating to $(pwd)"'
                    sh "docker build -t $DOCKER_HUB_REPO:$BUILD_NUMBER ."
                }
            }
        }

        stage('Login to Docker Hub') {
            steps {
                script {
                    // Docker Hub 로그인 자동화
                    withCredentials([usernamePassword(credentialsId: 'maruhan-docker-hub', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASSWORD')]) {
                        sh """
                            echo $DOCKER_PASSWORD | docker login -u $DOCKER_USER --password-stdin
                        """
                    }
                }
            }
        }

        stage('Push Docker Image') {
            steps {
                script {
                    // Docker Hub에 이미지 푸시
                    sh "docker push $DOCKER_HUB_REPO:$BUILD_NUMBER"
                }
            }
        }

        stage('Cleaning up') {
            steps {
                sh "docker rmi $DOCKER_HUB_REPO:$BUILD_NUMBER" // docker image 제거
            }
        }

        stage('Kubernetes deploy') {
            steps {
                sh "kubectl apply -f kubernetes/todomon-app.yml"
            }
        }
    }

    post {
        success {
            echo "Build and Push completed successfully!"
        }
        failure {
            echo "Build failed!"
        }
    }
}

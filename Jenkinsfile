pipeline {
  agent any

  environment {
    GIT_CREDENTIALS = 'TSSH'
    DEPLOY_DIR      = '/opt/myapp/backend'
    SERVICE_NAME    = 'myapp-backend.service'
  }

  stages {
    stage('Checkout') {
      steps {
        checkout([
          $class: 'GitSCM',
          branches: [[name: '*/master']],
          doGenerateSubmoduleConfigurations: false,
          extensions: [
            [$class: 'RelativeTargetDirectory', relativeTargetDir: 'backend']
          ],
          userRemoteConfigs: [[
            url: 'git@github.com:artyomchumachenko/jefferson-api.git',
            credentialsId: env.GIT_CREDENTIALS
          ]]
        ])
      }
    }

    stage('Check Changes') {
      steps {
        script {
          if (currentBuild.changeSets.empty) {
            echo "⚠️ Изменений в репозитории нет — пропускаем."
            currentBuild.result = 'SUCCESS'
            error('No changes')
          } else {
            echo "✅ Есть изменения: отправляем на билд/деплой."
          }
        }
      }
    }

    stage('Build & Deploy') {
      steps {
        dir('backend') {
          sh 'git reset --hard origin/master'
          sh 'mvn clean package -DskipTests'
        }
        sh '''
          rm -rf ${DEPLOY_DIR}/*
          rsync -av --delete backend/ ${DEPLOY_DIR}/
          systemctl restart ${SERVICE_NAME}
        '''
      }
    }
  }
}

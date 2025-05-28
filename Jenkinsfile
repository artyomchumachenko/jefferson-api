pipeline {
  agent any

  environment {
    GIT_CREDENTIALS = 'TSSH'
    DEPLOY_DIR      = '/opt/myapp/backend'
    SERVICE_NAME    = 'myapp-backend.service'
  }

  stages {
    stage('Prepare') {
      steps {
        deleteDir()
      }
    }

    stage('Checkout & Fetch') {
      steps {
        sshagent([env.GIT_CREDENTIALS]) {
          // клонируем (если пусто) или обновляем:
          sh '''
            if [ ! -d jefferson-api ]; then
              git clone git@github.com:artyomchumachenko/jefferson-api.git
            fi
            git fetch --all
            git reset --hard origin/master
          '''
        }
      }
    }

    stage('Build') {
      steps {
        dir('jefferson-api') {
          sh 'mvn clean package -DskipTests'
        }
      }
    }

    stage('Deploy') {
      steps {
        sshagent([env.GIT_CREDENTIALS]) {
          // копируем JAR и перезапускаем сервис
          sh """
            cp jefferson-api/target/*.jar ${DEPLOY_DIR}
            systemctl restart ${SERVICE_NAME}
          """
        }
      }
    }
  }

  post {
    success { echo '✅ Успешно собрали и задеплоили jefferson-api' }
    failure { echo '❌ Ошибка, см. логи' }
  }
}

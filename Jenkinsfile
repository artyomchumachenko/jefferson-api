pipeline {
  agent any

  environment {
    GIT_CREDENTIALS = 'TSSH'
    DEPLOY_DIR      = '/opt/myapp/backend'
    SERVICE_NAME    = 'myapp-backend.service'
  }

  stages {
    stage('Checkout & Fetch') {
      steps {
        sshagent([env.GIT_CREDENTIALS]) {
          // Если папки нет — клонируем, иначе просто fetch
          sh '''
            if [ ! -d backend/.git ]; then
              git clone git@github.com:artyomchumachenko/jefferson-api.git backend
            fi
            cd backend
            git fetch origin master
          '''
        }
      }
    }

    stage('Check Changes') {
      steps {
        script {
          dir('backend') {
            // локальный и удалённый хеши
            def local  = sh(script: 'git rev-parse HEAD',          returnStdout: true).trim()
            def remote = sh(script: 'git rev-parse origin/master', returnStdout: true).trim()

            if (local == remote) {
              echo "⚠️ HEAD (${local}) совпадает с origin/master — обновлений нет, pipeline завершён."
              currentBuild.result = 'SUCCESS'
              // true = остановить и не выполнять последующие шаги
              error('No changes')
            } else {
              echo "✅ Новые коммиты: локалка ${local} vs origin ${remote} — продолжаем."
              sh 'git reset --hard origin/master'
            }
          }
        }
      }
    }

    stage('Build & Deploy') {
      steps {
        dir('backend') {
          sh 'mvn clean package -DskipTests'
        }
        // Деплойим всё содержимое backend в продовую папку
        sh '''
          rm -rf ${DEPLOY_DIR}/*
          rsync -av --delete backend/ ${DEPLOY_DIR}/
          systemctl restart ${SERVICE_NAME}
        '''
      }
    }
  }

  post {
    success { echo '🎉 Завершено.' }
    failure {
      script {
        if (currentBuild.result == 'SUCCESS') {
          echo 'ℹ️ Без обновлений, билд/деплой не выполнялись.'
        } else {
          echo '❌ Ошибка, смотрите логи.'
        }
      }
    }
  }
}

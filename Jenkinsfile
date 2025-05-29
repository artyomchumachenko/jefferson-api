pipeline {
  agent any

  environment {
    GIT_CREDENTIALS = 'TSSH'
    DEPLOY_DIR      = '/opt/myapp/backend'
    SERVICE_NAME    = 'myapp-backend.service'
  }

  stages {
    stage('Prepare & Checkout') {
      steps {
        deleteDir()
        sshagent([env.GIT_CREDENTIALS]) {
          sh '''
            # Клоним или апдейтим workspace/backend
            if [ ! -d backend ]; then
              git clone git@github.com:artyomchumachenko/jefferson-api.git backend
            fi
            cd backend
            git fetch origin master
          '''
        }
      }
    }

    stage('Check → Build & Deploy if needed') {
      steps {
        script {
          dir('backend') {
            // получить локальный и удалённый хеш
            def local  = sh(script: 'git rev-parse HEAD',            returnStdout: true).trim()
            def remote = sh(script: 'git rev-parse origin/master',   returnStdout: true).trim()

            if (local == remote) {
              echo "⚠️ Нет новых коммитов (локальный и origin/master = ${local}). Заканчиваем pipeline."
              // прерываем остальные шаги, но считаем билд успешным
              currentBuild.result = 'SUCCESS'
              return
            }

            echo "✅ Обнаружены новые коммиты (локальный ${local} vs origin ${remote}). Начинаем сборку → деплой."

            // переключаемся на свежие изменения
            sh 'git reset --hard origin/master'

            // сборка
            sh 'mvn clean package -DskipTests'

            // копируем всё из backend в продовую папку
            sh """
              rm -rf ${env.DEPLOY_DIR}/*
              rsync -av --delete . ${env.DEPLOY_DIR}/
            """

            // перезапуск сервиса
            sh "systemctl restart ${env.SERVICE_NAME}"
          }
        }
      }
    }
  }

  post {
    success { echo '🎉 Pipeline отработал успешно.' }
    failure { echo '❌ Pipeline упал, смотрите логи.' }
  }
}

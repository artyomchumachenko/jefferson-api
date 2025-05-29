pipeline {
  agent any

  environment {
    GIT_CREDENTIALS = 'TSSH'
    DEPLOY_DIR      = '/opt/myapp/backend'
    SERVICE_NAME    = 'myapp-backend.service'
    SKIP_BUILD      = 'false'
  }

  stages {
    stage('Prepare') {
      steps {
        deleteDir()
      }
    }

    stage('Checkout') {
      steps {
        sshagent([env.GIT_CREDENTIALS]) {
          sh '''
            if [ ! -d backend ]; then
              git clone git@github.com:artyomchumachenko/jefferson-api.git backend
            fi
            cd backend
            git fetch --all
          '''
        }
      }
    }

    stage('Check Changes') {
      steps {
        script {
          dir('backend') {
            // Получаем хэши локального и удалённого HEAD
            def local  = sh(script: 'git rev-parse HEAD',       returnStdout: true).trim()
            def remote = sh(script: 'git rev-parse origin/master', returnStdout: true).trim()
            if (local == remote) {
              echo "⚠️ Нет новых коммитов (локальный и origin/master = ${local}). Пропускаем Build & Deploy."
              env.SKIP_BUILD = 'true'
            } else {
              echo "✅ Обнаружены новые коммиты (локальный ${local} vs origin ${remote}). Продолжаем."
            }
          }
        }
      }
    }

    stage('Build') {
      when {
        expression { env.SKIP_BUILD == 'false' }
      }
      steps {
        dir('backend') {
          sh 'git reset --hard origin/master'
          sh 'mvn clean package -DskipTests'
        }
      }
    }

    stage('Deploy') {
      when {
        expression { env.SKIP_BUILD == 'false' }
      }
      steps {
        // Синхронизируем workspace/backend → продовую папку
        sh '''
          rm -rf ${DEPLOY_DIR}/*
          rsync -av --delete backend/ ${DEPLOY_DIR}/
        '''
        // Перезапускаем сервис
        sh 'systemctl restart ${SERVICE_NAME}'
      }
    }
  }

  post {
    success {
      script {
        if (env.SKIP_BUILD == 'true') {
          echo 'ℹ️ Pipeline завершился: обновлений не было.'
        } else {
          echo '🎉 Pipeline успешно собрал и задеплоил jefferson-api.'
        }
      }
    }
    failure {
      echo '❌ Ошибка в pipeline, смотрите логи.'
    }
  }
}

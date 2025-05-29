def shouldBuild = false

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
          sh '''
            if [ ! -d backend ]; then
              git clone git@github.com:artyomchumachenko/jefferson-api.git backend
            fi
            cd backend
            git fetch origin master
          '''
        }
      }
    }

    stage('Check for Changes') {
      steps {
        dir('backend') {
          script {
            def remote = sh(script: 'git rev-parse origin/master', returnStdout: true).trim()
            def local  = sh(script: 'git rev-parse HEAD',            returnStdout: true).trim()
            if (remote != local) {
              echo "🔄 Найдены новые коммиты: ${local} → ${remote}"
              sh 'git reset --hard origin/master'
              shouldBuild = true
            } else {
              echo "✅ Нет новых коммитов (текущий: ${local}), пропускаем билд/деплой"
            }
          }
        }
      }
    }

    stage('Build') {
      when {
        expression { shouldBuild }
      }
      steps {
        dir('backend') {
          sh 'mvn clean package -DskipTests'
        }
      }
    }

    stage('Deploy') {
      when {
        expression { shouldBuild }
      }
      steps {
        sh '''
          rm -rf ${DEPLOY_DIR}/*
          rsync -av --delete backend/ ${DEPLOY_DIR}/
        '''
        sh "systemctl restart ${SERVICE_NAME}"
      }
    }
  }

  post {
    success {
      script {
        if (shouldBuild) {
          echo '✅ Успешно собрали и задеплоили jefferson-api'
        } else {
          echo 'ℹ️ Сборка и деплой не требуются (нет новых коммитов)'
        }
      }
    }
    failure {
      echo '❌ Ошибка, см. логи'
    }
  }
}
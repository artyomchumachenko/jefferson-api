pipeline {
    agent any

    environment {
        // ID ваших Git-credentials в Jenkins
        GIT_CREDENTIALS = 'TSSH'
        // Путь на проде, куда копируем собранный артефакт
        DEPLOY_DIR      = '/opt/myapp/backend/'
        // Имя systemd-сервиса для перезапуска
        SERVICE_NAME    = 'jefferson-api'
    }

    stages {
        stage('Prepare') {
            steps {
                // чистим workspace перед фетчем
                deleteDir()
            }
        }

        stage('Checkout') {
            steps {
                // клонируем и обновляем origin
                git url: 'git@github.com:artyomchumachenko/jefferson-api.git',
                    credentialsId: "${env.GIT_CREDENTIALS}",
                    branch: 'master'
                sh 'git fetch --all'
            }
        }

        stage('Build') {
            steps {
                // если у вас есть wrapper
                sh 'mvn clean package -DskipTests'
                // или просто: sh 'mvn clean package -DskipTests'
            }
        }

        stage('Deploy') {
            steps {
                // Копируем jar в директорию приложения (нужны права jenkins на запись или sudo без пароля)
                sh "sudo cp target/*.jar ${env.DEPLOY_DIR}"
                // Перезапускаем сервис
                sh "sudo systemctl restart ${env.SERVICE_NAME}"
            }
        }
    }

    post {
        success {
            echo '🎉 jefferson-api успешно собран и задеплоен'
        }
        failure {
            echo '❌ Ошибка в pipeline, смотрите логи'
        }
    }
}

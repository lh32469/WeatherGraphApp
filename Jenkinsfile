pipeline {

  options {
    // Discard everything except the last 10 builds
    buildDiscarder(logRotator(numToKeepStr: '10'))
    // Don't build the same branch concurrently
    disableConcurrentBuilds()
  }

  agent any

  stages {

    stage('Build Maven') {
      agent {
        docker {
          reuseNode true
          image 'maven:latest'
          args '-u root -v /var/lib/jenkins/.m2:/root/.m2'
        }
      }
      steps {
        sh 'mvn clean package'
      }
    }

    stage('Build New Docker') {
      environment {
        registry = "weather/master"
        registryCredential = 'dockerhub'
      }
      steps {
        sh 'ls -l target'
        script {
          image = docker.build registry + ":$BUILD_NUMBER"
        }
        // Cleanup previous images older than 12 hours
        sh 'docker image prune -af --filter "label=app.name=weather" --filter "until=12h"'
      }
    }

    stage('Stop Existing Docker') {
      steps {
        sh 'docker stop weather-master || true && docker rm weather-master || true'
      }
    }

    stage('Start New Docker') {
      steps {
        sh 'docker run -d -p 4802:8085 ' +
            '--restart=always ' +
            '--dns=172.17.0.1 ' +
            '--name weather-master ' +
            '-e TZ=America/Los_Angeles ' +
            'weather/master:$BUILD_NUMBER'
      }
    }

    stage('Register Consul Service') {
      steps {
        script {
          consul = "http://127.0.0.1:8500/v1/agent/service/register"
          ip = sh(
              returnStdout: true,
              script: "docker inspect weather-master | jq '.[].NetworkSettings.Networks.bridge.IPAddress'"
          )
          def service = readJSON text: '{ "Port": 4802 }'
          service["Address"] = ip.toString().trim() replaceAll("\"", "");
          service["Name"] = "weather-master".toString()
          writeJSON file: 'service.json', json: service, pretty: 3
          sh(script: "cat service.json")
          sh(script: "curl -X PUT -d @service.json " + consul)
        }
      }
    }

  }

}

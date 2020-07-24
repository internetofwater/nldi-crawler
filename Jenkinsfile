//pipeline {
//  agent {
//    node {
//      label 'project:any'
//    }
//  }
//  parameters {
//    choice(choices: ['snapshots', 'releases'], description: 'type of build', name: 'BUILD_TYPE')
//    string(defaultValue: 'nldi', description: 'docker image path in artifactory', name: 'ARTIFACTORY_PATH', trim: false)
//    string(defaultValue: 'wma/docker/nldi', description: 'docker image path in gitlab', name: 'GITLAB_PATH', trim: false)
//    string(defaultValue: 'nldi-crawler', description: 'Name of the docker image', name: 'DOCKER_IMAGE_NAME', trim: false)
//  }
//  stages {
//    stage('prep virtualenv') {
//      steps {
//        sh'''/usr/bin/virtualenv $WORKSPACE/env
//        $WORKSPACE/env/bin/pip install bumpversion
//        '''
//      }
//    }
//    stage('Build and test Docker image') {
//      steps {
//        withCredentials([
//          string(credentialsId: 'ARTIFACTORY_HOST', variable: 'artifactoryHost'),
//          string(credentialsId: 'GITLAB_HOST', variable: 'gitlabHost'),
//          string(credentialsId: 'ECR_HOST', variable: 'ecrHost')
//          ]) {
//          sh '''
//          if [ $BUILD_TYPE == "releases" ]
//          then
//            bumpversion_resp=$($WORKSPACE/env/bin/bumpversion --allow-dirty --list release)
//            result=$(echo $bumpversion_resp | grep -oh 'new_version=.*')
//          else
//            bumpversion_resp=$($WORKSPACE/env/bin/bumpversion --allow-dirty --no-tag --no-commit --list minor)
//            result=$(echo $bumpversion_resp | grep -oh '^[^ ]*')
//            /usr/bin/git reset --hard HEAD
//          fi
//          timestamp=$(date +%s)
//          bumpversion=$(/usr/bin/cut -d'=' -f2 <<<$result)
//          version="${bumpversion}-${timestamp}"
//          echo "version=${version}" > version.properties
//          echo "${version}" > version.txt
//          '''
//          script {
//            contents = readFile 'version.txt'
//            def version = contents.trim()
//            def artifactoryPort
//            if ("${BUILD_TYPE}" == "releases") {
//              artifactoryPort = '8446'
//            } else {
//              artifactoryPort = '8445'
//            }
//            def dockerImage = docker.build(DOCKER_IMAGE_NAME)
//            sh "docker tag ${DOCKER_IMAGE_NAME} ${artifactoryHost}:${artifactoryPort}/${ARTIFACTORY_PATH}/${DOCKER_IMAGE_NAME}:${version}"
//            sh "docker tag ${DOCKER_IMAGE_NAME} ${gitlabHost}:5001/${GITLAB_PATH}/${DOCKER_IMAGE_NAME}:${version}"
//            sh "docker tag ${DOCKER_IMAGE_NAME} ${ecrHost}/${DOCKER_IMAGE_NAME}:${version}"
//            sh "docker tag ${DOCKER_IMAGE_NAME} ${ecrHost}/${DOCKER_IMAGE_NAME}:latest"
//            sh "docker tag ${DOCKER_IMAGE_NAME} usgswma/${DOCKER_IMAGE_NAME}:${version}"
//          }
//        }
//      }
//    }
//    stage('Publish Docker images') {
//      steps {
//        withCredentials([
//          string(credentialsId: 'ARTIFACTORY_HOST', variable: 'artifactoryHost'),
//          string(credentialsId: 'GITLAB_HOST', variable: 'gitlabHost'),
//          string(credentialsId: 'ECR_HOST', variable: 'ecrHost')
//          ]) {
//          script {
//            contents = readFile 'version.txt'
//            def version = contents.trim()
//            if ("${BUILD_TYPE}" == "releases") {
//              artifactoryPort = '8446'
//            } else {
//              artifactoryPort = '8445'
//            }
//            // Push to Artifactory
//            withDockerRegistry([credentialsId: 'ARTIFACTORY_UPLOADER_CREDENTIALS', url: "https://${artifactoryHost}:${artifactoryPort}"]) {
//              sh "docker push ${artifactoryHost}:${artifactoryPort}/${ARTIFACTORY_PATH}/${DOCKER_IMAGE_NAME}:${version}"
//            }
//            // Push to Gitlab
//            withDockerRegistry([credentialsId: 'jenkins_ci_access_token', url: 'https://${gitlabHost}:5001']) {
//              sh "docker push ${gitlabHost}:5001/${GITLAB_PATH}/${DOCKER_IMAGE_NAME}:${version}"
//            }
//            // Push to ECR
//            sh "#!/bin/sh -e\n" + "eval \$(aws --region us-west-2 ecr get-login --no-include-email)"
//            sh "docker push ${ecrHost}/${DOCKER_IMAGE_NAME}:${version}"
//            sh "docker push ${ecrHost}/${DOCKER_IMAGE_NAME}:latest"
//          }
//        }
//      }
//    }
//    stage('Push bumped version to git') {
//      steps {
//        withCredentials([string(credentialsId: 'GITHUB_ACCESS_TOKEN', variable: 'jenkinsToken')]) {
//          sh '''
//            if [ $BUILD_TYPE == "releases" ]
//            then
//              $WORKSPACE/env/bin/bumpversion --allow-dirty --no-tag minor
//              /usr/bin/git push
//              /usr/bin/git push --tags
//            fi
//          '''
//        }
//      }
//    }
//  }
//}

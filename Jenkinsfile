pipeline{
	agent {
		node {
			label 'maven'
		}
	}
	environment {
        DOCKER_HUB_REGISTRY = 'registry.cn-hangzhou.aliyuncs.com'
        DOCKER_HUB_NAMESPACE = 'temporary-images'
        DOCKER_HUB_CREDENTIAL_ID = 'docker-hub-credential-id'
        GIT_URL = 'gitee.com/fighting-one-piece/demo-repository.git'
        GIT_CREDENTIAL_ID = 'gitee-credential-id'
        GIT_EMAIL = 'devops@gitee.com'
        KUBE_CREDENTIAL_ID = 'kube-credential-id'
        APP_NAME = 'demo-project'
        BRANCH_NAME = 'master'
    }
	parameters {
        string(name: 'TAG_NAME', defaultValue: '', description: '标签(vx.x.x用于发布正式版)')
    }
	stages{
		stage("prepare demo project") {
			steps {
				script {
					println "prepare demo project"
				}
				sh '''
                cd demo-project
                rm -rf target
                '''
			}
		}

		stage("checkout scm") {
			steps {
				git branch: 'main', credentialsId: "$GIT_CREDENTIAL_ID", url: "https://$GIT_URL"
			}
		}

		stage("unit test") {
		    steps {
		    	container('maven') {
			        echo 'unit test'
			        sh '''
			        cd demo-project
			        mvn clean -X -U test
			        '''
			    }
		    }
		}

		stage("build & push") {
		    steps {
		        container('maven') {
			        echo 'build'
			        sh '''
			        mvn clean -X -U package -Dmaven.test.skip=true
			        docker build -f Dockerfile -t $DOCKER_HUB_REGISTRY/$DOCKER_HUB_NAMESPACE/$APP_NAME:SNAPSHOT-$BRANCH_NAME-$BUILD_NUMBER .
			        '''
			        withCredentials([usernamePassword(credentialsId: "$DOCKER_HUB_CREDENTIAL_ID", passwordVariable: 'DOCKER_PASSWORD', usernameVariable: 'DOCKER_USERNAME')]) {
	                    sh 'echo "$DOCKER_PASSWORD" | docker login $DOCKER_HUB_REGISTRY -u "$DOCKER_USERNAME" --password-stdin'
	                    sh 'docker push $DOCKER_HUB_REGISTRY/$DOCKER_HUB_NAMESPACE/$APP_NAME:SNAPSHOT-$BRANCH_NAME-$BUILD_NUMBER'
	                }
	            }
		    }
		}

		stage('push latest') {
            when {
                branch 'master'
            }
            steps {
                container('maven') {
                    sh '(docker rmi $DOCKER_HUB_REGISTRY/$DOCKER_HUB_NAMESPACE/$APP_NAME:latest || echo "image not exist")'
                    sh 'docker tag $DOCKER_HUB_REGISTRY/$DOCKER_HUB_NAMESPACE/$APP_NAME:SNAPSHOT-$BRANCH_NAME-$BUILD_NUMBER $DOCKER_HUB_REGISTRY/$DOCKER_HUB_NAMESPACE/$APP_NAME:latest'
                    sh 'docker push $DOCKER_HUB_REGISTRY/$DOCKER_HUB_NAMESPACE/$APP_NAME:latest && docker rmi $DOCKER_HUB_REGISTRY/$DOCKER_HUB_NAMESPACE/$APP_NAME:latest'
                }
            }
        }

		stage("deploy to test") {
		    when {
                // branch 'master'
                not {
                    expression {
                        return params.TAG_NAME ==~ /^v?[0-9]+\.[0-9]+\.[0-9]+$/
                    }
                }
            }
		    steps {
		        echo 'deploy to test'
		        kubernetesDeploy(configs: 'config/deploy-test/**', enableConfigSubstitution: true, kubeconfigId: "$KUBE_CREDENTIAL_ID")
		    }
		}

		stage('push with tag') {
            when {
                expression {
                    return params.TAG_NAME =~ /^v?[0-9]+\.[0-9]+\.[0-9]+$/
                }
            }
            steps {
                container('maven') {
                    input(id: 'release-image-with-tag', message: '发布镜像TAG?')
                    withCredentials([usernamePassword(credentialsId: "$GIT_CREDENTIAL_ID", passwordVariable: 'GIT_PASSWORD', usernameVariable: 'GIT_USERNAME')]) {
                        sh 'git config --global user.name "$GIT_USERNAME"'
                        sh 'git config --global user.email "$GIT_EMAIL"'
                        sh 'git tag -a $TAG_NAME -m "$TAG_NAME"'
                        sh 'git push https://$GIT_USERNAME:$GIT_PASSWORD@$GIT_URL --tags'
                    }
                    sh 'docker tag $DOCKER_HUB_REGISTRY/$DOCKER_HUB_NAMESPACE/$APP_NAME:SNAPSHOT-$BRANCH_NAME-$BUILD_NUMBER $DOCKER_HUB_REGISTRY/$DOCKER_HUB_NAMESPACE/$APP_NAME:$TAG_NAME'
                    sh 'docker push $DOCKER_HUB_REGISTRY/$DOCKER_HUB_NAMESPACE/$APP_NAME:$TAG_NAME && docker rmi $DOCKER_HUB_REGISTRY/$DOCKER_HUB_NAMESPACE/$APP_NAME:$TAG_NAME'
                }
            }
        }

		stage("deploy to prod") {
		    when {
                expression {
                    return params.TAG_NAME =~ /^v?[0-9]+\.[0-9]+\.[0-9]+$/
                }
            }
		    steps {
		        echo 'deploy to prod'
                input(id: 'deploy-to-prod', message: '发布到正式环境?')
                kubernetesDeploy(configs: 'config/deploy-prod/**', enableConfigSubstitution: true, kubeconfigId: "$PROD_KUBE_CREDENTIAL_ID")
		    }
		}

	}
}
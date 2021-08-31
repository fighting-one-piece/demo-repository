pipeline{
	agent {
		node {
			label 'maven'
		}
	}
	environment {
        PATH="/bin:/sbin:/usr/bin:/usr/sbin:/usr/local/bin:/usr/local/jdk1.8.0_301/bin"
        DOCKER_HUB_REGISTRY = '192.168.2.128'
        DOCKER_HUB_NAMESPACE = 'temporary'
        DOCKER_HUB_CREDENTIAL_ID = '84e96a6e-a7f9-4f54-a01b-62597d23ef5c'
        GIT_URL = 'gitee.com/fighting-one-piece/demo-repository.git'
        GIT_CREDENTIAL_ID = '77e1a4a7-57ab-4655-b293-6d36cb760cfc'
        GIT_EMAIL = 'devops@gitee.com'
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
			        cd demo-project
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
                sh '(docker rmi $DOCKER_HUB_REGISTRY/$DOCKER_HUB_NAMESPACE/$APP_NAME:latest || echo "image not exist")'
                sh 'docker tag $DOCKER_HUB_REGISTRY/$DOCKER_HUB_NAMESPACE/$APP_NAME:SNAPSHOT-$BRANCH_NAME-$BUILD_NUMBER $DOCKER_HUB_REGISTRY/$DOCKER_HUB_NAMESPACE/$APP_NAME:latest'
                sh 'docker push $DOCKER_HUB_REGISTRY/$DOCKER_HUB_NAMESPACE/$APP_NAME:latest && docker rmi $DOCKER_HUB_REGISTRY/$DOCKER_HUB_NAMESPACE/$APP_NAME:latest'
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
		        sh '''
		        cd demo-project
		        nohup java -jar -Dserver.port=8001 -Dspring.profiles.active=test target/demo-project-1.0-SNAPSHOT.jar &
		        '''
		    }
		}

		stage('push with tag') {
            when {
                expression {
                    return params.TAG_NAME =~ /^v?[0-9]+\.[0-9]+\.[0-9]+$/
                }
            }
            steps {
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

		stage("deploy to prod") {
		    when {
                expression {
                    return params.TAG_NAME =~ /^v?[0-9]+\.[0-9]+\.[0-9]+$/
                }
            }
		    steps {
		        echo 'deploy to prod'
		    }
		}

	}
}
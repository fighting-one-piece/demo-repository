FROM anapsix/alpine-java:8_server-jre_unlimited
WORKDIR /publish
RUN sed -i 's/dl-cdn.alpinelinux.org/mirrors.aliyun.com/g' /etc/apk/repositories \
  && apk add -U tzdata tini \
  && cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime \
  && apk del tzdata
EXPOSE 8001
EXPOSE 8009

ENV YZ_MODE "test"
ENV JAVA_OPTS ""
ENV JAVA_PARAMS ""
ENV DEF_JAVA_OPTS "-Djava.security.egd=file:/dev/./urandom -Duser.timezone=Asia/Shanghai -Dfile.encoding=utf-8"
ENV APP_JAR_PATH=app.jar

ADD target/*.jar ./${APP_JAR_PATH}

ENTRYPOINT ["/sbin/tini","--","sh","-c","java ${JAVA_OPTS} ${DEF_JAVA_OPTS} -jar ${APP_JAR_PATH} ${JAVA_PARAMS} --spring.profiles.active=${YZ_MODE} --server.port=8001"]
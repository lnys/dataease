FROM registry.cn-qingdao.aliyuncs.com/dataease/fabric8-java-alpine-openjdk8-jre:edge-chromium

ARG IMAGE_TAG
ARG PACKAGE_URL

RUN mkdir -p /opt/apps /opt/dataease/data/feature/full /opt/dataease/drivers

ADD mapFiles/* /opt/dataease/data/feature/full/

ADD drivers/* /opt/dataease/drivers/

#RUN wget -O app.tgz $PACKAGE_URL \
#    && tar -zxvf app.tgz -C /opt/apps/ \
#    && rm app.tgz

COPY backend/target/*.jar /opt/apps/

ENV JAVA_APP_JAR=/opt/apps/backend-1.18.4.jar

ENV AB_OFF=true

ENV JAVA_OPTIONS=-Dfile.encoding=utf-8

HEALTHCHECK --interval=15s --timeout=5s --retries=20 --start-period=30s CMD curl -f 127.0.0.1:8081

CMD ["/deployments/run-java.sh"]

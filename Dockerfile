FROM maven:3.8.6-openjdk-11-slim
ENV TZ Asia/Taipei
# copy source code
RUN mkdir /workspace
RUN mkdir /workspace/trade-engine
COPY src /workspace/trade-engine/src
COPY pom.xml /workspace/trade-engine/
COPY Dockerfile /workspace/trade-engine/
COPY start-trade-engine.sh /workspace/trade-engine/

# package
WORKDIR /workspace/trade-engine
RUN mvn -T 4C clean package -DskipTests
RUN find ./target -name 'trade-engine*.jar' -print0 | xargs -0 chmod a+x

# exec
CMD ["sh", "start-trade-engine.sh"]
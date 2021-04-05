FROM openjdk:14-alpine

ENV WORKING_DIR=/root/agent

WORKDIR $WORKING_DIR
RUN mkdir -p $WORKING_DIR

# 'import' all the required files
COPY jason-2.4/libs ./jason/libs
COPY src/main/asl/utilities $WORKING_DIR/utilities
COPY src/main/asl/action $WORKING_DIR/action
COPY src/main/asl/agent.asl $WORKING_DIR

# start the agent
CMD java -cp "jason/libs/*" jade.Boot -container -host main_container "$NAME:jason.infra.jade.JadeAgArch($WORKING_DIR/agent.asl)"
FROM openjdk:14-alpine

ENV WORKING_DIR=/root/agent

WORKDIR $WORKING_DIR
RUN mkdir -p $WORKING_DIR

# 'import' all the required files
COPY jason-2.4/libs ./jason/libs
COPY utilities $WORKING_DIR/utilities
COPY state $WORKING_DIR/state
COPY agent.asl $WORKING_DIR

# start the agent
CMD java -cp "jason/libs/*" jade.Boot -container -host main_container "$NAME:jason.infra.jade.JadeAgArch($WORKING_DIR/agent.asl)"
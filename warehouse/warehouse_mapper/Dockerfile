FROM openjdk:14-alpine

ENV WORKING_DIR=/root/agent

WORKDIR $WORKING_DIR
RUN mkdir -p $WORKING_DIR

COPY jason-2.6/libs ./jason/libs
COPY utilities ./utilities
COPY *.asl ./

# set jade in classpath
ENV CLASSPATH=jason/libs/jade-4.3.jar:jason/libs/jason-2.6.jar

# TODO
# ricordo che devo rendere accessibile dall'esterno del container
# la porta 3000 su cui nodejs rimane in attesa di connessioni
# da parte dei client
# Occhio, non espongo la porta, questa EXPOSE è solo una nota per
# ricordare che devo esporre la porta quando eseguo il container.
EXPOSE 3000

# start the agent
CMD java jade.Boot -container -host main_container "agent:jason.infra.jade.JadeAgArch(agent.asl)"
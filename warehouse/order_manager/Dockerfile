FROM ubuntu:xenial

ENV WORKING_DIR=/root/agent

WORKDIR $WORKING_DIR
RUN mkdir -p $WORKING_DIR

# install all the needed packages
RUN apt-get -y update &&                            \
    apt-get -y install apt-utils &&                 \
    apt-get -y install wget &&                      \
    apt-get -y install unzip &&                     \
    apt-get -y install openjdk-8-jdk &&             \
    apt-get -y install ant &&                       \
    apt-get -y install ca-certificates-java &&      \
    apt-get clean

# fix certificate issues
RUN update-ca-certificates -f

# setup JAVA_HOME
ENV JAVA_HOME /usr/lib/jvm/java-8-openjdk-amd64/
RUN export JAVA_HOME

# download jason interpreter
RUN wget sourceforge.net/projects/jason/files/jason/version2.6/jason-2.6.zip && \
    unzip -q jason-2.6.zip -d jason &&              \
    rm jason-2.6.zip

# set jade in classpath
ENV CLASSPATH=jason/libs/jade-4.3.jar

# TODO
# ricordo che devo rendere accessibile dall'esterno del container
# la porta 3000 su cui nodejs rimane in attesa di connessioni
# da parte dei client
# Occhio, non espongo la porta, questa EXPOSE è solo una nota per
# ricordare che devo esporre la porta quando eseguo il container.
EXPOSE 3000

# start the agent
CMD java jade.Boot -container -host 127.0.0.1 "agent:jason.infra.jade.JadeAgArch(agent.asl)"
 
FROM phusion/baseimage
CMD ["/sbin/my_init"]

RUN \
  echo oracle-java8-installer shared/accepted-oracle-license-v1-1 select true | debconf-set-selections && \
  add-apt-repository -y ppa:webupd8team/java && \
  apt-get update && \
  apt-get install -y oracle-java8-installer && \
  rm -rf /var/lib/apt/lists/* && \
  rm -rf /var/cache/oracle-jdk8-installer

RUN apt-get update && apt-get install -y \
    unzip \
    xvfb \
    libxext6 \
    libxi6 \
    libxtst6 \
    libxrender1 

RUN apt-get clean && rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

COPY webswing-distribution.zip /opt/webswing/webswing.zip

RUN unzip /opt/webswing/webswing.zip -d /opt/webswing && mv /opt/webswing/webswing/* /opt/webswing/ && rm -d /opt/webswing/webswing.zip /opt/webswing/webswing
  
ENV WEBSWING_JAVA_HOME=/usr/lib/jvm/java-8-oracle \
    WEBSWING_JAVA_OPTS="-Xmx128M" \
    WEBSWING_HOME=/opt/webswing \
    DISPLAY=:99

ENV WEBSWING_OPTS="-h 0.0.0.0 -j $WEBSWING_HOME/jetty.properties -u $WEBSWING_HOME/user.properties -c $WEBSWING_HOME/webswing.config"

WORKDIR /opt/webswing

RUN mkdir /etc/service/xvfb && \
    echo "#!/bin/sh\nexec Xvfb :99" > /etc/service/xvfb/run && \
    chmod +x /etc/service/xvfb/run

RUN mkdir /etc/service/webswing && \
    echo "#!/bin/sh\ncd $WEBSWING_HOME\nexec $WEBSWING_JAVA_HOME/bin/java $WEBSWING_JAVA_OPTS -jar $WEBSWING_HOME/webswing-server.war $WEBSWING_OPTS" > /etc/service/webswing/run && \
    chmod +x /etc/service/webswing/run

EXPOSE 8080
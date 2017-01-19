FROM java:alpine
MAINTAINER Philippe Tjon-A-Hen (philippe@tjonahen.nl)

COPY maven/*.jar /app/
COPY maven/dependency/*.jar /app/dependency/

VOLUME /app/log
EXPOSE 40000
EXPOSE 40001

CMD java -Dcom.sun.management.jmxremote.port=4001 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false -cp app/dependency/*:app/iptableslogd.jar nl.tjonahen.iptableslogd.Application --input /app/log/ulogd.syslogemu

FROM java:alpine
MAINTAINER Philippe Tjon-A-Hen (philippe@tjonahen.nl)

COPY maven/*.jar /app/

VOLUME /app/log
EXPOSE 40000
EXPOSE 40001

CMD java -Dcom.sun.management.jmxremote.port=4001 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false -jar /app/iptableslogd.jar --input /app/log/ulogd.syslogemu

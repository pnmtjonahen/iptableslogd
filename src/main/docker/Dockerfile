FROM java:alpine
MAINTAINER Philippe Tjon-A-Hen (philippe@tjonahen.nl)

COPY maven/*.jar /app/
COPY maven/*.properties /app/
COPY maven/dependency/*.jar /app/dependency/

VOLUME /app/log
EXPOSE 40000
EXPOSE 40001

CMD java -Djava.util.logging.SimpleFormatter.format='%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS %4$s %2$s %5$s%6$s%n' -Dcom.sun.management.jmxremote.port=4001 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false -cp app/dependency/*:app/iptableslogd.jar nl.tjonahen.iptableslogd.Application --port 4000 --input /app/log/ulogd.syslogemu

FROM java:alpine
MAINTAINER Philippe Tjon-A-Hen (philippe@tjonahen.nl)

COPY maven/*.jar /app/

VOLUME /app/log
EXPOSE 40000

CMD java -jar /app/iptableslogd.jar --input /app/log/ulogd.syslogemu

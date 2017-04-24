# iptableslogd
iptables log deamon parses and shows the content of the iptables log. 

docker run -d -p 4000:4000 -p 4001:4001 --restart unless-stopped -v /var/log:/app/log:ro tjonahen/iptableslogd


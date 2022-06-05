source ~/.bashrc
activate py27

UTIL="import util;print util"
ROOT=$(cd `dirname $0`;pwd)
HOST=$(python -c "$UTIL.get_ip()")
JOB=$(basename $0)
PROJECT="likeit"
OWNER="owner"

JAR="likeit[a-zA-Z0-9]*.jar"
PY="app.py"

ps -ef | grep -e "$JAR" | grep -v grep | awk '{print $2}' | xargs kill -9
ps -ef | grep -e "$PY" | grep -v grep | awk '{print $2}' | xargs kill -9

find ../log/*.log* -mtime +3 | xargs rm -rf

today=$(python -c "$UTIL.today()")
LOG="../log/likeit.log.$today"
{
    java -cp target/$JAR:conf/* com.likeit.search.SearchApp > ../log/java.log.$today 2>&1 &
    python web/app.py runserver -h 0.0.0.0 -p 8888 > ../log/python.log.$today 2>&1 &
} >> $LOG 2>&1

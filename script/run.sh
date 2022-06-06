source ~/.bashrc
conda activate py27

set -x

ROOT=$(cd `dirname $0`;pwd)
cd $ROOT
UTIL="import util;print util"
HOST=$(python -c "$UTIL.get_ip()")
JOB=$(basename $0)
PROJECT="likeit"
OWNER="owner"

JAR="likeit-1.0-SNAPSHOT.jar"
PY="app.py"

ps -ef | grep -e "$JAR" | grep -v grep | awk '{print $2}' | xargs kill -9
ps -ef | grep -e "$PY" | grep -v grep | awk '{print $2}' | xargs kill -9

find ../log/*.log* -mtime +3 | xargs rm -rf

today=$(python -c "$UTIL.today()")
LOG="$ROOT/../log/likeit.log.$today"
{
    java -jar $ROOT/../java/target/$JAR \
        --spring.config.location=$ROOT/../conf/application.yml > $ROOT/../log/java.log.$today 2>&1 &

    conda activate base
    python $ROOT/../web/app.py runserver -h 0.0.0.0 -p 8888 > $ROOT/../log/python.log.$today 2>&1 &
} >> $LOG 2>&1

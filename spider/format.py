#coding=utf8
import sys
from datetime import datetime, timedelta

DT_X = '2017-09-18'
TM_P = None
DT_D = 0
DATA = []

def parse_flag(line):
    global DT_D
    global TM_P
    if line.startswith("我的手机") or \
            line.startswith("我的电脑"):
        fds = line.split()
        dt = datetime.strptime(DT_X + ' ' + fds[-1], '%Y-%m-%d %H:%M:%S')
        if TM_P != None and fds[-1] < TM_P:
            DT_D += 1
        mdt = dt + timedelta(days = DT_D)
        TM_P = fds[-1]
        return datetime.strftime(mdt, '%Y-%m-%d %H:%M:%S')
    return None

data = []
data_ts = None
for line in sys.stdin:
    line = line.strip()
    if line == "":
        continue

    dt = parse_flag(line)
    if dt != None:
        #print line
        #print dt
        if len(data) != 0:
            dx = []
            for d in data:
                if d.startswith('http'):
                    print "\t".join([d, data_ts])
                else:
                    dx.append(d)
            if len(dx) > 0:
                sd = "|".join(dx)
                print "\t".join([sd, data_ts])
            data = []
        data_ts = dt
    else:
        data.append(line)


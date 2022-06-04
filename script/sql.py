#coding=utf8
import sys
import logging

import util

for line in sys.stdin:
    line = line.strip()

    str_begin = 'INSERT INTO `user_urls` VALUES '
    str_vals = line[len(str_begin):-1]
    #print(str_vals)

    R = []
    ps = None
    record = []
    word = ''
    status = 'B'
    for s in str_vals:
        if status == 'B':
            if s == ',':
                pass
            elif s == '(':
                status = 'R'
            else:
                logging.error('error:%s,%s' % (status, s))
                break
        elif status == 'R':
            if s == "'":
                word += s
                status = 'Q'
            elif s == ',':
                record.append(word)
                word = ''
            elif s == '(':
                logging.error('error:%s,%s' % (status, s))
                break
            elif s == ')':
                record.append(word)
                #print(",".join(record))
                R.append(record)
                word = ''
                record = []
                status = 'B'
            else:
                word += s
                status = 'W'
        elif status == 'Q':
            if s == "'":
                word += s
                if ps == '\\':
                    pass
                else:
                    status = 'W'
            else:
                word += s
        elif status == 'W':
            if s == "'":
                word += s
                if ps == '\\':
                    pass
                else:
                    status = 'Q'
            elif s == ',':
                record.append(word)
                word = ''
                status = 'R'
            elif s == ')':
                record.append(word)
                #print(",".join(record))
                R.append(record)
                word = ''
                record = []
                status = 'B'
            elif s == '(':
                logging.error('error:%s,%s' % (status, s))
                break
            else:
                word += s
        else:
            logging.error('error:%s,%s' % (status, s))
            break
        ps = s

    #word must be empty
    #print("===:" + word)

    '''
    SR = []
    for r in R:
        SR.append("(%s)" % ",".join(r))
    print('INSERT INTO `user_urls` VALUES %s;' % ",".join(SR))
    '''

    S = {}
    for r in R:
        uid = r[1]
        url = r[5]

        key = uid + ',' + url
        if key not in S:
            S[key] = []
        md5 = util.md5sum(url[1:-1]) #rm quote
        #S[key].append("(%s)" % ",".join(r + ["'%s'" % md5]))
        r[-1] = "'%s'" % md5
        S[key].append("(%s)" % ",".join(r))

    '''
    for k, v in S.items():
        if len(v) > 1:
            for sv in v:
                print(str(len(v)) + " " + sv)
    '''

    U = set()
    SR = []
    for r in R:
        uid = r[1]
        url = r[5]

        key = uid + ',' + url
        data = S[key]
        if key not in U:
            SR.append(data[-1])
            U.add(key)

    #print("\n".join(SR))
    print('INSERT INTO `user_urls` VALUES %s;' % ",".join(SR))

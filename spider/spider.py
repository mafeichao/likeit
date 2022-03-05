#coding=utf8
import re
import sys
import time
import jieba
import tools
import logging
import hashlib
import requests
import elasticsearch
from bs4 import BeautifulSoup

logging.basicConfig(level=logging.INFO,
                    format='%(asctime)s - %(filename)s[line:%(lineno)d] - %(levelname)s: %(message)s')  # logging.basicConfig函数对日志的输出格式及方式做相关配置

ES = elasticsearch.Elasticsearch(["127.0.0.1:9200","127.0.0.1:9201","127.0.0.1:9202"])

STOP_WORDS = set()
for line in open('stop_words.txt'):
    line = line.strip()
    if line == "":
        continue

    STOP_WORDS.add(line)
logging.info("stop words size:%d" % len(STOP_WORDS))

def get_html(url):
    try:
        header = {'user-agent': 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10.9; rv:32.0) Gecko/20100101 Firefox/32.0',}
        ret = requests.get(url, headers = header)
        return ret.content
    except Exception as e:
        logging.error(e)
        return None

def get_text_from_html(html):
    try:
        soup = BeautifulSoup(html, 'html.parser')
        text = tools.strQ2B(soup.get_text())#.encode('utf8')
        lines = map(lambda x:x.strip(), re.split("[\r\n]", text))
        lines = filter(lambda x:len(x) > 0, lines)
        return "\n".join(lines)
    except Exception as e:
        logging.error(e)
        return None


def seg_text(text):
    try:
        def _cut_process(x):
            #x = x.encode('utf8')
            return x.strip()

        words = jieba.cut_for_search(text)
        words = map(lambda x:_cut_process(x), words)
        words = filter(lambda x:len(x) > 0 and x not in STOP_WORDS, words)
        return list(words)
    except Exception as e:
        logging.error(e)
        return None

def index_words(url, words):
    try:
        createdAt = int(time.time())
        sign = hashlib.md5(url.encode('utf8')).hexdigest()
        data = {"url": url, "words" : words, \
                "source" : "https://www.baidu.com", "query" : [], "query_org" : [], \
                "createdAt" : createdAt}
        ES.index(index = 'likeit', body = data, id = sign)
    except Exception as e:
        logging.error(e)

if __name__ == "__main__":
    for line in sys.stdin:
        line = line.strip()
        if line == "":
            continue

        html = get_html(line)
        if html != None:
            logging.info('url:%s' % line)
            text = get_text_from_html(html)
            if text != None:
                words = seg_text(text)
                #print >> sys.stdout, 'words:%s' % "|".join(words[:100])
                #print('words:%s' % "|".join(words))
                index_words(line, words)
            else:
                logging.error('url:%s, text none' % line)
        else:
            logging.error('url:%s, html none' % line)

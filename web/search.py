#coding=utf8

import jieba
import tools
import logging
import elasticsearch
from elasticsearch_dsl import Q, Search
from flask import Flask, jsonify, request
#from flask_restful import reqparse, abort, Api, Resource

logging.basicConfig(level=logging.INFO,
                    format='%(asctime)s - %(filename)s[line:%(lineno)d] - %(levelname)s: %(message)s')  # logging.basicConfig函数对日志的输出格式及方式做相关配置

ES = elasticsearch.Elasticsearch(["127.0.0.1:9200","127.0.0.1:9201","127.0.0.1:9202"])

STOP_WORDS = tools.load_set('stop_words.txt')
logging.info("stop words size:%d" % len(STOP_WORDS))

app = Flask(__name__)
#api = Api(app)

def should_words(words):
    q = None #Q('terms', words = words)
    for word in words:
        if q == None:
            q = Q('term', words = word)
        else:
            q = q | Q('term', words = word)
    return q

def must_words(words):
    q = None
    for word in words:
        if q == None:
            q = Q('term', words = word)
        else:
            q = q & Q('term', words = word)
    return q

@app.route('/likeit/search', methods=['GET']) 
def search():
    query = request.args.get("query") or None
    result = {"query": query}
    if query != None:
        words = jieba.cut(query)
        words = filter(lambda x:x not in STOP_WORDS, words)
        words = list(words)
        
        q1 = should_words(words)
        q2 = must_words(words)

        q = q1 | q2 
        s = Search(using = ES, index = 'likeit').query(q)
        s.update_from_dict({"from":0, "size": 1000})

        logging.info("query:" + query + ",dsl:" + str(s.to_dict()).replace("'", '"'))
        r = s.execute()
        hits = r.hits if r != None else []
        docs = []
        for hit in hits:
            docs.append({"url":hit.url, "score": str(hit.meta.score), "title":"title", "desc":"desc"})
        result['words'] = words
        result['docs'] = docs
    return jsonify(result)

if __name__ == "__main__":
    app.run(host="0.0.0.0", port=8383, debug=True)

# coding=utf8

from flask import Flask, render_template, request
from flask_paginate import Pagination

import logging
import requests
import json

app = Flask(__name__)

logging.basicConfig(level=logging.INFO,
                    format='%(asctime)s - %(filename)s[line:%(lineno)d] - %(levelname)s: %(message)s')  # logging.basicConfig函数对日志的输出格式及方式做相关配置


@app.route("/")
@app.route("/search", methods=['GET'])
def search():
    query = request.args.get("query", type=str, default=None)
    page = request.args.get("page", type=int, default=1)
    logging.info("method:%s, query:%s, page:%d", request.method, query, page)

    total = 0
    docs = []
    pagination = None
    if query:
        if page <= 1:
            page = 1

        try:
            result = requests.get('http://127.0.0.1:8082/search/query.json?q=%s&page=%d' % (query, page))
            objs = json.loads(result.text)
            total = objs["total"]
            docs = objs["data"]
            per_page = len(docs)
            # logging.info("result:%s", docs)
            pagination = Pagination(bs_version=3, page=page, per_page_parameter=per_page, total=total)
        except Exception as e:
            logging.error("search failed:%s,%s", query, e)

    return render_template("search.html", query=query, total=total, docs=docs, pages=pagination)


if __name__ == "__main__":
    app.run("0.0.0.0", 8898)

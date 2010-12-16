#! /usr/bin/env python
#encoding=utf-8
'''
Created on 2010-10-25

@author: jiugao
'''
from client.TimeTunnel2 import use, passport, tunnel, post, release
import logging.config
import os


basepath = os.path.dirname(__file__)
logging.config.fileConfig(basepath + "/../conf/log.conf")

if __name__ == '__main__':
    use(passport("jiugao", "1111"))
    t = tunnel("t1")
    m = "hello"
    times = range(100)
    for i in times:
        post(t, m)
            
    release();
    

#! /usr/bin/env python
#encoding=utf-8
'''
Created on 2010-10-29

@author: jiugao
'''
import logging
from logging import config
import os, sys
from ptail.Config import Config
from ptail.Tail import Tail
import signal
import traceback
from time import sleep
from client.TimeTunnel2 import use, passport, set_router, release

basepath = os.path.dirname(__file__)
config.fileConfig(basepath + "../conf/log.conf")


logger = logging.getLogger("tailfile")

def daemonize (stdin='/dev/null', stdout='/dev/null', stderr='/dev/null'):
    try:
        pid = os.fork()
        if pid > 0:
            sys.exit(0) # Exit first parent.
    except OSError, e:
        sys.stderr.write("fork #1 failed: (%d) %s\n" % (e.errno, e.strerror))
        sys.exit(1)
    os.chdir("/")
    os.umask(0)
    os.setsid()
    try:
        pid = os.fork()
        if pid > 0:
            sys.exit(0)
    except OSError, e:
        sys.stderr.write("fork #2 failed: (%d) %s\n" % (e.errno, e.strerror))
        sys.exit(1)
    for f in sys.stdout, sys.stderr: f.flush()
    si = file(stdin, 'r')
    so = file(stdout, 'a+')
    se = file(stderr, 'a+', 0)
    os.dup2(si.fileno(), sys.stdin.fileno())
    os.dup2(so.fileno(), sys.stdout.fileno())
    os.dup2(se.fileno(), sys.stderr.fileno())

if __name__ == '__main__':
    index = 0
    tail = []
    stop_flag = False
    
    daemonize()
    while True:
    	logger.error("stopped")
    
    
    

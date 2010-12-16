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
config.fileConfig(basepath + "/../conf/log.conf")
conf = Config(basepath + '/../conf/tailfile.conf')


logger = logging.getLogger("tailfile")

class SingleInstance:
    def __init__(self):
        self.lockfile = os.path.normpath(basepath + '/' + os.path.basename(__file__) + '.lock')
        if sys.platform == 'win32':
            try:
                # file already exists, we try to remove (in case previous execution was interrupted)
                if(os.path.exists(self.lockfile)):
                    os.unlink(self.lockfile)
                self.fd = os.open(self.lockfile, os.O_CREAT | os.O_EXCL | os.O_RDWR)
            except OSError, e:
                if e.errno == 13:
                    print "Another instance is already running, quitting."
                    sys.exit(-1)
                print e.errno
                raise
        else: # non Windows
            import fcntl
            self.fp = open(self.lockfile, 'w')
            try:
                fcntl.lockf(self.fp, fcntl.LOCK_EX | fcntl.LOCK_NB)
            except IOError: 
                print "Another instance is already running, quitting."
                logger.error("Another instance is already running, quitting.")
                sys.exit(-1)

    def __del__(self):
        import sys
        if sys.platform == 'win32':
            if hasattr(self, 'fd'):
                os.close(self.fd)
                os.unlink(self.lockfile)

def daemonize (stdin='/dev/null', stdout='/dev/null', stderr='/dev/null'):
    try:
        pid = os.fork()
        if pid > 0:
            sys.exit(0) # Exit first parent.
    except OSError, e:
        sys.stderr.write("fork #1 failed: (%d) %s\n" % (e.errno, e.strerror))
        sys.exit(1)
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
    topics = conf.get_topic_name().split(",")
    index = 0
    tail = []
    stop_flag = False
    
    if sys.platform != 'win32':
        daemonize()

    single=SingleInstance()

    if conf.get_test_path() == "null":
        use(passport(conf.get_passport().split(":")[0], conf.get_passport().split(":")[1]))
        set_router(conf.get_routers())
        
    for topic in topics:
        t = Tail(index, conf)
        tail.append(t)
        index += 1
        logger.error("Tail file for topic: " + topic)
        
    def stop(n=0, e=0):
        global stop_flag
        logger.error("stopping.....")
        for t in tail:
            logger.error("stopping " + str(t))
            try:
                t.join(60)
            except:
                logger.error(traceback.format_exc())
        if conf.get_test_path() != "null":
            release()
        stop_flag = True
        
        
    signal.signal(signal.SIGTERM, stop)
    signal.signal(signal.SIGINT, stop)
    
    for t in tail:
        t.start()

    print "start ok"
    logger.error("started")
    
    while stop_flag is False:
        sleep(1)
    

    logger.error("stopped")
    
    
    

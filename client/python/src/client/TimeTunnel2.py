#! /usr/bin/env python
#encoding=utf-8
'''
Created on 2010-10-22

@author: jiugao
'''
from client.Authentication import Authentication
from client.Tunnel import Tunnel
from common.Config import Config
from connection.TunnelConnection import TCPool
from destination.Destination import Destination
from message.MessageFactory import MessageFactory
import os.path

basepath = os.path.dirname(__file__)

auth = None
conf = Config(basepath + '/../../conf/client.conf')
destination = Destination(conf)
tcPool = TCPool(destination)

def set_router(urls):
    conf.setRouters(urls)
    
def use(passport):
    global auth
    oldAuth = auth
    auth = passport
    return oldAuth

def passport(user, pwd):
    return Authentication(user, pwd)

def tunnel(name, compress=False, timeout=None, sequence=False):
    return Tunnel(name, compress, sequence, timeout)

def post(t, m, p={}):
    global tcPool, auth
    toPost = MessageFactory.createMessage(t.name, m, props=p)
    if t.compress is True:
        toPost.compress()
    return tcPool.getTC(auth , t, conf).pub(toPost)

def release():
    global tcPool, destination
    tcPool.destoy()
    destination.destroy()



    

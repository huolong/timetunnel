#! /usr/bin/env python
#encoding=utf-8
'''
Created on 2010-10-25

@author: jiugao
'''
from client.Authentication import Authentication
from json.encoder import JSONEncoder
import json
import socket

def bulidAuth(auth, url, sessionid=None):
    authInfo = {}
    authInfo['CLIENT_ID'] = socket.gethostname()
    authInfo['BROKER_URL'] = url
    authInfo['SESSION_ID'] = sessionid
    authInfo['AUTHENTICATION'] = json.dumps(auth, cls=AuthEncoder)
    return authInfo


class AuthEncoder(JSONEncoder):
    def default(self, o):
        if not isinstance (o, Authentication):
            return JSONEncoder.default(self, o);
        return {"username":o.username, "password":o.password}
    
if __name__ == '__main__':
    url = "test.com"
    sessionid = "sdfsffsdfdf"
    
    print bulidAuth(Authentication("jiugao", "qqqq"), url, sessionid)

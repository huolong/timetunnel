#! /usr/bin/env python
#encoding=utf-8
'''
Created on 2010-11-26

@author: jiugao
'''
import os
import ConfigParser
import traceback
import sys
from string import atol
from ptail.Meta import MetaInfo
import time
from threading import Thread
from ptail.Config import Config

SEP = "_"

class LogConfig(object):
    def __init__(self, path):
        self.config = ConfigParser.ConfigParser()
        try:
            fd = open(path)
        except:
            print "can not locate log conf path: " + path
            sys.exit(1)
        try:
            self.config.readfp(fd)
        except IOError:
            print 'read log conf error ' + str(traceback.format_exc())
            sys.exit(1)
        finally:
            fd.close()
    def getLogPath(self):
        value = self.config.get('handler_fileHandler', 'args')
        if value is None or value is "":
            print "no log name defined in log conf"
            sys.exit(1)
        return eval(value)[0]

basepath = os.path.dirname(__file__)
log_config = LogConfig(basepath + "/../conf/log.conf")
conf = Config(basepath + '/../conf/tailfile.conf')
       
class HealthCheck(object):
    def __init__(self, log_path, cp_path, cpname):
        self.log_path = log_path
        self.dir = cp_path
        self.name = cpname
        pass
    
    def __check_meta(self):
        print "{COLLECT CHECK INFO BEGIN}"
        f = None
        while True:
            index = self.__max_index()
            if index == -1L:
                print "no meta found"
                return repr(MetaInfo())
            meta_name = self.__bulidname(index)
            try:
                f = open(meta_name, "r")
            except:
                continue
            if os.path.exists(meta_name):
                break
            else:
                continue
        check_info = self.__load_from_file(f)
        print "{COLLECT CHECK INFO END}"
        return check_info
    
    def __bulidname(self, index):
        return self.dir + os.sep + self.name + str(index)
    
    def __load_from_file(self, f):
        times = 0;
        while True:
            try:
                meta = self.__setMeta(f)
                return repr(meta)
            except Exception:
                print "check latest file error, maybe due to write not finish, wait 2 sec"
                if times == 0:
                    time.sleep(0.7)
                    times += 1
                else:
                    print "get check meta info error, maybe due to wrong format"
                    
    def __max_index(self):
        highest_index = -1L
        if os.path.exists(self.dir) is False:
            print "cp path not exist: " + self.dir
            return highest_index
        files = os.listdir(self.dir)
        if files is None or len(files) == 0:
            print "no cp file in cp path: " + self.dir
            return highest_index
        global SEP
        
        for f in files:
            if f.startswith(self.name) is False:
                continue
            rp = f.rpartition(SEP)
            if len(rp) == 0:
                print "error filename format: " + f
                continue
            try:
                num = atol(rp[len(rp) - 1])
            except:
                print "error filename format: " + f
                continue
            if num > highest_index:
                highest_index = num
        return highest_index
            
    def __setMeta(self, open_file):       
        line = None
        try:
            line = open_file.readlines()
        except Exception:
            print traceback.format_exc()
            raise Exception("load from file for meta failed")
        finally:
            open_file.close()
        meta = MetaInfo()
        meta.set(line)
        return meta
    
    def check(self):
        print "####TT2 TAILFILE HEALTH CHECK LIST####"
        check_info = self.__check_meta()
        print "[CHECKPOINT INFO RESULT]"
        print check_info
        log_info = self.__check_log()
        print "[LOG INFO RESULT]"
        if len(log_info) == 0:
            print "NO FILE SWITCHED"
        print log_info
    
    def __check_log(self):
        print "{COLLECT LOG INFO BEGIN}"
        f = open(self.log_path, "r")
        f.seek(0, 2)
        ret = []
        i = 0
        try:
            while i < 2:
                lines = f.readlines(10000)
                for l in lines:
                    if l.startswith("CHECK_" + self.name) is False:
                        continue
                    else:
                        ret.append(l)
                if len(ret) == 0:
                    time.sleep(2)
                    i += 1
                    continue
                else:
                    break
        except Exception:
            print traceback.format_exc()
            raise Exception("read log file failed")
        finally:
            f.close()
        print "{COLLECT LOG INFO END}"
        return "".join(ret)
      
if __name__ == '__main__':
    topics = conf.get_topic_name().split(",")
    i = 0
    path = log_config.getLogPath()
    for t in topics:
        cp_path = conf.get_cp_path()
        cp_name = conf.get_cp_name().split(",")[i]
        i += 1
        HealthCheck(path, cp_path, cp_name).check()
    pass

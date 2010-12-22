#! /usr/bin/env python
#encoding=utf-8
'''
Created on 2010-12-3

@author: jiugao
'''
from string import atoi
import ConfigParser
import logging
import sys
import traceback

logger = logging.getLogger("tailfile.config")
    
class Config:
    def __init__(self, filename):
        logger.debug("init in config")
        self.confFile = filename
        self.config = ConfigParser.ConfigParser()
        try:
            fd = open(filename)
        except:
            logger.info("no config file " + filename) 
            return
        try:
            self.config.readfp(fd)
        except IOError:
            logger.error('read configuration error ' + str(traceback.format_exc()))
            sys.exit(1)
        finally:
            fd.close()
        self.__load()
    
    def __load(self):
        self.__getSection()
        self.__init_topic()
        self.__init_encoding()
        self.__init_max_buf_size()
        self.__init_test_path()
        self.__init_passport()
        self.__init_routers()
        self.__init_check_point_name()
        self.__init_cp_base_path()
        self.__init_log_base_path()
        self.__init_path_regx()
        self.__init_tmp_path()
        self.__init_topic_compress()
        self.__init_topic_timeout()
        self.__init_sequence()
        
    def __getSection(self):
        self.ret = []
        for name in self.config.sections():
            if name.startswith("topic_"):
                self.ret.append(name)
        if self.ret is None or len(self.ret) == 0:
            logger.error("no topic name defined in conf " + self.confFile)
            sys.exit(1)
    
    def __init_topic(self):
        topics = []
        for name in self.ret:
            value = self.config.get(name, 'name')
            if value is None or value is "":
                logger.error("topic name not defined in conf " + self.confFile)
                sys.exit(1)
            topics.append(value.strip())
        self.topicstr = ",".join(topics)
    
    def get_topic_name(self):
        if self.topicstr is None or self.topicstr is "":
            logger.error("no topic encoding defined in conf " + self.confFile)
            sys.exit(1)
        return self.topicstr
    
    def __init_encoding(self):
        encoding = []
        for name in self.ret:
            value = self.config.get(name, 'encoding')
            if value is None or value is "":
                logger.error("no topic encoding defined in conf " + self.confFile)
                sys.exit(1)
            encoding.append(value)
        self.encodingstr = ",".join(encoding)
        
    def get_topic_encoding(self):
        return self.encodingstr
    
    def __init_max_buf_size(self):
        value = self.config.get('app', 'max_buf_size')
        if value is None or value is "":
            logger.error("no max_buf_size defined in conf " + self.confFile + " and use default")
            self.max_buf_size = 131072
        else:
            self.max_buf_size = atoi(value)
            
    def get_max_buf_size(self):
        return self.max_buf_size
    
    def __init_test_path(self):
        value = self.config.get('local_test', 'local_test_path')
        if value is None or value is "":
            logger.error("no local_test_path defined in conf " + self.confFile + " and use default")
            self.local_test_path = "null"
        else:
            self.local_test_path = value
    
    def get_test_path(self):
        return self.local_test_path
    
    def __init_passport(self):
        value = self.config.get('timetunnel', 'passport')
        if value is None or value is "":
            logger.error("no passport defined in conf " + self.confFile)
            if self.get_test_path() == "null":
                sys.exit(1)
        self.passport = value
    
    def get_passport(self):
        return self.passport
    
    def __init_routers(self):
        value = self.config.get('timetunnel', 'routers')
        if value is None or value is "":
            logger.error("no routers defined in conf " + self.confFile)
            if self.get_test_path() == "null":
                sys.exit(1)
        self.router = value
    
    def get_routers(self):
        return self.router
    
    def __init_topic_timeout(self):
        timeout = []
        for name in self.ret:
            value = self.config.get(name, 'timeout')
            if value is None or value is "":
                logger.error("no topic timeout defined in conf " + self.confFile)
                sys.exit(1)
            timeout.append(value)
        self.timeourstr = ",".join(timeout)
        
    def get_topic_timeout(self):
        return self.timeourstr
    
    def __init_topic_compress(self):
        compress = []
        for name in self.ret:
            value = self.config.get(name, 'compress')
            if value is None or value is "":
                logger.error("no topic compress defined in conf " + self.confFile)
                sys.exit(1)
            compress.append(value)
        self.compressstr = ",".join(compress)
        
    def get_topic_compress(self):
        return self.compressstr

    def __init_cp_base_path(self):
        value = self.config.get('app', 'checkpoint_base_path')
        if value is None or value is "":
            logger.error("no checkpoint path defined in conf " + self.confFile + +" and use default")
            self.cp_base_path = "./"
        else:
            self.cp_base_path = value
    
    def get_cp_path(self):
        return self.cp_base_path

    def __init_check_point_name(self):
        cp_name = []
        for name in self.ret:
            value = self.config.get(name, 'checkpoint_base_name')
            if value is None or value is "":
                logger.error("no check point name defined in conf " + self.confFile)
                sys.exit(1)
            cp_name.append(value)
        self.checkpointstr = ",".join(cp_name)
        
    def get_cp_name(self):
        return self.checkpointstr
   
    def __init_log_base_path(self):
        base_path=[]
        for name in self.ret:
            value = self.config.get(name, 'log_base_path')
            if value is None or value is "":
                logger.error("no log_base_path defined in conf " + self.confFile)
                sys.exit(1)
            base_path.append(value)
        self.log_base_path = ",".join(base_path)
            
    def get_base_path(self):
        return self.log_base_path
    
    def __init_tmp_path(self):
        tmp = []
        for name in self.ret:
            value = self.config.get(name, 'tmp_log_fullpath')
            if value is None or value is "":
                tmp.append("null")
            else:
                tmp.append(value)
        self.tmpstr = ",".join(tmp)
    
    def get_tmp_path(self):
        return self.tmpstr
    
    def __init_path_regx(self):
        regx = []
        for name in self.ret:
            value = self.config.get(name, 'log_name_regx')
            if value is None or value is "":
                logger.error("no path_regx defined in conf " + self.confFile)
                sys.exit(1)
            regx.append(value)
        self.path_regx = ",".join(regx)
        
    def get_path_regx(self):
        return self.path_regx
    
    def __init_sequence(self):
        seq = []
        for name in self.ret:
            value = self.config.get(name, 'sequence')
            if value is None or value is "":
                logger.error("no sequence defined in conf " + self.confFile)
                sys.exit(1)
            seq.append(value)
        self.seq_str = ",".join(seq)
        
    def get_topic_sequence(self):
        return self.seq_str
        
    def __repr__(self):
        L = ["%s=%r" % (k, v) for k, v in self.__dict__.iteritems()]
        return "%s(%s)" % (self.__class__.__name__, ",".join(L))
    

if __name__ == '__main__':
    conf = Config('../../conf/tailfile.conf')
    print conf.get_topic_name()
    print conf.get_topic_encoding()
    print conf.get_max_buf_size()
    print conf.get_test_path()
    print conf.get_topic_timeout()
    print conf.get_topic_compress()
    print conf.get_tmp_path()
    print conf.get_routers()
    print conf.get_path_regx()
    print conf.get_passport()
    print conf.get_cp_path()
    print conf.get_cp_name()
    print conf.get_base_path()
    print conf.get_topic_sequence()

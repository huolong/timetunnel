#! /usr/bin/env python
#encoding=utf-8
'''
Created on 2010-11-2

@author: jiugao
'''
from ptail.Meta import Meta, MetaUrl
import os.path
from threading import Thread
import threading
import logging
from time import sleep
from ptail.Config import Config
from ptail.Sender import DiskSender, TTSender
from ptail.DirScanner import DirScanner
import traceback
import time

logger = logging.getLogger("tailfile.dirscanner")

class Tail(Thread):
    def __init__(self, index, conf):
        self.__load_conf(index, conf)
        self.q = []
        self.meta = Meta(MetaUrl(self.cp_path, self.cp_name))
        self.meta.startRecoding()
        self.meta_info = self.__load_from_meta()
        self.__check_meta()
        self.__init_sender()
        
        self.stopFlag = False
        self.stopevent = threading.Event()
        self.sleeptime = 0.1
        
        threading.Thread.__init__(self, name="thread_tail_" + self.topic)
    
    def __init_sender(self):
        if self.local_test_path != "null":
            self.sender = DiskSender(self.local_test_path, self.topic, self.encoding, self.max_buf_size)
        else:
            self.sender = TTSender(self.max_buf_size, self.topic, self.topic_timeout, self.encoding, self.topic_compress, self.topic_sequence)
            
    def __check_meta(self):
        self.scanner.scan()
        if len(self.q) == 0:
            return
        if self.tmp_path == "null":
            if self.meta_info.cur_file != "null" and self.meta_info.cur_file != self.q[0]:
                logger.error("cur file in meta: " + self.meta_info.cur_file + " not match: " + self.q[0])
                self.offset = 0L
        return
            
        
    def __load_conf(self, i, c):
        self.path_regx = c.get_path_regx().split(",")[i]
        self.base_path = c.get_base_path().split(",")[i]
        tmp_paths = c.get_tmp_path()
        if tmp_paths == "null":
            self.tmp_path = "null"
        else:
            tmpath = c.get_tmp_path().split(",")[i]
            if tmpath is "" or tmpath is "None":
                self.tmp_path = "null"
            else:
                self.tmp_path = tmpath
        self.cp_path = c.get_cp_path()
        self.cp_name = c.get_cp_name().split(",")[i]
        self.local_test_path = c.get_test_path()
        
        self.topic = c.get_topic_name().split(",")[i]
        self.encoding = c.get_topic_encoding().split(",")[i]
        self.topic_timeout = c.get_topic_timeout().split(",")[i]
        self.topic_compress = c.get_topic_compress().split(",")[i]
        self.topic_sequence = c.get_topic_sequence().split(",")[i]
        
        self.max_buf_size = c.get_max_buf_size()
        
        pass
    
    def __load_from_meta(self):
        meta_info = self.meta.restore()
        self.offset = meta_info.offset
        history_files = meta_info.last_file.split(",")
        modify_time = 0
        len_lasts=len(history_files)
        if len_lasts!=0:
            last_file=history_files[len_lasts-1]
            if os.path.exists(last_file) is True:
                modify_time = os.path.getmtime(last_file)
            else:
                modify_time = 0
                logger.error("last file in meta: " + last_file + " not exist and set modify_time to 0")
        else:
            logger.error("no last files in meta: " + last_file + " and set modify_time to 0")
            
        self.scanner = DirScanner(self.base_path, self.tmp_path, modify_time, self.path_regx, self.q, history_files)
        return meta_info
    
    def run(self):
        self.stopped = False
        while not self.stopFlag:
            try:
                self.__process()
            except:
                logger.error("abnormal error happened, exit this thread")
                break
            logger.debug("one file done")
            if len(self.q) <= 3:
                self.stopevent.wait(self.sleeptime)
                self.stopevent.clear()
        self.stopped = True
        
    def join(self, timeout=None):
        self.stopFlag = True
        self.stopevent.set()
        while not self.stopped:
            logger.error("waiting for stopped")
            time.sleep(1)
        self.meta.flush(self.meta_info)
        self.meta.close()
        self.sender.close()
        threading.Thread.join(self, timeout)
    
    def __process(self):
        self.scanner.scan()
        if len(self.q) == 0:
            return
        try:
            filename = self.q[0]
            to_read = open(filename, "rb")
            if self.tmp_path != "null" and filename == self.tmp_path:
                self.scanner.scan(True)
                new_filename = self.q[0]
                while filename != new_filename:
                    filename = new_filename
                    to_read.close()
                    to_read = open(filename, "rb")
                    self.scanner.scan(True)
                    new_filename = self.q[0]
        except IOError, e:
            logger.error(str(e))
            logger.error(traceback.format_exc())
            if filename != self.tmp_path and len(self.q) > 1:
                logger.error("CHECK_" + self.cp_name + ": file: " + filename + " no exist and skip it")
                self.__move_file()
            return
        
        self.meta_info.cur_file = filename
        self.meta.flush(self.meta_info)
        try:
            while self.stopFlag is False:
                self.offset = self.__handle_one_file(filename, to_read, self.offset)
                self.scanner.scan()
                if len(self.q) > 1:
                    self.offset = self.__handle_one_file(filename, to_read, self.offset)
                    to_read.close()
                    self.__move_file()
                    break
                sleep(0.1)
                continue
        except Exception, e:
            logger.error(str(e))
            logger.error(traceback.format_exc())
            to_read.close()
            raise e
    
    def __handle_one_file(self, filename, read_f, pos):
        """handle one file """
        while self.stopFlag is False:
            read_f.seek(0, os.SEEK_END)
            size = read_f.tell()
            if size > pos:
                read_f.seek(pos)
                c = read_f.read(self.max_buf_size)
                read_len = len(c)
                if read_len == 0:
                    break
                sended_len = self.sender.send(c, read_len, filename)
                old_pos = pos
                pos += sended_len
                logger.debug("pos: " + str(old_pos) + " len: " + str(sended_len))
                self.meta_info.offset = pos
                self.meta.flush(self.meta_info)
                if old_pos + read_len == size:
                    break
                else:
                    continue
            else:
                break
        return pos
    
    def __move_file(self):
        last_file=self.q.pop(0)
        self.meta_info.cur_file = self.q[0]
        self.meta_info.offset = 0L
        self.offset = 0L
        toDel=self.scanner.addToDel(last_file)
        self.meta_info.last_file=",".join(toDel)
        logger.error("CHECK_" + self.cp_name + ": remove file: " + self.meta_info.last_file + " and current file: " + self.meta_info.cur_file + " at offset: " + str(self.meta_info.offset))


if __name__ == '__main__':
    conf = Config("../../conf/tailfile.conf")
    tail = Tail(0, conf)
    tail.start()
    
    sleep(3)
    
    tail.join()
    
        

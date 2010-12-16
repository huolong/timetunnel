namespace java com.taobao.timetunnel2.router

enum ExReason {
  SERVICE_UNAVAILABLE = 100, 
  INVALID_USERORPWD = 200, 
  NOTFOUND_BROKERURL=201, 
  UNAUTHORIZED_CATEGORY =202 
}

exception RouterException {
  1: i16     code,        
  2: string reason,   
  3: string detail    
}

const string LOCAL_HOST = "host";
const string TYPE = "type";
const string RECVWINSIZE = "size";
const string TIMEOUT = "timeout";
const string SUBSCRIBER = "subscriber";

service RouterService{
  string getBroker(1: string user, 2: string pwd, 3:string topic, 4:string apply, 5:map<string, string> prop) throws (1:RouterException f)
}
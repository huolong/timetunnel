import static com.sun.btrace.BTraceUtils.*;

import java.util.Collection;

import com.sun.btrace.*;
import com.sun.btrace.BTraceUtils.Collections;
import com.sun.btrace.BTraceUtils.Strings;
import com.sun.btrace.annotations.*;

@BTrace
public class Tracer {
  @OnMethod(clazz = "/com.taobao.timetunnel.+/", method = "", location = @Location(value = Kind.RETURN))
  public static void trace(@ProbeClassName String clazz,
                           @ProbeMethodName String method,
                           @Self Object self,
                           @Return Object result) {
    Appendable sb = Strings.newStringBuilder();
    Strings.append(sb, method);
    println(str(sb));
  }

  private static int safeSize(Collection<?> result) {
    if (startsWith(str(result), "java")) return size(result);
    return Collections.toArray(result).length;
  }
  
  /*
   * @OnMethod(clazz = "/com.taobao.timetunnel.+/", method = "", location =
   * @Location(value = Kind.ENTRY)) public static void trace(@ProbeClassName
   * String clazz,
   * @ProbeMethodName String method,
   * @Self Object self, AnyType... args) { }
   */
}

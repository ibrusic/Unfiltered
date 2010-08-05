package unfiltered.request

/** Extractor for json request bodies. Complement to dispatch.json._ */
object JsonBody {
  import javax.servlet.http.{HttpServletRequest => Req}
  import dispatch.json._
  import dispatch.json.Js._
  
  /** @return Some(JsValue, req) if request accepts json and contains a valid json body. */
  def unapply(r: Req) = r match {
    case Accepts.Json(Bytes(body, _)) =>
      try { Some(Js(new String(body)), r) } catch { case _ => None }
    case _ => None
  } 
}

/** jsonp extractor(s). Useful for extracting a callback out of a request */
object Jsonp {
  import javax.servlet.http.{HttpServletRequest => Req}

  object Callback extends Params.Extract("callback", Params.first)
  
  trait Wrapper {
    def wrap(body: String): String
  }
  
  object EmptyWrapper extends Wrapper {
    def wrap(body: String) = body
  }
  
  class CallbackWrapper(cb: String) extends Wrapper {
    def wrap(body: String) = "%s(%s)" format(cb, body)
  }
  
  /** @return if request accepts json, (callbackwrapper, req) tuple if a callback param
      is provided else (emptywrapper, req) tuple is no callback param is provided */
  object Optional {
    def unapply(r: Req) = r match {
      case Accepts.Json(Params(p, req)) => Some(p match {
        case Callback(cb, _) => new CallbackWrapper(cb)
        case _ => EmptyWrapper
      }, r)
      case _ => None
    }
  }
  
  /** @return (callbackwrapper, req) tuple if request accepts json and a callback 
      param is provided  */
  def unapply(r: Req) = r match {
    case Accepts.Json(Params(Callback(cb, _), _)) => Some(new CallbackWrapper(cb), r)
    case _ => None
  }
}
package http

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import http.client.HTTPClient
import org.junit.Assert._
import org.junit.Test

import java.net.{MalformedURLException, ProtocolException, SocketException}
import java.text.{DecimalFormat, NumberFormat}
import scala.jdk.CollectionConverters._

class HTTPServerTests {

  private val NF: NumberFormat = DecimalFormat.getInstance()
  private var PORT_TO_USE: Int = 1024

  @Test
  def detectBadRequestManager(): Unit = {
    val opList1 = List(
      new HTTPServer.Operation(
        "GET",
        "/oplist",
        emptyOperation,
        "List of all available operations."),
      new HTTPServer.Operation(
        "POST",
        "/create/{it}",
        emptyOperation,
        "Blah."),
      new HTTPServer.Operation(
        "POST",
        "/terminate",
        emptyOperation,
        "Hard stop, shutdown. VERY unusual REST resource...")
    )

    val opList2 = List(
      new HTTPServer.Operation(
        "GET",
        "/oplist",
        emptyOperation,
        "List of all available operations."),
      new HTTPServer.Operation(
        "POST",
        "/create/{this}",
        emptyOperation,
        "Blah."),
      new HTTPServer.Operation(
        "POST",
        "/finish",
        emptyOperation,
        "Hard stop, shutdown. VERY unusual REST resource...")
    )

    val restServerImplOne = new RESTRequestManager {
      override def onRequest(request: HTTPServer.Request): HTTPServer.Response = null

      override def getRESTOperationList(): java.util.List[HTTPServer.Operation] =
        opList1.asJava
    }

    val restServerImplTwo = new RESTRequestManager {
      override def onRequest(request: HTTPServer.Request): HTTPServer.Response = null

      override def getRESTOperationList(): java.util.List[HTTPServer.Operation] =
        opList2.asJava
    }

    var httpServer: HTTPServer = null
    PORT_TO_USE += 1
    try {
      println("Starting HTTP Server...")
      httpServer = new HTTPServer(PORT_TO_USE, restServerImplOne)
      httpServer.startServer()
    } catch {
      case ex: Exception => fail(ex.toString)
    }
    assertNotNull(httpServer)
    println("... HTTP Server started.")
    try {
      httpServer.addRequestManager(restServerImplTwo)
      fail("We should not be there")
    } catch {
      case ex: IllegalArgumentException =>
        printf("As expected [%s]\n", ex.toString)
    } finally {
      println("Stopping HTTP Server...")
      httpServer.stopRunning()
      println("... HTTP Server stopped.")
    }
  }

  // Placeholder for emptyOperation method reference
  private def emptyOperation(request: HTTPServer.Request): HTTPServer.Response = {
    // Implementation here or null if empty
    null
  }
}
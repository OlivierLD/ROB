package http

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import http.client.HTTPClient
import junit.framework.TestCase
import org.junit.Assert
import org.junit.Test
import java.net.MalformedURLException
import java.net.ProtocolException
import java.net.SocketException
import java.text.DecimalFormat
import java.util.*

//import java.net.ConnectException;
//import java.text.SimpleDateFormat;
class HTTPServerKTTests {
    @Test
    fun detectBadRequestManager() {
        val opList1 = Arrays.asList(
            HTTPServer.Operation(
                "GET",
                "/oplist", { request: HTTPServer.Request -> emptyOperation(request) },
                "List of all available operations."
            ),
            HTTPServer.Operation(
                "POST",
                "/create/{it}", { request: HTTPServer.Request -> emptyOperation(request) },
                "Blah."
            ),
            HTTPServer.Operation(
                "POST",
                "/terminate", { request: HTTPServer.Request -> emptyOperation(request) },
                "Hard stop, shutdown. VERY unusual REST resource..."
            )
        )
        val opList2 = Arrays.asList(
            HTTPServer.Operation(
                "GET",
                "/oplist", { request: HTTPServer.Request -> emptyOperation(request) },
                "List of all available operations."
            ),
            HTTPServer.Operation(
                "POST",
                "/create/{this}", { request: HTTPServer.Request -> emptyOperation(request) },
                "Blah."
            ),
            HTTPServer.Operation(
                "POST",
                "/finish", { request: HTTPServer.Request -> emptyOperation(request) },
                "Hard stop, shutdown. VERY unusual REST resource..."
            )
        )
        val restServerImplOne: RESTRequestManager = object : RESTRequestManager {
            @Throws(UnsupportedOperationException::class)
            override fun onRequest(request: HTTPServer.Request): HTTPServer.Response? {
                return null
            }

            override fun getRESTOperationList(): List<HTTPServer.Operation> {
                return opList1
            }
        }
        val restServerImplTwo: RESTRequestManager = object : RESTRequestManager {
            @Throws(UnsupportedOperationException::class)
            override fun onRequest(request: HTTPServer.Request): HTTPServer.Response? {
                return null
            }

            override fun getRESTOperationList(): List<HTTPServer.Operation> {
                return opList2
            }
        }
        var httpServer: HTTPServer? = null
        PORT_TO_USE += 1
        try {
            println("Starting HTTP Server...")
            httpServer = HTTPServer(PORT_TO_USE, restServerImplOne)
            httpServer.startServer()
        } catch (ex: Exception) {
            Assert.fail(ex.toString())
        }
        TestCase.assertNotNull(httpServer)
        println("... HTTP Server started.")
        try {
            httpServer!!.addRequestManager(restServerImplTwo)
            Assert.fail("We should not be there")
        } catch (ex: IllegalArgumentException) {
            System.out.printf("As expected [%s]\n", ex.toString())
        } finally {
            println("Stopping HTTP Server...")
            httpServer!!.stopRunning()
            println("... HTTP Server stopped.")
        }
    }

    @Test
    fun detectDuplicateOperations() {
        val opList = Arrays.asList(
            HTTPServer.Operation(
                "GET",
                "/oplist", { request: HTTPServer.Request -> emptyOperation(request) },
                "List of all available operations."
            ),
            HTTPServer.Operation(
                "POST",
                "/create/{it}", { request: HTTPServer.Request -> emptyOperation(request) },
                "Blah."
            ),
            HTTPServer.Operation(
                "POST",
                "/create/{stuff}", { request: HTTPServer.Request -> emptyOperation(request) },
                "Blah."
            ),
            HTTPServer.Operation(
                "POST",
                "/terminate", { request: HTTPServer.Request -> emptyOperation(request) },
                "Hard stop, shutdown. VERY unusual REST resource..."
            )
        )
        try {
            RESTProcessorUtil.checkDuplicateOperations(opList)
            Assert.fail("Should have detected duplicate")
        } catch (ex: Exception) {
            System.out.printf("As expected: %s\n", ex.toString())
        }
    }

    @Test
    fun useCustomVerb() {
        val opList = Arrays.asList(
            HTTPServer.Operation(
                "GET",
                "/oplist", { request: HTTPServer.Request -> emptyOperation(request) },
                "List of all available operations."
            ),
            HTTPServer.Operation(
                "CUST",
                "/customverb/{it}", { request: HTTPServer.Request -> emptyOperation(request) },
                "Blah."
            )
        )
        val restServerImpl: RESTRequestManager = object : RESTRequestManager {
            @Throws(UnsupportedOperationException::class)
            override fun onRequest(request: HTTPServer.Request): HTTPServer.Response? {
                return null
            }

            override fun getRESTOperationList(): List<HTTPServer.Operation> {
                return opList
            }
        }
        var httpServer: HTTPServer? = null
        PORT_TO_USE += 1
        try {
            println("Starting HTTP Server...")
            httpServer = HTTPServer(PORT_TO_USE, restServerImpl, true)
        } catch (ex: Exception) {
            Assert.fail(ex.toString())
        }
        TestCase.assertNotNull(httpServer)
        println("... HTTP Server started")

        // Add a Shutdown Callback
        val shutdownCallback = Runnable { println("!! Server was shut down !!") }
        httpServer!!.setShutdownCallback(shutdownCallback)
        PORT_TO_USE += 1
        try {
            val ret = HTTPClient.doCustomVerb(
                "CUST",
                "http://localhost:" + PORT_TO_USE.toString() + "/cutomverb/Yo",
                null,
                null
            )
            Assert.fail("Custom protocol should not be supported.")
        } catch (pe: ProtocolException) {
            // Expected
            println("As expected.")
        } catch (ex: Exception) {
            Assert.fail(ex.toString())
        } finally {
            println("Stopping HTTP Server...")
            try {
                httpServer.stopRunning()
            } catch (ex: Exception) {
                System.err.println("Managed Exception")
                ex.printStackTrace()
            }
            println("... HTTP Server stopped")
        }
    }

    private var opList: List<HTTPServer.Operation>? = null
    @Test
    fun bombardSeveralRequests() {
        System.setProperty("http.verbose", "false")
        opList = Arrays.asList(
            HTTPServer.Operation(
                "GET",
                "/oplist", { request: HTTPServer.Request -> getOperationList(request) },
                "List of all available operations."
            )
        )
        val restServerImpl: RESTRequestManager = object : RESTRequestManager {
            @Throws(UnsupportedOperationException::class)
            override fun onRequest(request: HTTPServer.Request): HTTPServer.Response? {
                return null
            }

            override fun getRESTOperationList(): List<HTTPServer.Operation> {
                return opList as MutableList<HTTPServer.Operation>
            }
        }
        var httpServer: HTTPServer? = null
        PORT_TO_USE += 1
        try {
            println("Starting HTTP Server...")
            httpServer = HTTPServer(PORT_TO_USE, restServerImpl, true)
        } catch (ex: Exception) {
            Assert.fail(ex.toString())
        }
        TestCase.assertNotNull(httpServer)
        println("... HTTP Server started.")
        val youTellMe = Runnable { println("---- Callback: Server shutting down...") }
        httpServer!!.setShutdownCallback(youTellMe)
        var t1: Thread? = null
        var t2: Thread? = null
        var t3: Thread? = null
        try {
            val runnable = Runnable {
                System.out.printf("===> Starting client thread at %s\n", NF.format(System.currentTimeMillis()))
                try {
                    val response = HTTPClient.doGet(
                        String.format("http://localhost:%d/oplist", PORT_TO_USE),
                        null
                    ) // Response will be empty, but that 's OK.
                    System.out.printf("===> Got response at %s\n", NF.format(System.currentTimeMillis()))
                    Assert.assertNotNull("Response is null", response)
                } catch (se: SocketException) {
                    if (se.message!!.contains("Unexpected end of file from server")) {
                        // Expected
                    } else {
                        Assert.fail(se.message)
                    }
                } catch (ex: Exception) {
                    // ex.printStackTrace();
                    Assert.fail(ex.toString())
                } finally {
                    System.out.printf("===> End of Runnable at %s\n", NF.format(System.currentTimeMillis()))
                }
            }
            t1 = Thread(runnable, "T1")
            t2 = Thread(runnable, "T2")
            t3 = Thread(runnable, "T3")
            t1.start()
            t2.start()
            t3.start()
            try {
                println(">>>> Client taking a 5s nap")
                Thread.sleep(5000L)
            } catch (ie: InterruptedException) {
                ie.printStackTrace()
            }
        } catch (ex: Exception) {
            Assert.fail(ex.toString())
        } finally {
            val serverRunning = httpServer.isRunning
            System.out.printf("HTTP Server is %s running.\n", if (serverRunning) "still" else "not")
            if (true) {
                if (serverRunning) {
                    if (t1 != null && t1.isAlive || t2 != null && t2.isAlive || t3 != null && t3.isAlive) {
                        try {
                            println(">>>> Client taking a 1s nap")
                            Thread.sleep(1000L)
                        } catch (ie: InterruptedException) {
                            ie.printStackTrace()
                        }
                    }
                    println("Now stopping the server...")
                    httpServer.stopRunning()
                } else {
                    println("Who stopped the server ??")
                    Assert.fail("Who stopped the server ??")
                }
            }
        }
    }

    @Test
    fun customProtocol() {
        opList = Arrays.asList(
            HTTPServer.Operation(
                "GET",
                "/oplist", { request: HTTPServer.Request -> getOperationList(request) },
                "List of all available operations."
            )
        )
        val restServerImpl: RESTRequestManager = object : RESTRequestManager {
            @Throws(UnsupportedOperationException::class)
            override fun onRequest(request: HTTPServer.Request): HTTPServer.Response? {
                return null // Server will fail, but that's OK.
            }

            override fun getRESTOperationList(): List<HTTPServer.Operation> {
                return opList as MutableList<HTTPServer.Operation>
            }
        }
        var httpServer: HTTPServer? = null
        PORT_TO_USE += 1
        try {
            println("Starting HTTP Server...")
            httpServer = HTTPServer(PORT_TO_USE, restServerImpl, true)
        } catch (ex: Exception) {
            Assert.fail(ex.toString())
        }
        TestCase.assertNotNull(httpServer)
        println("...HTTP Server started.")
        try {
            val response = HTTPClient.doGet(
                String.format("gemini://localhost:%d/oplist", PORT_TO_USE),
                null
            ) // Response will be empty, but that 's OK.
            Assert.assertNotNull("Response is null", response)
        } catch (mue: MalformedURLException) {
            Assert.assertTrue("gemini protocol should have failed.", mue.message!!.contains("unknown protocol: gemini"))
        } catch (ex: Exception) {
            Assert.fail(ex.toString())
        } finally {
            if (httpServer!!.isRunning) {
                println("Stopping HTTP Server...")
                try {
                    httpServer.stopRunning()
                } catch (ce: Throwable) {
                    println("Oops")
                }
                println("... HTTP Server stopped")
            } else {
                println("No HTTP server to stop...")
            }
        }
    }

    private fun getOperationList(request: HTTPServer.Request): HTTPServer.Response {
        val response = HTTPServer.Response(request.protocol, HTTPServer.Response.STATUS_OK)
        var content: String? // new Gson().toJson(this.opList);
        try {
            content = ObjectMapper().writeValueAsString(opList)
        } catch (jpe: JsonProcessingException) {
            content = jpe.message
            jpe.printStackTrace()
        }
        RESTProcessorUtil.generateResponseHeaders(response, content!!.toByteArray().size)
        response.payload = content.toByteArray()
        return response
    }

    private fun emptyOperation(request: HTTPServer.Request): HTTPServer.Response {
        return HTTPServer.Response(request.protocol, HTTPServer.Response.STATUS_OK)
    }

    companion object {
        private val NF = DecimalFormat.getInstance()
        private var PORT_TO_USE = 1024
    }
}
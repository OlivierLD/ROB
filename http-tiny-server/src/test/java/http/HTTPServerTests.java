package http;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import http.client.HTTPClient;
import org.junit.Assert;
import org.junit.Test;

//import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.SocketException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
//import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class HTTPServerTests {

    private final static ObjectMapper mapper = new ObjectMapper();

    private final static NumberFormat NF = DecimalFormat.getInstance();
    private static int PORT_TO_USE = 1024;

    @Test
    public void detectBadRequestManager() {
        List<HTTPServer.Operation> opList1 = Arrays.asList(
                new HTTPServer.Operation(
                        "GET",
                        "/oplist",
                        this::emptyOperation,
                        "List of all available operations."),
                new HTTPServer.Operation(
                        "POST",
                        "/create/{it}",
                        this::emptyOperation,
                        "Blah."),
                new HTTPServer.Operation(
                        "POST",
                        "/terminate",
                        this::emptyOperation,
                        "Hard stop, shutdown. VERY unusual REST resource..."));

        List<HTTPServer.Operation> opList2 = Arrays.asList(
                new HTTPServer.Operation(
                        "GET",
                        "/oplist",
                        this::emptyOperation,
                        "List of all available operations."),
                new HTTPServer.Operation(
                        "POST",
                        "/create/{this}",
                        this::emptyOperation,
                        "Blah."),
                new HTTPServer.Operation(
                        "POST",
                        "/finish",
                        this::emptyOperation,
                        "Hard stop, shutdown. VERY unusual REST resource..."));

        RESTRequestManager restServerImplOne = new RESTRequestManager() {

            @Override
            public HTTPServer.Response onRequest(HTTPServer.Request request) throws UnsupportedOperationException {
                return null;
            }

            @Override
            public List<HTTPServer.Operation> getRESTOperationList() {
                return opList1;
            }
        };

        RESTRequestManager restServerImplTwo = new RESTRequestManager() {

            @Override
            public HTTPServer.Response onRequest(HTTPServer.Request request) throws UnsupportedOperationException {
                return null;
            }

            @Override
            public List<HTTPServer.Operation> getRESTOperationList() {
                return opList2;
            }
        };
        HTTPServer httpServer = null;
        PORT_TO_USE += 1;
        try {
            System.out.println("Starting HTTP Server...");
            httpServer = new HTTPServer(PORT_TO_USE, restServerImplOne);
            httpServer.startServer();
        } catch (Exception ex) {
            fail(ex.toString());
        }
        assertNotNull(httpServer);
        System.out.println("... HTTP Server started.");
        try {
            httpServer.addRequestManager(restServerImplTwo);
            fail("We should not be there");
        } catch (IllegalArgumentException ex) {
            System.out.printf("As expected [%s]\n", ex.toString());
        } finally {
            System.out.println("Stopping HTTP Server...");
            httpServer.stopRunning();
            System.out.println("... HTTP Server stopped.");
        }
    }

    @Test
    public void detectDuplicateOperations() {
        List<HTTPServer.Operation> opList = Arrays.asList(
                new HTTPServer.Operation(
                        "GET",
                        "/oplist",
                        this::emptyOperation,
                        "List of all available operations."),
                new HTTPServer.Operation(
                        "POST",
                        "/create/{it}",
                        this::emptyOperation,
                        "Blah."),
                new HTTPServer.Operation(
                        "POST",
                        "/create/{stuff}",
                        this::emptyOperation,
                        "Blah."),
                new HTTPServer.Operation(
                        "POST",
                        "/terminate",
                        this::emptyOperation,
                        "Hard stop, shutdown. VERY unusual REST resource..."));
        try {
            RESTProcessorUtil.checkDuplicateOperations(opList);
            fail("Should have detected duplicate");
        } catch (Exception ex) {
            System.out.printf("As expected: %s\n", ex.toString());
        }
    }

    @Test
    public void useCustomVerb() {
        List<HTTPServer.Operation> opList = Arrays.asList(
                new HTTPServer.Operation(
                        "GET",
                        "/oplist",
                        this::emptyOperation,
                        "List of all available operations."),
                new HTTPServer.Operation(
                        "CUST",
                        "/customverb/{it}",
                        this::emptyOperation,
                        "Blah."));

        RESTRequestManager restServerImpl = new RESTRequestManager() {

            @Override
            public HTTPServer.Response onRequest(HTTPServer.Request request) throws UnsupportedOperationException {
                return null;
            }

            @Override
            public List<HTTPServer.Operation> getRESTOperationList() {
                return opList;
            }
        };
        HTTPServer httpServer = null;
        PORT_TO_USE += 1;
        try {
            System.out.println("Starting HTTP Server...");
            httpServer = new HTTPServer(PORT_TO_USE, restServerImpl, true);
        } catch (Exception ex) {
            fail(ex.toString());
        }
        assertNotNull(httpServer);
        System.out.println("... HTTP Server started");

        // Add a Shutdown Callback
        Runnable shutdownCallback = () -> System.out.println("!! Server was shut down !!");
        httpServer.setShutdownCallback(shutdownCallback);

        PORT_TO_USE += 1;
        try {
            int ret = HTTPClient.doCustomVerb("CUST", "http://localhost:" + String.valueOf(PORT_TO_USE) + "/cutomverb/Yo", null, null);
            fail("Custom protocol should not be supported.");
        } catch (ProtocolException pe) {
            // Expected
            System.out.println("As expected.");
        } catch (Exception ex) {
            fail(ex.toString());
        } finally {
            System.out.println("Stopping HTTP Server...");
            try {
                httpServer.stopRunning();
            } catch (Exception ex) {
                System.err.println("Managed Exception");
                ex.printStackTrace();
            }
            System.out.println("... HTTP Server stopped");
        }
    }

    @Test
    public void addOperationDynamically() {
        List<HTTPServer.Operation> opList = new ArrayList<>();
        // Arrays.asList would not work here. It returns a final list, cannot add to it.
        opList.add(new HTTPServer.Operation(
                        "GET",
                        "/oplist",
                        (request) -> {
                            HTTPServer.Response response = new HTTPServer.Response(request.getProtocol(), HTTPServer.Response.STATUS_OK);
                            String content;
                            try {
                                content = mapper.writeValueAsString(opList); // new Gson().toJson(opList);
                            } catch (JsonProcessingException jpe) {
                                response = HTTPServer.buildErrorResponse(response,
                                        HTTPServer.Response.BAD_REQUEST,
                                        new HTTPServer.ErrorPayload()
                                                .errorCode("TEST-0001")
                                                .errorMessage(jpe.toString())
                                                .errorStack(HTTPServer.dumpException(jpe)));
                                return response;
                            }
                            RESTProcessorUtil.generateResponseHeaders(response, content.getBytes().length);
                            response.setPayload(content.getBytes());
                            return response;
                        },
                        "List of all available operations."));
        opList.add(new HTTPServer.Operation(
                        "POST",
                        "/dummy/{it}",
                        this::emptyOperation,
                        "Blah."));
        RESTRequestManager restServerImpl = new RESTRequestManager() {
            @Override
            public HTTPServer.Response onRequest(HTTPServer.Request request) throws UnsupportedOperationException {

                Optional<HTTPServer.Operation> opOp = opList
                        .stream()
                        .filter(op -> op.getVerb().equals(request.getVerb()) && RESTProcessorUtil.pathMatches(op.getPath(), request.getPath()))
                        .findFirst();
                if (opOp.isPresent()) {
                    HTTPServer.Operation op = opOp.get();
                    request.setRequestPattern(op.getPath()); // To get the prms later on.
                    HTTPServer.Response processed = op.getFn().apply(request); // Execute here.
                    return processed;
                } else {
                    throw new UnsupportedOperationException(String.format("%s not managed", request.toString()));
                }
            }

            @Override
            public List<HTTPServer.Operation> getRESTOperationList() {
                return opList;
            }
        };

        // restServerImpl.getRESTOperationList()
        opList.add(new HTTPServer.Operation("GET",
                "/pouet",
                    (request) -> {
                        // Dummy one
                        HTTPServer.Response response = new HTTPServer.Response(request.getProtocol(), HTTPServer.Response.STATUS_OK);
                        return response;
                    },
                "Dynamically added"));

        HTTPServer httpServer = null;
        // PORT_TO_USE += 1;
        try {

            final List<HTTPServer.Operation> restOperationList = restServerImpl.getRESTOperationList();
            System.out.printf("Operation List has %d element(s)\n", restOperationList.size());

            System.out.printf("Starting HTTP Server on port %d...\n", PORT_TO_USE);
            httpServer = new HTTPServer(PORT_TO_USE, restServerImpl, true);
        } catch (Exception ex) {
            fail(ex.toString());
        }
        assertNotNull(httpServer);
        System.out.println("... HTTP Server started");

        // Add a Shutdown Callback
        Runnable shutdownCallback = () -> System.out.println("!! Server was shut down !!");
        httpServer.setShutdownCallback(shutdownCallback);

        try {
            String request = String.format("http://localhost:%d/oplist", PORT_TO_USE);
            System.out.printf("Requesting: %s\n", request);
            String response = HTTPClient.doGet(request, new HashMap<>());
            System.out.printf("===> Got response at %s\n", NF.format(System.currentTimeMillis()));
            Assert.assertNotNull("Response is null", response);
            System.out.printf("%s\n", response);
        } catch (SocketException se) {
            se.printStackTrace();
            fail(se.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            fail(ex.toString());
        } finally {
            httpServer.stopRunning();
            System.out.printf("===> End of Runnable at %s\n", NF.format(System.currentTimeMillis()));
        }
        System.out.println("That's it.");
    }

    private List<HTTPServer.Operation> opList = null;
    @Test
    public void bombardSeveralRequests() {

        System.setProperty("http.verbose", "false");

        this.opList = Arrays.asList(
                new HTTPServer.Operation(
                        "GET",
                        "/oplist",
                        this::getOperationList,
                        "List of all available operations."));

        RESTRequestManager restServerImpl = new RESTRequestManager() {

            @Override
            public HTTPServer.Response onRequest(HTTPServer.Request request) throws UnsupportedOperationException {
                return null;
            }

            @Override
            public List<HTTPServer.Operation> getRESTOperationList() {
                return opList;
            }
        };
        HTTPServer httpServer = null;
        PORT_TO_USE += 1;
        try {
            System.out.println("Starting HTTP Server...");
            httpServer = new HTTPServer(PORT_TO_USE, restServerImpl, true);
        } catch (Exception ex) {
            fail(ex.toString());
        }
        assertNotNull(httpServer);
        System.out.println("... HTTP Server started.");
        Runnable youTellMe = () -> System.out.println("---- Callback: Server shutting down...");
        httpServer.setShutdownCallback(youTellMe);

        Thread t1 = null, t2 = null, t3 = null;
        try {
            Runnable runnable = () -> {
                System.out.printf("===> Starting client thread at %s\n", NF.format(System.currentTimeMillis()));
                try {
                    String response = HTTPClient.doGet(String.format("http://localhost:%d/oplist", PORT_TO_USE), null); // Response will be empty, but that 's OK.
                    System.out.printf("===> Got response at %s\n", NF.format(System.currentTimeMillis()));
                    Assert.assertNotNull("Response is null", response);
                } catch (SocketException se) {
                    if (se.getMessage().contains("Unexpected end of file from server")) {
                        // Expected
                    } else {
                        fail(se.getMessage());
                    }
                } catch (Exception ex) {
                    // ex.printStackTrace();
                    fail(ex.toString());
                } finally {
                    System.out.printf("===> End of Runnable at %s\n", NF.format(System.currentTimeMillis()));
                }
            };
            t1 = new Thread(runnable, "T1");
            t2 = new Thread(runnable, "T2");
            t3 = new Thread(runnable, "T3");
            t1.start();
            t2.start();
            t3.start();

            try {
                System.out.println(">>>> Client taking a 5s nap");
                Thread.sleep(5_000L);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }

        } catch (Exception ex) {
            fail(ex.toString());
        } finally {
            boolean serverRunning = httpServer.isRunning();
            System.out.printf("HTTP Server is %s running.\n", serverRunning ? "still" : "not");
            if (true) {
                if (serverRunning) {
                    if ((t1 != null && t1.isAlive()) || (t2 != null && t2.isAlive()) || (t3 != null && t3.isAlive())) {
                        try {
                            System.out.println(">>>> Client taking a 1s nap");
                            Thread.sleep(1_000L);
                        } catch (InterruptedException ie) {
                            ie.printStackTrace();
                        }
                    }
                    System.out.println("Now stopping the server...");
                    httpServer.stopRunning();
                } else {
                    System.out.println("Who stopped the server ??");
                    fail("Who stopped the server ??");
                }
            }
        }
    }

    @Test
    public void customProtocol() {

        this.opList = Arrays.asList(
                new HTTPServer.Operation(
                        "GET",
                        "/oplist",
                        this::getOperationList,
                        "List of all available operations."));

        RESTRequestManager restServerImpl = new RESTRequestManager() {

            @Override
            public HTTPServer.Response onRequest(HTTPServer.Request request) throws UnsupportedOperationException {
                return null; // Server will fail, but that's OK.
            }

            @Override
            public List<HTTPServer.Operation> getRESTOperationList() {
                return opList;
            }
        };
        HTTPServer httpServer = null;
        PORT_TO_USE += 1;
        try {
            System.out.println("Starting HTTP Server...");
            httpServer = new HTTPServer(PORT_TO_USE, restServerImpl, true);
        } catch (Exception ex) {
            fail(ex.toString());
        }
        assertNotNull(httpServer);
        System.out.println("...HTTP Server started.");
        try {
            String response = HTTPClient.doGet(String.format("gemini://localhost:%d/oplist", PORT_TO_USE), null); // Response will be empty, but that 's OK.
            Assert.assertNotNull("Response is null", response);
        } catch (MalformedURLException mue) {
            assertTrue ("gemini protocol should have failed.", mue.getMessage().contains("unknown protocol: gemini"));
        } catch (Exception ex) {
            fail(ex.toString());
        } finally {
            if (httpServer.isRunning()) {
                System.out.println("Stopping HTTP Server...");
                try {
                    httpServer.stopRunning();
                } catch (Throwable ce) {
                    System.out.println("Oops");
                }
                System.out.println("... HTTP Server stopped");
            } else {
                System.out.println("No HTTP server to stop...");
            }
        }
    }

    private HTTPServer.Response getOperationList(HTTPServer.Request request) {
        HTTPServer.Response response = new HTTPServer.Response(request.getProtocol(), HTTPServer.Response.STATUS_OK);

        String content; // new Gson().toJson(this.opList);
        try {
            content = new ObjectMapper().writeValueAsString(this.opList);
        } catch (JsonProcessingException jpe) {
            content = jpe.getMessage();
            jpe.printStackTrace();
        }
        RESTProcessorUtil.generateResponseHeaders(response, content.getBytes().length);
        response.setPayload(content.getBytes());
        return response;
    }

    private HTTPServer.Response emptyOperation(HTTPServer.Request request) {
        HTTPServer.Response response = new HTTPServer.Response(request.getProtocol(), HTTPServer.Response.STATUS_OK);
        return response;
    }
}
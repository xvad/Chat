import com.google.common.base.Stopwatch;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.Charset;

/**
 * @author Diego Urrutia Astorga <durrutia@ucn.cl>
 * @version 20170330130700
 */
public class ProcessRequestRunnable implements Runnable {

    /**
     * Logger de la clase
     */
    private static final Logger log = LoggerFactory.getLogger(ProcessRequestRunnable.class);

    /**
     * Socket asociado al cliente.
     */
    private Socket socket;


    /**
     * Constructor
     *
     * @param socket
     */
    public ProcessRequestRunnable(final Socket socket) {
        this.socket = socket;
    }


    /**
     * When an object implementing interface <code>Runnable</code> is used
     * to create a thread, starting the thread causes the object's
     * <code>run</code> method to be called in that separately executing
     * thread.
     * <p>
     * The general contract of the method <code>run</code> is that it may
     * take any action whatsoever.
     *
     * @see Thread#run()
     */
    @Override
    public void run() {

        // Cronometro ..
        final Stopwatch stopWatch = Stopwatch.createStarted();

        log.debug("Connection from {} in port {}.", socket.getInetAddress(), socket.getPort());

        // A dormir por un segundo.
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        try {
            processRequest(this.socket);
        } catch (Exception ex) {
            log.error("Error", ex);
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                // Nothing here
            }
        }

        log.debug("Request procesed in {}.", stopWatch);

    }

    /**
     * Procesar peticion
     *
     * @param socket
     */
    private static void processRequest(final Socket socket) throws IOException {

        // Iterador de la peticion
        final LineIterator lineIterator = IOUtils.lineIterator(socket.getInputStream(), Charset.defaultCharset());

        // Peticion
        final String request = getRequest(lineIterator);
        log.debug("Request detected: {}", request);

        // Output
        final OutputStream outputStream = IOUtils.buffer(socket.getOutputStream());

        log.debug("Writing data for: {}", request);

        // HTTP header
        writeHeader(outputStream);

        // HTTP Body
        writeBody(outputStream, request);

        // Cierro el stream
        IOUtils.closeQuietly(outputStream);

    }

    /**
     * Obtengo la linea de peticion de request.
     *
     * @param lineIterator
     * @return the request.
     */
    private static String getRequest(LineIterator lineIterator) {

        String request = null;
        int n = 0;

        while (lineIterator.hasNext()) {

            // Leo linea a linea
            final String line = lineIterator.nextLine();
            log.debug("Line {}: {}", ++n, line);

            // Guardo la peticion de la primera linea
            if (n == 1) {
                request = line;
            }

            // Termine la peticion si llegue al final de la peticion
            if (StringUtils.isEmpty(line)) {
                break;
            }

        }

        return request;
    }

    /**
     * Escribe el encabezado del protocolo HTTP.
     *
     * @param outputStream
     * @throws IOException
     */
    private static void writeHeader(OutputStream outputStream) throws IOException {

        // Header
        IOUtils.write("HTTP/1.0 200 OK\r\n", outputStream, Charset.defaultCharset());
        IOUtils.write("Content-type: text/html\r\n", outputStream, Charset.defaultCharset());

        // end-header
        IOUtils.write("\r\n", outputStream, Charset.defaultCharset());

    }

    /**
     * Escribe el body del encabezado.
     *
     * @param outputStream
     * @param request
     */
    private static void writeBody(OutputStream outputStream, String request) throws IOException {

        // Body
        final String body = "<html><head><title>WebServer v1.0</title></head><body><h3>Result:</h3><pre>CONTENT</pre></body></html>";

        final String random = RandomStringUtils.randomAlphabetic(100);

        final String result = StringUtils.replace(body, "CONTENT", random);

        IOUtils.write(result + "\r\n", outputStream, Charset.defaultCharset());

    }


}

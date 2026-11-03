package com.spring.ia.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class CodexAppServerClientProcessTest {
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void logsThreadStartJsonRpcErrorCodeAndMessageWithoutPrompt() throws Exception {
        FakeProcess process = new FakeProcess();
        var events = new LinkedBlockingQueue<JsonNode>();
        var approvalRequests = new LinkedBlockingQueue<JsonNode>();
        var logger = (Logger) LoggerFactory.getLogger(CodexAppServerClient.class);
        var appender = new ListAppender<ILoggingEvent>();
        appender.start();
        logger.addAppender(appender);
        var client = new CodexAppServerClient(mapper, "codex-stub", Path.of(".").toAbsolutePath().toString(), ignored -> process);
        var keepPipeReaderAlive = new CountDownLatch(1);
        String prompt = "private task prompt";
        try {
            BufferedReader requests = new BufferedReader(new InputStreamReader(process.clientRequests, StandardCharsets.UTF_8));
            Thread responder = new Thread(() -> {
                try {
                    requests.readLine();
                    process.emit("{\"jsonrpc\":\"2.0\",\"id\":1,\"result\":{\"userAgent\":\"stub\"}}");
                    keepPipeReaderAlive.await();
                } catch (IOException | InterruptedException e) { Thread.currentThread().interrupt(); throw new RuntimeException(e); }
            });
            responder.setDaemon(true); responder.start();
            client.start(events::add, approvalRequests::add);
            requests.readLine();
            client.startThread(prompt);
            assertThat(mapper.readTree(requests.readLine()).path("method").asText()).isEqualTo("thread/start");
            process.emit("{\"jsonrpc\":\"2.0\",\"id\":2,\"error\":{\"code\":-32001,\"message\":\"No active account\"}}");
            JsonNode event;
            do { event = events.poll(2, TimeUnit.SECONDS); } while (event != null && !"app-server/thread-start-failed".equals(event.path("method").asText()));
            assertThat(event).isNotNull();
            assertThat(appender.list).extracting(ILoggingEvent::getFormattedMessage)
                    .anySatisfy(message -> assertThat(message).contains("code=-32001", "No active account").doesNotContain(prompt));
        } finally {
            keepPipeReaderAlive.countDown();
            client.stop();
            logger.detachAppender(appender);
        }
    }

    @Test
    void negotiatesAndStartsTaskUsingOnlySimulatedAppServerProcess() throws Exception {
        FakeProcess process = new FakeProcess();
        var events = new LinkedBlockingQueue<JsonNode>();
        var approvals = new LinkedBlockingQueue<JsonNode>();
        var client = new CodexAppServerClient(mapper, "codex-stub", Path.of(".").toAbsolutePath().toString(), ignored -> process);
        var keepPipeReaderAlive = new CountDownLatch(1);
        try {
            BufferedReader requests = new BufferedReader(new InputStreamReader(process.clientRequests, StandardCharsets.UTF_8));
            var initializeLine = new AtomicReference<String>();
            Thread responder = new Thread(() -> {
                try {
                    initializeLine.set(requests.readLine());
                    process.emit("{\"jsonrpc\":\"2.0\",\"id\":1,\"result\":{\"userAgent\":\"stub\"}}");
                    keepPipeReaderAlive.await();
                }
                catch (IOException | InterruptedException e) { Thread.currentThread().interrupt(); throw new RuntimeException(e); }
            });
            responder.setDaemon(true); responder.start();
            client.start(events::add, approvals::add);
            JsonNode initialize = mapper.readTree(initializeLine.get());
            assertThat(initialize.path("method").asText()).isEqualTo("initialize");
            assertThat(initialize.path("params").path("clientInfo").path("name").asText()).isEqualTo("local-agent-orchestrator");
            assertThat(initialize.path("params").path("capabilities").path("experimentalApi").asBoolean()).isTrue();
            assertThat(mapper.readTree(requests.readLine()).path("method").asText()).isEqualTo("initialized");

            client.startThread("simulated user task");
            JsonNode threadStart = mapper.readTree(requests.readLine());
            assertThat(threadStart.path("method").asText()).isEqualTo("thread/start");
            assertThat(threadStart.path("params").path("sandbox").asText()).isEqualTo("workspace-write");
            assertThat(threadStart.path("params").path("approvalPolicy").asText()).isEqualTo("on-request");
            process.emit("""
                    {"jsonrpc":"2.0","id":2,"result":{"thread":{"id":"thread-stub"}}}
                    """);
            JsonNode turnStart = mapper.readTree(requests.readLine());
            assertThat(turnStart.path("method").asText()).isEqualTo("turn/start");
            assertThat(turnStart.path("params").path("threadId").asText()).isEqualTo("thread-stub");
            assertThat(turnStart.path("params").path("input").get(0).path("text").asText()).isEqualTo("simulated user task");

            process.emit("""
                    {"jsonrpc":"2.0","id":77,"method":"item/commandExecution/requestApproval","params":{"command":"git status","cwd":"C:/project"}}
                    """);
            assertThat(approvals.poll(2, TimeUnit.SECONDS)).isNotNull();
            client.decide("77", "accept");
            JsonNode approvalResponse = mapper.readTree(requests.readLine());
            assertThat(approvalResponse.path("id").isNumber()).isTrue();
            assertThat(approvalResponse.path("id").intValue()).isEqualTo(77);
            assertThat(approvalResponse.path("result").path("decision").asText()).isEqualTo("accept");

            process.emit("""
                    {"jsonrpc":"2.0","method":"turn/completed","params":{"threadId":"thread-stub","turn":{"status":"completed"}}}
                    """);
            JsonNode event;
            do { event = events.poll(2, TimeUnit.SECONDS); } while (event != null && !"turn/completed".equals(event.path("method").asText()));
            assertThat(event).isNotNull();
        } finally { keepPipeReaderAlive.countDown(); client.stop(); }
    }

    @Test
    void keepsApprovalPendingWhenWritingTheDecisionFails() throws Exception {
        FakeProcess process = new FakeProcess();
        var approvals = new LinkedBlockingQueue<JsonNode>();
        var client = new CodexAppServerClient(mapper, "codex-stub", Path.of(".").toAbsolutePath().toString(), ignored -> process);
        var keepPipeReaderAlive = new CountDownLatch(1);
        try {
            BufferedReader requests = new BufferedReader(new InputStreamReader(process.clientRequests, StandardCharsets.UTF_8));
            Thread responder = new Thread(() -> {
                try {
                    requests.readLine();
                    process.emit("{\"jsonrpc\":\"2.0\",\"id\":1,\"result\":{\"userAgent\":\"stub\"}}");
                    keepPipeReaderAlive.await();
                } catch (IOException | InterruptedException e) { Thread.currentThread().interrupt(); throw new RuntimeException(e); }
            });
            responder.setDaemon(true);
            responder.start();
            client.start(ignored -> {}, approvals::add);
            requests.readLine();
            process.emit("{\"jsonrpc\":\"2.0\",\"id\":42,\"method\":\"item/commandExecution/requestApproval\",\"params\":{\"command\":[\"Get-Date\"],\"cwd\":\"C:/workspace\"}}");
            assertThat(approvals.poll(2, TimeUnit.SECONDS)).isNotNull();
            process.destroy();

            org.assertj.core.api.Assertions.assertThatThrownBy(() -> client.decide("42", "accept"))
                    .isInstanceOf(IOException.class);
            org.assertj.core.api.Assertions.assertThatThrownBy(() -> client.decide("42", "accept"))
                    .isInstanceOf(IOException.class);
        } finally {
            keepPipeReaderAlive.countDown();
            client.stop();
        }
    }

    @Test
    void ignoresDisconnectFromAnOlderProcessAfterAReplacementStarts() throws Exception {
        FakeProcess first = new FakeProcess();
        first.deferStdoutCloseOnDestroy();
        FakeProcess second = new FakeProcess();
        var processes = new java.util.concurrent.LinkedBlockingQueue<>(java.util.List.of(first, second));
        var oldEvents = new LinkedBlockingQueue<JsonNode>();
        var newEvents = new LinkedBlockingQueue<JsonNode>();
        var client = new CodexAppServerClient(mapper, "codex-stub", Path.of(".").toAbsolutePath().toString(), ignored -> processes.remove());
        var keepOldReaderAlive = new CountDownLatch(1);
        var keepNewReaderAlive = new CountDownLatch(1);
        try {
            Thread oldResponder = responderFor(first, keepOldReaderAlive);
            oldResponder.start();
            client.start(oldEvents::add, ignored -> {});
            client.stop();

            Thread newResponder = responderFor(second, keepNewReaderAlive);
            newResponder.start();
            client.start(newEvents::add, ignored -> {});
            first.finishStdout();

            assertThat(newEvents.poll(300, TimeUnit.MILLISECONDS))
                    .isNull();
        } finally {
            keepOldReaderAlive.countDown();
            keepNewReaderAlive.countDown();
            client.stop();
            first.finishStdout();
        }
    }

    private Thread responderFor(FakeProcess process, CountDownLatch keepAlive) {
        return new Thread(() -> {
            try {
                BufferedReader requests = new BufferedReader(new InputStreamReader(process.clientRequests, StandardCharsets.UTF_8));
                requests.readLine();
                process.emit("{\"jsonrpc\":\"2.0\",\"id\":1,\"result\":{\"userAgent\":\"stub\"}}");
                keepAlive.await();
            } catch (IOException | InterruptedException e) { Thread.currentThread().interrupt(); throw new RuntimeException(e); }
        });
    }

    private static final class FakeProcess extends Process {
        private final PipedInputStream stdout = new PipedInputStream();
        private final PipedOutputStream stdoutWriter;
        private final ByteArrayOutputStream stdin = new ByteArrayOutputStream();
        private final PipedInputStream clientRequests = new PipedInputStream();
        private final PipedOutputStream clientWriter;
        private volatile boolean alive = true;
        private volatile boolean deferStdoutClose;
        FakeProcess() throws IOException {
            stdoutWriter = new PipedOutputStream(stdout);
            clientWriter = new PipedOutputStream(clientRequests);
        }
        void emit(String line) throws IOException { stdoutWriter.write((line.strip() + "\n").getBytes(StandardCharsets.UTF_8)); stdoutWriter.flush(); }
        void deferStdoutCloseOnDestroy() { deferStdoutClose = true; }
        void finishStdout() { try { stdoutWriter.close(); } catch (IOException ignored) {} }
        @Override public OutputStream getOutputStream() { return clientWriter; }
        @Override public InputStream getInputStream() { return stdout; }
        @Override public InputStream getErrorStream() { return new ByteArrayInputStream(new byte[0]); }
        @Override public int waitFor() throws InterruptedException { while (alive) Thread.sleep(2); return 0; }
        @Override public int exitValue() { if (alive) throw new IllegalThreadStateException(); return 0; }
        @Override public void destroy() { alive = false; if (!deferStdoutClose) finishStdout(); try { clientWriter.close(); } catch (IOException ignored) {} }
        @Override public boolean isAlive() { return alive; }
    }
}

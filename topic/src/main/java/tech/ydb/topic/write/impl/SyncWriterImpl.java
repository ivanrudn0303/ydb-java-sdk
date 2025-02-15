package tech.ydb.topic.write.impl;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import tech.ydb.topic.TopicRpc;
import tech.ydb.topic.settings.WriterSettings;
import tech.ydb.topic.write.InitResult;
import tech.ydb.topic.write.Message;
import tech.ydb.topic.write.SyncWriter;

/**
 * @author Nikolay Perfilov
 */
public class SyncWriterImpl extends WriterImpl implements SyncWriter {
    //private static final Logger logger = LoggerFactory.getLogger(SyncWriterImpl.class);

    public SyncWriterImpl(TopicRpc topicRpc, WriterSettings settings, Executor compressionExecutor) {
        super(topicRpc, settings, compressionExecutor);
    }

    @Override
    public void init() {
        initImpl();
    }

    @Override
    public InitResult initAndWait() {
        return initImpl().join();
    }

    @Override
    public void send(Message message) {
        sendImpl(message, false).join();
    }

    @Override
    public void send(Message message, long timeout, TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {
        sendImpl(message, false).get(timeout, unit);
    }

    @Override
    public void flush() {
        flushImpl().join();
    }

    @Override
    public void shutdown(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException,
            TimeoutException {
        shutdownImpl().get(timeout, unit);
    }
}

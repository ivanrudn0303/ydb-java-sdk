package tech.ydb.coordination.scenario.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tech.ydb.coordination.CoordinationClient;
import tech.ydb.coordination.CoordinationSession;
import tech.ydb.coordination.scenario.WorkingScenario;
import tech.ydb.core.Status;
import tech.ydb.proto.coordination.SemaphoreDescription;
import tech.ydb.proto.coordination.SessionRequest;

/**
 * @author Kirill Kurdyukov
 */
public class ConfigurationSubscriber extends WorkingScenario {

    private static final Logger logger = LoggerFactory.getLogger(ConfigurationSubscriber.class);

    private ConfigurationSubscriber(CoordinationClient client, Settings settings) {
        super(client, settings, ConfigurationPublisher.SEMAPHORE_LIMIT);
    }

    public static Builder newBuilder(CoordinationClient client, Observer observer) {
        return new Builder(client, observer);
    }

    private void describeSemaphore() {
        currentCoordinationSession.get().sendDescribeSemaphore(
                SessionRequest.DescribeSemaphore.newBuilder()
                        .setName(settings.getSemaphoreName())
                        .setWatchData(true)
                        .build()
        );
    }

    public interface Observer {

        void onNext(byte[] configurationData);
    }

    public static class Builder extends WorkingScenario.Builder<ConfigurationSubscriber> {

        private final Observer observer;

        public Builder(CoordinationClient client, Observer observer) {
            super(client);

            this.observer = observer;
        }

        @Override
        protected ConfigurationSubscriber buildScenario(Settings settings) {
            ConfigurationSubscriber subscriber = new ConfigurationSubscriber(client, settings);

            subscriber.start(
                    new CoordinationSession.Observer() {
                        @Override
                        public void onSessionStarted() {
                            logger.info("Starting subscriber coordination session, sessionId: {}",
                                    subscriber.currentCoordinationSession.get().getSessionId());
                        }

                        @Override
                        public void onCreateSemaphoreResult(Status status) {
                            logger.info("Creating semaphore {}, with status: {}", settings.getSemaphoreName(), status);

                            subscriber.describeSemaphore();
                        }

                        @Override
                        public void onDescribeSemaphoreResult(SemaphoreDescription semaphoreDescription,
                                                              Status status) {
                            if (status.isSuccess()) {
                                observer.onNext(
                                        semaphoreDescription.getData()
                                                .toByteArray()
                                );
                            } else {
                                logger.error(
                                        "Error describer result from configuration subscriber session, status: {}",
                                        status
                                );
                            }
                        }

                        @Override
                        public void onDescribeSemaphoreChanged(boolean dataChanged, boolean ownersChanged) {
                            if (dataChanged) {
                                subscriber.describeSemaphore();
                            }
                        }

                        @Override
                        public void onFailure(Status status) {
                            logger.error("Failed from subscriber session: {}", status);
                        }
                    }
            );

            return subscriber;
        }
    }
}

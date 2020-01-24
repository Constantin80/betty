package info.fmro.betty.threads;

import info.fmro.betty.objects.Statics;
import info.fmro.shared.enums.ExistingFundsModificationCommand;
import info.fmro.shared.enums.RulesManagerModificationCommand;
import info.fmro.shared.logic.ManagedEvent;
import info.fmro.shared.logic.ManagedMarket;
import info.fmro.shared.logic.ManagedRunner;
import info.fmro.shared.stream.objects.PoisonPill;
import info.fmro.shared.stream.objects.RunnerId;
import info.fmro.shared.stream.objects.SerializableObjectModification;
import info.fmro.shared.stream.objects.StreamObjectInterface;
import info.fmro.shared.utility.Generic;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;

public class InterfaceConnectionThread
        extends Thread {
    private static final Logger logger = LoggerFactory.getLogger(InterfaceConnectionThread.class);
    private final Socket socket;
    @SuppressWarnings("FieldAccessedSynchronizedAndUnsynchronized")
    private final InterfaceConnectionWriterThread writerThread;
    private final LinkedBlockingQueue<StreamObjectInterface> sendQueue = new LinkedBlockingQueue<>();

    public InterfaceConnectionThread(final Socket socket) {
        super();
        this.socket = socket;
        this.writerThread = new InterfaceConnectionWriterThread(socket, this.sendQueue);
    }

//    public synchronized void sendObject(@NotNull final StreamObjectInterface object) {
//        this.sendQueue.add(object);
//    }

    public synchronized void closeSocket() {
        logger.info("closing InterfaceConnectionThread socket");
        Generic.closeObjects(this.socket);
    }

    private synchronized void runAfterReceive(@NotNull final StreamObjectInterface receivedCommand) {
        if (receivedCommand instanceof SerializableObjectModification) {
            final SerializableObjectModification<?> serializableObjectModification = (SerializableObjectModification<?>) receivedCommand;
            final Enum<?> command = serializableObjectModification.getCommand();
            if (command instanceof RulesManagerModificationCommand) {
                final RulesManagerModificationCommand rulesManagerModificationCommand = (RulesManagerModificationCommand) command;
                final Object[] objectsToModify = serializableObjectModification.getArray();
                switch (rulesManagerModificationCommand) {
                    case addManagedEvent:
                        if (objectsToModify != null && objectsToModify.length == 2) {
                            if (objectsToModify[0] instanceof String && objectsToModify[1] instanceof ManagedEvent) {
                                final String eventId = (String) objectsToModify[0];
                                final ManagedEvent managedEvent = (ManagedEvent) objectsToModify[1];
                                Statics.rulesManagerThread.addManagedEvent(eventId, managedEvent);
                            } else {
                                logger.error("wrong objectsToModify class in betty runAfterReceive: {} {} {}", Generic.objectToString(objectsToModify), rulesManagerModificationCommand.name(), Generic.objectToString(receivedCommand));
                            }
                        } else {
                            logger.error("wrong size objectsToModify in betty runAfterReceive: {} {} {}", Generic.objectToString(objectsToModify), rulesManagerModificationCommand.name(), Generic.objectToString(receivedCommand));
                        }
                        break;
                    case removeManagedEvent:
                        if (objectsToModify != null && objectsToModify.length == 1) {
                            if (objectsToModify[0] instanceof String) {
                                final String eventId = (String) objectsToModify[0];
                                Statics.rulesManagerThread.removeManagedEvent(eventId);
                            } else {
                                logger.error("wrong objectsToModify class in betty runAfterReceive: {} {} {}", Generic.objectToString(objectsToModify), rulesManagerModificationCommand.name(), Generic.objectToString(receivedCommand));
                            }
                        } else {
                            logger.error("wrong size objectsToModify in betty runAfterReceive: {} {} {}", Generic.objectToString(objectsToModify), rulesManagerModificationCommand.name(), Generic.objectToString(receivedCommand));
                        }
                        break;
                    case addManagedMarket:
                        if (objectsToModify != null && objectsToModify.length == 2) {
                            if (objectsToModify[0] instanceof String && objectsToModify[1] instanceof ManagedMarket) {
                                final String marketId = (String) objectsToModify[0];
                                final ManagedMarket managedMarket = (ManagedMarket) objectsToModify[1];
                                Statics.rulesManagerThread.addManagedMarket(marketId, managedMarket);
                            } else {
                                logger.error("wrong objectsToModify class in betty runAfterReceive: {} {} {}", Generic.objectToString(objectsToModify), rulesManagerModificationCommand.name(), Generic.objectToString(receivedCommand));
                            }
                        } else {
                            logger.error("wrong size objectsToModify in betty runAfterReceive: {} {} {}", Generic.objectToString(objectsToModify), rulesManagerModificationCommand.name(), Generic.objectToString(receivedCommand));
                        }
                        break;
                    case removeManagedMarket:
                        if (objectsToModify != null && objectsToModify.length == 1) {
                            if (objectsToModify[0] instanceof String) {
                                final String marketId = (String) objectsToModify[0];
                                Statics.rulesManagerThread.removeManagedMarket(marketId);
                            } else {
                                logger.error("wrong objectsToModify class in betty runAfterReceive: {} {} {}", Generic.objectToString(objectsToModify), rulesManagerModificationCommand.name(), Generic.objectToString(receivedCommand));
                            }
                        } else {
                            logger.error("wrong size objectsToModify in betty runAfterReceive: {} {} {}", Generic.objectToString(objectsToModify), rulesManagerModificationCommand.name(), Generic.objectToString(receivedCommand));
                        }
                        break;
                    case setEventAmountLimit:
                        if (objectsToModify != null && objectsToModify.length == 2) {
                            if (objectsToModify[0] instanceof String && objectsToModify[1] instanceof Double) {
                                final String eventId = (String) objectsToModify[0];
                                final Double newAmount = (Double) objectsToModify[1];
                                Statics.rulesManagerThread.setEventAmountLimit(eventId, newAmount, Statics.pendingOrdersThread, Statics.orderCache, Statics.safetyLimits, Statics.marketCataloguesMap);
                            } else {
                                logger.error("wrong objectsToModify class in betty runAfterReceive: {} {} {}", Generic.objectToString(objectsToModify), rulesManagerModificationCommand.name(), Generic.objectToString(receivedCommand));
                            }
                        } else {
                            logger.error("wrong size objectsToModify in betty runAfterReceive: {} {} {}", Generic.objectToString(objectsToModify), rulesManagerModificationCommand.name(), Generic.objectToString(receivedCommand));
                        }
                        break;
                    case addManagedRunner:
                        if (objectsToModify != null && objectsToModify.length == 1) {
                            if (objectsToModify[0] instanceof ManagedRunner) {
                                final ManagedRunner managedRunner = (ManagedRunner) objectsToModify[0];
                                Statics.rulesManagerThread.addManagedRunner(managedRunner);
                            } else {
                                logger.error("wrong objectsToModify class in betty runAfterReceive: {} {} {}", Generic.objectToString(objectsToModify), rulesManagerModificationCommand.name(), Generic.objectToString(receivedCommand));
                            }
                        } else {
                            logger.error("wrong size objectsToModify in betty runAfterReceive: {} {} {}", Generic.objectToString(objectsToModify), rulesManagerModificationCommand.name(), Generic.objectToString(receivedCommand));
                        }
                        break;
                    case removeManagedRunner:
                        if (objectsToModify != null && objectsToModify.length == 2) {
                            if (objectsToModify[0] instanceof String && objectsToModify[1] instanceof RunnerId) {
                                final String marketId = (String) objectsToModify[0];
                                final RunnerId runnerId = (RunnerId) objectsToModify[1];
                                Statics.rulesManagerThread.removeManagedRunner(marketId, runnerId);
                            } else {
                                logger.error("wrong objectsToModify class in betty runAfterReceive: {} {} {}", Generic.objectToString(objectsToModify), rulesManagerModificationCommand.name(), Generic.objectToString(receivedCommand));
                            }
                        } else {
                            logger.error("wrong size objectsToModify in betty runAfterReceive: {} {} {}", Generic.objectToString(objectsToModify), rulesManagerModificationCommand.name(), Generic.objectToString(receivedCommand));
                        }
                        break;
                    case setMarketAmountLimit:
                        if (objectsToModify != null && objectsToModify.length == 2) {
                            if (objectsToModify[0] instanceof String && objectsToModify[1] instanceof Double) {
                                final String marketId = (String) objectsToModify[0];
                                final Double amountLimit = (Double) objectsToModify[1];
                                Statics.rulesManagerThread.setMarketAmountLimit(marketId, amountLimit);
                            } else {
                                logger.error("wrong objectsToModify class in betty runAfterReceive: {} {} {}", Generic.objectToString(objectsToModify), rulesManagerModificationCommand.name(), Generic.objectToString(receivedCommand));
                            }
                        } else {
                            logger.error("wrong size objectsToModify in betty runAfterReceive: {} {} {}", Generic.objectToString(objectsToModify), rulesManagerModificationCommand.name(), Generic.objectToString(receivedCommand));
                        }
                        break;
                    case setBackAmountLimit:
                        if (objectsToModify != null && objectsToModify.length == 3) {
                            if (objectsToModify[0] instanceof String && objectsToModify[1] instanceof RunnerId && objectsToModify[2] instanceof Double) {
                                final String marketId = (String) objectsToModify[0];
                                final RunnerId runnerId = (RunnerId) objectsToModify[1];
                                final Double amountLimit = (Double) objectsToModify[2];
                                Statics.rulesManagerThread.setRunnerBackAmountLimit(marketId, runnerId, amountLimit);
                            } else {
                                logger.error("wrong objectsToModify class in betty runAfterReceive: {} {} {}", Generic.objectToString(objectsToModify), rulesManagerModificationCommand.name(), Generic.objectToString(receivedCommand));
                            }
                        } else {
                            logger.error("wrong size objectsToModify in betty runAfterReceive: {} {} {}", Generic.objectToString(objectsToModify), rulesManagerModificationCommand.name(), Generic.objectToString(receivedCommand));
                        }
                        break;
                    case setLayAmountLimit:
                        if (objectsToModify != null && objectsToModify.length == 3) {
                            if (objectsToModify[0] instanceof String && objectsToModify[1] instanceof RunnerId && objectsToModify[2] instanceof Double) {
                                final String marketId = (String) objectsToModify[0];
                                final RunnerId runnerId = (RunnerId) objectsToModify[1];
                                final Double amountLimit = (Double) objectsToModify[2];
                                Statics.rulesManagerThread.setRunnerLayAmountLimit(marketId, runnerId, amountLimit);
                            } else {
                                logger.error("wrong objectsToModify class in betty runAfterReceive: {} {} {}", Generic.objectToString(objectsToModify), rulesManagerModificationCommand.name(), Generic.objectToString(receivedCommand));
                            }
                        } else {
                            logger.error("wrong size objectsToModify in betty runAfterReceive: {} {} {}", Generic.objectToString(objectsToModify), rulesManagerModificationCommand.name(), Generic.objectToString(receivedCommand));
                        }
                        break;
                    case setMinBackOdds:
                        if (objectsToModify != null && objectsToModify.length == 3) {
                            if (objectsToModify[0] instanceof String && objectsToModify[1] instanceof RunnerId && objectsToModify[2] instanceof Double) {
                                final String marketId = (String) objectsToModify[0];
                                final RunnerId runnerId = (RunnerId) objectsToModify[1];
                                final Double odds = (Double) objectsToModify[2];
                                Statics.rulesManagerThread.setRunnerMinBackOdds(marketId, runnerId, odds);
                            } else {
                                logger.error("wrong objectsToModify class in betty runAfterReceive: {} {} {}", Generic.objectToString(objectsToModify), rulesManagerModificationCommand.name(), Generic.objectToString(receivedCommand));
                            }
                        } else {
                            logger.error("wrong size objectsToModify in betty runAfterReceive: {} {} {}", Generic.objectToString(objectsToModify), rulesManagerModificationCommand.name(), Generic.objectToString(receivedCommand));
                        }
                        break;
                    case setMaxLayOdds:
                        if (objectsToModify != null && objectsToModify.length == 3) {
                            if (objectsToModify[0] instanceof String && objectsToModify[1] instanceof RunnerId && objectsToModify[2] instanceof Double) {
                                final String marketId = (String) objectsToModify[0];
                                final RunnerId runnerId = (RunnerId) objectsToModify[1];
                                final Double odds = (Double) objectsToModify[2];
                                Statics.rulesManagerThread.setRunnerMaxLayOdds(marketId, runnerId, odds);
                            } else {
                                logger.error("wrong objectsToModify class in betty runAfterReceive: {} {} {}", Generic.objectToString(objectsToModify), rulesManagerModificationCommand.name(), Generic.objectToString(receivedCommand));
                            }
                        } else {
                            logger.error("wrong size objectsToModify in betty runAfterReceive: {} {} {}", Generic.objectToString(objectsToModify), rulesManagerModificationCommand.name(), Generic.objectToString(receivedCommand));
                        }
                        break;
                    default:
                        logger.error("unknown rulesManagerModificationCommand in betty runAfterReceive: {} {}", rulesManagerModificationCommand.name(), Generic.objectToString(receivedCommand));
                } // end switch
            } else if (command instanceof ExistingFundsModificationCommand) {
                final ExistingFundsModificationCommand existingFundsModificationCommand = (ExistingFundsModificationCommand) command;
                final Object[] objectsToModify = serializableObjectModification.getArray();
                switch (existingFundsModificationCommand) {
                    case setCurrencyRate:
                        if (objectsToModify != null && objectsToModify.length == 1) {
                            if (objectsToModify[0] instanceof Double) {
                                final Double rate = (Double) objectsToModify[0];
                                Statics.safetyLimits.setCurrencyRate(rate);
                            } else {
                                logger.error("wrong objectsToModify class in betty runAfterReceive: {} {} {}", Generic.objectToString(objectsToModify), existingFundsModificationCommand.name(), Generic.objectToString(receivedCommand));
                            }
                        } else {
                            logger.error("wrong size objectsToModify in betty runAfterReceive: {} {} {}", Generic.objectToString(objectsToModify), existingFundsModificationCommand.name(), Generic.objectToString(receivedCommand));
                        }
                        break;
                    case setReserve:
                        if (objectsToModify != null && objectsToModify.length == 1) {
                            if (objectsToModify[0] instanceof Double) {
                                final Double reserve = (Double) objectsToModify[0];
                                Statics.safetyLimits.setReserve(reserve);
                            } else {
                                logger.error("wrong objectsToModify class in betty runAfterReceive: {} {} {}", Generic.objectToString(objectsToModify), existingFundsModificationCommand.name(), Generic.objectToString(receivedCommand));
                            }
                        } else {
                            logger.error("wrong size objectsToModify in betty runAfterReceive: {} {} {}", Generic.objectToString(objectsToModify), existingFundsModificationCommand.name(), Generic.objectToString(receivedCommand));
                        }
                        break;
                    default:
                        logger.error("unknown safetyLimitsModificationCommand in betty runAfterReceive: {} {}", existingFundsModificationCommand.name(), Generic.objectToString(receivedCommand));
                } // end switch
            } else {
                logger.error("unknown command object in betty runAfterReceive: {} {} {} {}", command == null ? null : command.getClass(), command, receivedCommand.getClass(), Generic.objectToString(receivedCommand));
            }
        } else {
            logger.error("unknown receivedCommand object in betty runAfterReceive: {} {}", receivedCommand.getClass(), Generic.objectToString(receivedCommand));
        }
    }

    @Override
    public void run() {
        this.writerThread.start();
        ObjectInputStream objectInputStream = null;
        try {
            //noinspection resource,IOResourceOpenedButNotSafelyClosed
            objectInputStream = new ObjectInputStream(this.socket.getInputStream());

            Object receivedObject;
            do {
                receivedObject = objectInputStream.readObject();
                if (receivedObject instanceof StreamObjectInterface) {
                    final StreamObjectInterface receivedCommand = (StreamObjectInterface) receivedObject;

                    runAfterReceive(receivedCommand);
                } else if (receivedObject == null) { // nothing to be done, will reach end of loop and exit loop
                } else {
                    logger.error("unknown type of object in interfaceConnection stream: {} {}", receivedObject.getClass(), Generic.objectToString(receivedObject));
                }
            } while (receivedObject != null && !Statics.mustStop.get() && !this.writerThread.finished.get());
        } catch (IOException iOException) {
            if (Statics.mustStop.get()) { // program is stopping and the socket has been closed from another thread
            } else {
                logger.error("IOException in interfaceConnection thread", iOException);
            }
        } catch (ClassNotFoundException e) {
            logger.error("ClassNotFoundException in interfaceConnection thread", e);
        } finally {
            //noinspection ConstantConditions
            Generic.closeObjects(objectInputStream, this.socket);
            this.writerThread.finished.set(true);
        }

        if (this.writerThread.isAlive()) {
            this.sendQueue.add(new PoisonPill());
//            logger.info("joining writerThread thread");
            try {
                this.writerThread.join();
            } catch (InterruptedException e) {
                logger.error("InterruptedException in interfaceConnection thread end", e);
            }
        }
        logger.info("reached the end of interfaceConnectionThread");
    }
}

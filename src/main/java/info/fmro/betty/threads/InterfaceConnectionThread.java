package info.fmro.betty.threads;

import info.fmro.betty.objects.Statics;
import info.fmro.shared.entities.Event;
import info.fmro.shared.entities.MarketCatalogue;
import info.fmro.shared.enums.CommandType;
import info.fmro.shared.enums.ExistingFundsModificationCommand;
import info.fmro.shared.enums.PrefSide;
import info.fmro.shared.enums.RulesManagerModificationCommand;
import info.fmro.shared.enums.SynchronizedMapModificationCommand;
import info.fmro.shared.logic.ManagedEvent;
import info.fmro.shared.logic.ManagedMarket;
import info.fmro.shared.logic.ManagedRunner;
import info.fmro.shared.objects.SharedStatics;
import info.fmro.shared.stream.objects.PoisonPill;
import info.fmro.shared.stream.objects.RunnerId;
import info.fmro.shared.stream.objects.SerializableObjectModification;
import info.fmro.shared.stream.objects.StreamObjectInterface;
import info.fmro.shared.stream.objects.StreamSynchronizedMap;
import info.fmro.shared.utility.Generic;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLException;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashSet;
import java.util.TreeSet;
import java.util.concurrent.LinkedBlockingQueue;

@SuppressWarnings({"OverlyComplexClass", "OverlyCoupledClass"})
public class InterfaceConnectionThread
        extends Thread {
    private static final Logger logger = LoggerFactory.getLogger(InterfaceConnectionThread.class);
    private final Socket socket;
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

    @SuppressWarnings({"OverlyComplexMethod", "OverlyLongMethod", "SwitchStatementDensity", "unchecked", "OverlyNestedMethod", "OverlyCoupledMethod"})
    private synchronized void runAfterReceive(@NotNull final StreamObjectInterface receivedCommand) {
        if (receivedCommand instanceof final SerializableObjectModification<?> serializableObjectModification) {
            final Enum<?> command = serializableObjectModification.getCommand();
            //noinspection ChainOfInstanceofChecks
            if (command instanceof final RulesManagerModificationCommand rulesManagerModificationCommand) {
                final Object[] objectsToModify = serializableObjectModification.getArray();
                switch (rulesManagerModificationCommand) {
                    case addManagedEvent:
                        if (objectsToModify != null && objectsToModify.length == 2) {
                            if (objectsToModify[0] instanceof final String eventId && objectsToModify[1] instanceof final ManagedEvent managedEvent) {
                                Statics.rulesManagerThread.rulesManager.addManagedEvent(eventId, managedEvent);
                            } else {
                                logger.error("wrong objectsToModify class in betty runAfterReceive: {} {} {}", Generic.objectToString(objectsToModify), rulesManagerModificationCommand.name(), Generic.objectToString(receivedCommand));
                            }
                        } else {
                            logger.error("wrong size objectsToModify in betty runAfterReceive: {} {} {}", Generic.objectToString(objectsToModify), rulesManagerModificationCommand.name(), Generic.objectToString(receivedCommand));
                        }
                        break;
                    case removeManagedEvent:
                        if (objectsToModify != null && objectsToModify.length == 1) {
                            if (objectsToModify[0] instanceof final String eventId) {
                                Statics.rulesManagerThread.rulesManager.removeManagedEvent(eventId, Statics.marketCataloguesMap);
                            } else {
                                logger.error("wrong objectsToModify class in betty runAfterReceive: {} {} {}", Generic.objectToString(objectsToModify), rulesManagerModificationCommand.name(), Generic.objectToString(receivedCommand));
                            }
                        } else {
                            logger.error("wrong size objectsToModify in betty runAfterReceive: {} {} {}", Generic.objectToString(objectsToModify), rulesManagerModificationCommand.name(), Generic.objectToString(receivedCommand));
                        }
                        break;
                    case addManagedMarket:
                        if (objectsToModify != null && objectsToModify.length == 2) {
                            if (objectsToModify[0] instanceof final String marketId && objectsToModify[1] instanceof final ManagedMarket managedMarket) {
                                Statics.rulesManagerThread.rulesManager.addManagedMarket(marketId, managedMarket, Statics.marketCataloguesMap, Statics.eventsMap);
                            } else {
                                logger.error("wrong objectsToModify class in betty runAfterReceive: {} {} {}", Generic.objectToString(objectsToModify), rulesManagerModificationCommand.name(), Generic.objectToString(receivedCommand));
                            }
                        } else {
                            logger.error("wrong size objectsToModify in betty runAfterReceive: {} {} {}", Generic.objectToString(objectsToModify), rulesManagerModificationCommand.name(), Generic.objectToString(receivedCommand));
                        }
                        break;
                    case removeManagedMarket:
                        if (objectsToModify != null && objectsToModify.length == 1) {
                            if (objectsToModify[0] instanceof final String marketId) {
                                Statics.rulesManagerThread.rulesManager.removeManagedMarket(marketId, Statics.marketCataloguesMap);
                            } else {
                                logger.error("wrong objectsToModify class in betty runAfterReceive: {} {} {}", Generic.objectToString(objectsToModify), rulesManagerModificationCommand.name(), Generic.objectToString(receivedCommand));
                            }
                        } else {
                            logger.error("wrong size objectsToModify in betty runAfterReceive: {} {} {}", Generic.objectToString(objectsToModify), rulesManagerModificationCommand.name(), Generic.objectToString(receivedCommand));
                        }
                        break;
                    case setEventAmountLimit:
                        if (objectsToModify != null && objectsToModify.length == 2) {
                            if (objectsToModify[0] instanceof final String eventId && objectsToModify[1] instanceof final Double newAmount) {
                                Statics.rulesManagerThread.rulesManager.setEventAmountLimit(eventId, newAmount, Statics.safetyLimits.existingFunds, Statics.marketCataloguesMap);
                            } else {
                                logger.error("wrong objectsToModify class in betty runAfterReceive: {} {} {}", Generic.objectToString(objectsToModify), rulesManagerModificationCommand.name(), Generic.objectToString(receivedCommand));
                            }
                        } else {
                            logger.error("wrong size objectsToModify in betty runAfterReceive: {} {} {}", Generic.objectToString(objectsToModify), rulesManagerModificationCommand.name(), Generic.objectToString(receivedCommand));
                        }
                        break;
                    case addManagedRunner:
                        if (objectsToModify != null && objectsToModify.length == 1) {
                            if (objectsToModify[0] instanceof final ManagedRunner managedRunner) {
                                Statics.rulesManagerThread.rulesManager.addManagedRunner(managedRunner, Statics.marketCataloguesMap, Statics.eventsMap);
                            } else {
                                logger.error("wrong objectsToModify class in betty runAfterReceive: {} {} {}", Generic.objectToString(objectsToModify), rulesManagerModificationCommand.name(), Generic.objectToString(receivedCommand));
                            }
                        } else {
                            logger.error("wrong size objectsToModify in betty runAfterReceive: {} {} {}", Generic.objectToString(objectsToModify), rulesManagerModificationCommand.name(), Generic.objectToString(receivedCommand));
                        }
                        break;
                    case removeManagedRunner:
                        if (objectsToModify != null && objectsToModify.length == 2) {
                            if (objectsToModify[0] instanceof final String marketId && objectsToModify[1] instanceof final RunnerId runnerId) {
                                Statics.rulesManagerThread.rulesManager.removeManagedRunner(marketId, runnerId);
                            } else {
                                logger.error("wrong objectsToModify class in betty runAfterReceive: {} {} {}", Generic.objectToString(objectsToModify), rulesManagerModificationCommand.name(), Generic.objectToString(receivedCommand));
                            }
                        } else {
                            logger.error("wrong size objectsToModify in betty runAfterReceive: {} {} {}", Generic.objectToString(objectsToModify), rulesManagerModificationCommand.name(), Generic.objectToString(receivedCommand));
                        }
                        break;
                    case setMarketAmountLimit:
                        if (objectsToModify != null && objectsToModify.length == 2) {
                            if (objectsToModify[0] instanceof final String marketId && objectsToModify[1] instanceof final Double amountLimit) {
                                Statics.rulesManagerThread.rulesManager.setMarketAmountLimit(marketId, amountLimit, Statics.safetyLimits.existingFunds);
                            } else {
                                logger.error("wrong objectsToModify class in betty runAfterReceive: {} {} {}", Generic.objectToString(objectsToModify), rulesManagerModificationCommand.name(), Generic.objectToString(receivedCommand));
                            }
                        } else {
                            logger.error("wrong size objectsToModify in betty runAfterReceive: {} {} {}", Generic.objectToString(objectsToModify), rulesManagerModificationCommand.name(), Generic.objectToString(receivedCommand));
                        }
                        break;
                    case setBackAmountLimit:
                        if (objectsToModify != null && objectsToModify.length == 3) {
                            if (objectsToModify[0] instanceof final String marketId && objectsToModify[1] instanceof final RunnerId runnerId && objectsToModify[2] instanceof final Double amountLimit) {
                                Statics.rulesManagerThread.rulesManager.setRunnerBackAmountLimit(marketId, runnerId, amountLimit);
                            } else {
                                logger.error("wrong objectsToModify class in betty runAfterReceive: {} {} {}", Generic.objectToString(objectsToModify), rulesManagerModificationCommand.name(), Generic.objectToString(receivedCommand));
                            }
                        } else {
                            logger.error("wrong size objectsToModify in betty runAfterReceive: {} {} {}", Generic.objectToString(objectsToModify), rulesManagerModificationCommand.name(), Generic.objectToString(receivedCommand));
                        }
                        break;
                    case setLayAmountLimit:
                        if (objectsToModify != null && objectsToModify.length == 3) {
                            if (objectsToModify[0] instanceof final String marketId && objectsToModify[1] instanceof final RunnerId runnerId && objectsToModify[2] instanceof final Double amountLimit) {
                                Statics.rulesManagerThread.rulesManager.setRunnerLayAmountLimit(marketId, runnerId, amountLimit);
                            } else {
                                logger.error("wrong objectsToModify class in betty runAfterReceive: {} {} {}", Generic.objectToString(objectsToModify), rulesManagerModificationCommand.name(), Generic.objectToString(receivedCommand));
                            }
                        } else {
                            logger.error("wrong size objectsToModify in betty runAfterReceive: {} {} {}", Generic.objectToString(objectsToModify), rulesManagerModificationCommand.name(), Generic.objectToString(receivedCommand));
                        }
                        break;
                    case setMinBackOdds:
                        if (objectsToModify != null && objectsToModify.length == 3) {
                            if (objectsToModify[0] instanceof final String marketId && objectsToModify[1] instanceof final RunnerId runnerId && objectsToModify[2] instanceof final Double odds) {
                                Statics.rulesManagerThread.rulesManager.setRunnerMinBackOdds(marketId, runnerId, odds);
                            } else {
                                logger.error("wrong objectsToModify class in betty runAfterReceive: {} {} {}", Generic.objectToString(objectsToModify), rulesManagerModificationCommand.name(), Generic.objectToString(receivedCommand));
                            }
                        } else {
                            logger.error("wrong size objectsToModify in betty runAfterReceive: {} {} {}", Generic.objectToString(objectsToModify), rulesManagerModificationCommand.name(), Generic.objectToString(receivedCommand));
                        }
                        break;
                    case setMaxLayOdds:
                        if (objectsToModify != null && objectsToModify.length == 3) {
                            if (objectsToModify[0] instanceof final String marketId && objectsToModify[1] instanceof final RunnerId runnerId && objectsToModify[2] instanceof final Double odds) {
                                Statics.rulesManagerThread.rulesManager.setRunnerMaxLayOdds(marketId, runnerId, odds);
                            } else {
                                logger.error("wrong objectsToModify class in betty runAfterReceive: {} {} {}", Generic.objectToString(objectsToModify), rulesManagerModificationCommand.name(), Generic.objectToString(receivedCommand));
                            }
                        } else {
                            logger.error("wrong size objectsToModify in betty runAfterReceive: {} {} {}", Generic.objectToString(objectsToModify), rulesManagerModificationCommand.name(), Generic.objectToString(receivedCommand));
                        }
                        break;
                    case setMarketName:
                        if (objectsToModify != null && objectsToModify.length == 2) {
                            if (objectsToModify[0] instanceof final String marketId && objectsToModify[1] instanceof final String marketName) {
                                Statics.rulesManagerThread.rulesManager.setMarketName(marketId, marketName);
                            } else {
                                logger.error("wrong objectsToModify class in runAfterReceive: {} {} {}", Generic.objectToString(objectsToModify), rulesManagerModificationCommand.name(), Generic.objectToString(receivedCommand));
                            }
                        } else {
                            logger.error("wrong size objectsToModify in runAfterReceive: {} {} {}", Generic.objectToString(objectsToModify), rulesManagerModificationCommand.name(), Generic.objectToString(receivedCommand));
                        }
                        break;
                    case setEventName:
                        if (objectsToModify != null && objectsToModify.length == 2) {
                            if (objectsToModify[0] instanceof final String eventId && objectsToModify[1] instanceof final String eventName) {
                                Statics.rulesManagerThread.rulesManager.setEventName(eventId, eventName);
                            } else {
                                logger.error("wrong objectsToModify class in runAfterReceive: {} {} {}", Generic.objectToString(objectsToModify), rulesManagerModificationCommand.name(), Generic.objectToString(receivedCommand));
                            }
                        } else {
                            logger.error("wrong size objectsToModify in runAfterReceive: {} {} {}", Generic.objectToString(objectsToModify), rulesManagerModificationCommand.name(), Generic.objectToString(receivedCommand));
                        }
                        break;
                    case setMarketEnabled:
                        if (objectsToModify != null && objectsToModify.length == 2) {
                            if (objectsToModify[0] instanceof final String marketId && objectsToModify[1] instanceof final Boolean marketEnabled) {
                                Statics.rulesManagerThread.rulesManager.setMarketEnabled(marketId, marketEnabled);
                            } else {
                                logger.error("wrong objectsToModify class in runAfterReceive: {} {} {}", Generic.objectToString(objectsToModify), rulesManagerModificationCommand.name(), Generic.objectToString(receivedCommand));
                            }
                        } else {
                            logger.error("wrong size objectsToModify in runAfterReceive: {} {} {}", Generic.objectToString(objectsToModify), rulesManagerModificationCommand.name(), Generic.objectToString(receivedCommand));
                        }
                        break;
                    case setMarketMandatoryPlace:
                        if (objectsToModify != null && objectsToModify.length == 2) {
                            if (objectsToModify[0] instanceof final String marketId && objectsToModify[1] instanceof final Boolean mandatoryPlace) {
                                Statics.rulesManagerThread.rulesManager.setMarketMandatoryPlace(marketId, mandatoryPlace);
                            } else {
                                logger.error("wrong objectsToModify class in runAfterReceive: {} {} {}", Generic.objectToString(objectsToModify), rulesManagerModificationCommand.name(), Generic.objectToString(receivedCommand));
                            }
                        } else {
                            logger.error("wrong size objectsToModify in runAfterReceive: {} {} {}", Generic.objectToString(objectsToModify), rulesManagerModificationCommand.name(), Generic.objectToString(receivedCommand));
                        }
                        break;
                    case setRunnerMandatoryPlace:
                        if (objectsToModify != null && objectsToModify.length == 3) {
                            if (objectsToModify[0] instanceof final String marketId && objectsToModify[1] instanceof final RunnerId runnerId && objectsToModify[2] instanceof final Boolean mandatoryPlace) {
                                Statics.rulesManagerThread.rulesManager.setRunnerMandatoryPlace(marketId, runnerId, mandatoryPlace);
                            } else {
                                logger.error("wrong objectsToModify class in betty runAfterReceive: {} {} {}", Generic.objectToString(objectsToModify), rulesManagerModificationCommand.name(), Generic.objectToString(receivedCommand));
                            }
                        } else {
                            logger.error("wrong size objectsToModify in betty runAfterReceive: {} {} {}", Generic.objectToString(objectsToModify), rulesManagerModificationCommand.name(), Generic.objectToString(receivedCommand));
                        }
                        break;
                    case setMarketKeepAtInPlay:
                        if (objectsToModify != null && objectsToModify.length == 2) {
                            if (objectsToModify[0] instanceof final String marketId && objectsToModify[1] instanceof final Boolean keepAtInPlay) {
                                Statics.rulesManagerThread.rulesManager.setMarketKeepAtInPlay(marketId, keepAtInPlay);
                            } else {
                                logger.error("wrong objectsToModify class in runAfterReceive: {} {} {}", Generic.objectToString(objectsToModify), rulesManagerModificationCommand.name(), Generic.objectToString(receivedCommand));
                            }
                        } else {
                            logger.error("wrong size objectsToModify in runAfterReceive: {} {} {}", Generic.objectToString(objectsToModify), rulesManagerModificationCommand.name(), Generic.objectToString(receivedCommand));
                        }
                        break;
                    case setRunnerPrefSide:
                        if (objectsToModify != null && objectsToModify.length == 3) {
                            if (objectsToModify[0] instanceof final String marketId && objectsToModify[1] instanceof final RunnerId runnerId && objectsToModify[2] instanceof final PrefSide prefSide) {
                                Statics.rulesManagerThread.rulesManager.setRunnerPrefSide(marketId, runnerId, prefSide);
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
            } else if (command instanceof final ExistingFundsModificationCommand existingFundsModificationCommand) {
                final Object[] objectsToModify = serializableObjectModification.getArray();
                switch (existingFundsModificationCommand) {
                    case setCurrencyRate:
                        if (objectsToModify != null && objectsToModify.length == 1) {
                            if (objectsToModify[0] instanceof final Double rate) {
                                Statics.safetyLimits.existingFunds.setCurrencyRate(rate);
                            } else {
                                logger.error("wrong objectsToModify class in betty runAfterReceive: {} {} {}", Generic.objectToString(objectsToModify), existingFundsModificationCommand.name(), Generic.objectToString(receivedCommand));
                            }
                        } else {
                            logger.error("wrong size objectsToModify in betty runAfterReceive: {} {} {}", Generic.objectToString(objectsToModify), existingFundsModificationCommand.name(), Generic.objectToString(receivedCommand));
                        }
                        break;
                    case setReserve:
                        if (objectsToModify != null && objectsToModify.length == 1) {
                            if (objectsToModify[0] instanceof final Double reserve) {
                                Statics.safetyLimits.existingFunds.setReserve(reserve);
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
            } else if (command instanceof @NotNull final SynchronizedMapModificationCommand synchronizedMapModificationCommand) {
                final Object[] objectsToModify = serializableObjectModification.getArray();
                if (objectsToModify == null || !(objectsToModify[0] instanceof final Class<?> clazz)) {
                    logger.error("improper objectsToModify for SynchronizedMapModificationCommand: {} {} {}", Generic.objectToString(objectsToModify), synchronizedMapModificationCommand.name(), Generic.objectToString(receivedCommand));
                } else {
                    @Nullable final StreamSynchronizedMap<String, Serializable> mapToUse;

                    if (MarketCatalogue.class.equals(clazz)) {
                        //noinspection rawtypes
                        mapToUse = (StreamSynchronizedMap) Statics.marketCataloguesMap;
                    } else if (Event.class.equals(clazz)) {
                        //noinspection rawtypes
                        mapToUse = (StreamSynchronizedMap) Statics.eventsMap;
                    } else {
                        logger.error("unknown streamSynchronizedMap class in runAfterReceive for: {} {}", clazz, Generic.objectToString(receivedCommand));
                        mapToUse = null;
                    }

                    if (mapToUse == null) { // nothing to do, error message was already printed
                    } else {
                        final int nObjects = objectsToModify.length;
                        switch (synchronizedMapModificationCommand) {
                            case refresh:
                                if (nObjects == 1) {
                                    if (Event.class.equals(clazz)) {
                                        SharedStatics.threadPoolExecutor.execute(new LaunchCommandThread(CommandType.checkEventResultList));
                                    } else {
                                        logger.error("unsupported refresh map in runAfterReceive: {} {} {} {}", clazz, Generic.objectToString(objectsToModify), synchronizedMapModificationCommand.name(), Generic.objectToString(receivedCommand));
                                    }
                                } else {
                                    logger.error("wrong size objectsToModify in betty runAfterReceive: {} {} {}", Generic.objectToString(objectsToModify), synchronizedMapModificationCommand.name(), Generic.objectToString(receivedCommand));
                                }
                                break;
                            case getMarkets:
                                if (nObjects > 1) {
                                    if (Event.class.equals(clazz)) {
                                        final HashSet<Event> events = new HashSet<>(Generic.getCollectionCapacity(nObjects - 1));
                                        for (int i = 1; i < nObjects; i++) {
                                            events.add((Event) objectsToModify[i]);
                                        }
                                        SharedStatics.threadPoolExecutor.execute(new LaunchCommandThread(CommandType.findMarkets, events));
                                    } else //noinspection ConstantConditions
                                        if (MarketCatalogue.class.equals(clazz)) {
                                            final TreeSet<String> marketIds = new TreeSet<>();
                                            for (int i = 1; i < nObjects; i++) {
                                                marketIds.add((String) objectsToModify[i]);
                                            }
                                            SharedStatics.threadPoolExecutor.execute(new LaunchCommandThread(CommandType.findMarkets, marketIds));
                                        } else {
                                            logger.error("unsupported getMarkets map in runAfterReceive: {} {} {} {}", clazz, Generic.objectToString(objectsToModify), synchronizedMapModificationCommand.name(), Generic.objectToString(receivedCommand));
                                        }
                                } else {
                                    logger.error("wrong size objectsToModify in betty runAfterReceive: {} {} {}", Generic.objectToString(objectsToModify), synchronizedMapModificationCommand.name(), Generic.objectToString(receivedCommand));
                                }
                                break;
                            default:
                                logger.error("unsupported synchronizedMapModificationCommand in betty runAfterReceive: {} {}", synchronizedMapModificationCommand.name(), Generic.objectToString(receivedCommand));
                        }
                    }
                }
            } else {
                logger.error("unknown command object in betty runAfterReceive: {} {} {} {}", command == null ? null : command.getClass(), command, receivedCommand.getClass(), Generic.objectToString(receivedCommand));
            }
        } else {
            logger.error("unknown receivedCommand object in betty runAfterReceive: {} {}", receivedCommand.getClass(), Generic.objectToString(receivedCommand));
        }
    }

    public void closeSocket() { // this is hard shutoff, probably best to not synchronize
        logger.info("closing InterfaceConnectionThread socket");
        Generic.closeObject(this.socket);
    }

    @Override
    public void run() {
        this.writerThread.start();
        ObjectInputStream objectInputStream = null;
        try {
            objectInputStream = new ObjectInputStream(this.socket.getInputStream());

            Object receivedObject;
            do {
                receivedObject = objectInputStream.readObject();
                if (receivedObject instanceof final StreamObjectInterface receivedCommand) {

                    runAfterReceive(receivedCommand);
                } else if (receivedObject == null) { // nothing to be done, will reach end of loop and exit loop
                } else {
                    logger.error("unknown type of object in interfaceConnection stream: {} {}", receivedObject.getClass(), Generic.objectToString(receivedObject));
                }
            } while (receivedObject != null && !SharedStatics.mustStop.get() && !this.writerThread.finished.get());
        } catch (IOException iOException) {
            @NotNull final Class<?> eClass = iOException.getClass();
            if ((eClass.equals(SocketException.class) || eClass.equals(SSLException.class)) && (SharedStatics.mustStop.get() || this.writerThread.finished.get())) { // normal, the socket has been closed from another thread
            } else if (eClass.equals(EOFException.class)) {
                this.writerThread.finished.set(true); // set here as well, not just in finally, for speed, as there's a racing condition
                logger.info("EOFException received in InterfaceConnectionThread, thread ending: {}", iOException.toString());
            } else {
                logger.warn("IOException in interfaceConnection thread", iOException);
            }
        } catch (ClassNotFoundException e) {
            logger.error("ClassNotFoundException in interfaceConnection thread", e);
        } finally {
            this.writerThread.finished.set(true);
            //noinspection ConstantConditions
            Generic.closeObjects(objectInputStream, this.socket);
        }

        if (this.writerThread.isAlive()) {
            this.sendQueue.add(new PoisonPill());
//            this.writerThread.close();
//            logger.info("joining writerThread thread");
            try {
                this.writerThread.join();
            } catch (InterruptedException e) {
                logger.error("InterruptedException in interfaceConnection thread end", e);
            }
        }
        logger.debug("reached the end of interfaceConnectionThread");
    }
}

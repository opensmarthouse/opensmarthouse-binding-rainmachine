/**
 * Copyright (c) 2010-2021 Contributors to the OpenSmartHouse project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.opensmarthouse.binding.rainmachine.internal.handler;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.UnDefType;
import org.opensmarthouse.binding.rainmachine.internal.RainMachineAddressCache;
import org.opensmarthouse.binding.rainmachine.internal.RainMachineBindingConstants;
import org.opensmarthouse.binding.rainmachine.internal.RainMachineException;
import org.opensmarthouse.binding.rainmachine.internal.api.RainMachineApiVersion;
import org.opensmarthouse.binding.rainmachine.internal.api.RainMachineCommunicator;
import org.opensmarthouse.binding.rainmachine.internal.api.RainMachineDeviceInformation;
import org.opensmarthouse.binding.rainmachine.internal.api.RainMachineDiagnostics;
import org.opensmarthouse.binding.rainmachine.internal.api.RainMachineZoneInformation;
import org.opensmarthouse.binding.rainmachine.internal.api.RainMachineZonesInformation;
import org.opensmarthouse.binding.rainmachine.internal.config.RainMachineConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link RainMachineBridgeHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Chris Jackson - Initial contribution
 */
@NonNullByDefault
public class RainMachineBridgeHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(RainMachineBridgeHandler.class);

    private final String PROPERTY_APIVERSION = "version_api";
    private final String PROPERTY_HWVERSION = "version_hardware";
    private final String PROPERTY_SWVERSION = "version_software";

    private final String PROPERTY_UPTIME = "uptime";

    private final String VERSION_APIVERSION = "apiVer";
    private final String VERSION_HWVERSION = "hwVer";
    private final String VERSION_SWVERSION = "swVer";

    private final String DIAG_UPTIME = "uptime";

    private @Nullable RainMachineCommunicator device = null;
    private @NonNullByDefault({}) RainMachineConfiguration config = null;

    private @Nullable ScheduledFuture<?> updateJob = null;

    private Map<Integer, RainMachineZoneHandler> zoneHandlers = new HashMap<>();

    private final RainMachineAddressCache hostAddressCache;

    private static final Long MAXIMUM_REFRESH_PERIOD = 3000L;
    private Long lastZoneUpdate = 0L;
    private @Nullable RainMachineZonesInformation zonesCache;

    /*
     * Constructor class. Only call the parent constructor
     */
    public RainMachineBridgeHandler(final Bridge bridge, final RainMachineAddressCache hostAddressCache) {
        super(bridge);
        this.hostAddressCache = hostAddressCache;
        this.updateJob = null;
        updateStatus(ThingStatus.OFFLINE);
    }

    @Override
    public void initialize() {
        config = getConfigAs(RainMachineConfiguration.class);

        try {
            device = new RainMachineCommunicator(config.host, config.password);
            RainMachineApiVersion versions = device.getVersions();
            getThing().setProperty(PROPERTY_APIVERSION, versions.apiVer);
            getThing().setProperty(PROPERTY_HWVERSION, versions.hwVer);
            getThing().setProperty(PROPERTY_SWVERSION, versions.swVer);
            updateStatus(ThingStatus.ONLINE);
        } catch (RainMachineException e) {
            logger.debug("RainMaker exception initialising communicator and getting versions", e);
            updateStatus(ThingStatus.OFFLINE);
        }

        startUpdateJob();
    }

    @Override
    public void dispose() {
        stopUpdateJob();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command == RefreshType.REFRESH) {
            // scheduler.execute(this::updateBridge);
        }
    }

    /**
     * Registers a zone for status updates
     *
     * @param zone the zone ID
     * @param zoneHandler the handler that will receive the status updates
     */
    protected void registerZoneStatusCallback(int zone, RainMachineZoneHandler zoneHandler) {
        logger.debug("Zone {}: Callback registered", zone);

        zoneHandlers.put(zone, zoneHandler);
        startUpdateJob();
    }

    private void startUpdateJob() {
        stopUpdateJob();
        logger.debug("Starting RainMachine Update Job");
        this.updateJob = scheduler.scheduleWithFixedDelay(this::updateBridge, 0, config.refresh, TimeUnit.SECONDS);

        logger.debug("RainMachine sucessfully initialized. Starting status poll at: {}", config.refresh);
    }

    private void stopUpdateJob() {
        final ScheduledFuture<?> updateJob = this.updateJob;
        if (updateJob != null && !updateJob.isDone()) {
            logger.debug("Stopping RainMachine Update Job");
            updateJob.cancel(false);
        }

        this.updateJob = null;
    }

    private synchronized boolean updateBridge() {
        logger.debug("RainMaker updating bridge");
        try {
            RainMachineDiagnostics diagnostics = device.getDiagnostics();
            getThing().setProperty(PROPERTY_UPTIME, diagnostics.uptime);

            RainMachineDeviceInformation deviceInfo = device.getDeviceInfo();

            if (deviceInfo.rainSensorRainStart == null) {
                updateState(RainMachineBindingConstants.CHANNEL_ID_LASTRAIN, UnDefType.UNDEF);
            } else {
                updateState(RainMachineBindingConstants.CHANNEL_ID_LASTRAIN,
                        new StringType(deviceInfo.rainSensorRainStart.toString()));
            }

            if (zonesCache == null || System.currentTimeMillis() - lastZoneUpdate > MAXIMUM_REFRESH_PERIOD) {
                zonesCache = device.getZones();
                lastZoneUpdate = System.currentTimeMillis();
            }

            for (RainMachineZoneInformation zone : zonesCache.zones) {
                RainMachineZoneHandler zoneHandler = zoneHandlers.get(zone.uid);
                if (zoneHandler != null) {
                    zoneHandler.updateZoneInformation(zone);
                }
            }

        } catch (RainMachineException e) {
            logger.debug("RainMaker exception getting diagnostics", e);
        }

        updateStatus(ThingStatus.ONLINE);

        return true;
    }
}

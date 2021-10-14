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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.opensmarthouse.binding.rainmachine.internal.RainMachineBindingConstants;
import org.opensmarthouse.binding.rainmachine.internal.api.RainMachineZoneInformation;
import org.opensmarthouse.binding.rainmachine.internal.config.RainMachineZoneConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link RainMachineZoneHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Chris Jackson - Initial contribution
 */
@NonNullByDefault
public class RainMachineZoneHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(RainMachineZoneHandler.class);

    private final String PROPERTY_APIVERSION = "version_api";
    private final String PROPERTY_HWVERSION = "version_hardware";
    private final String PROPERTY_SWVERSION = "version_software";

    private @NonNullByDefault({}) RainMachineZoneConfiguration config = null;

    private @Nullable Bridge bridge;

    /*
     * Constructor class. Only call the parent constructor
     */
    public RainMachineZoneHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        config = getConfigAs(RainMachineZoneConfiguration.class);

        Bridge bridge = getBridge();
        if (bridge == null) {
            logger.debug("Zone {}: RainMachine bridget not found when initialising zone", config.uid);
            return;
        }

        bridgeStatusChanged(bridge.getStatusInfo());
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        logger.debug("Zone {}: RainMaker bridge status changed to {}", config.uid, bridgeStatusInfo.getStatus());

        if (getBridge() == null || getBridge().getHandler() == null) {
            logger.debug("Zone {}: RainMaker bridge was not found!", config.uid);
            return;
        }
        RainMachineBridgeHandler bridgeHandler = (RainMachineBridgeHandler) getBridge().getHandler();
        bridgeHandler.registerZoneStatusCallback(config.uid, this);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command == RefreshType.REFRESH) {
            // scheduler.execute(this::updateBridge);
        }
    }

    /**
     * Method called by the bridge when zone information is updated
     *
     * @param zone the latest {@link RainMachineZoneInformation}. If null, then the bridge has detected an error.
     */
    protected void updateZoneInformation(@Nullable RainMachineZoneInformation zone) {
        if (zone == null) {
            updateStatus(ThingStatus.OFFLINE);
            return;
        }

        updateStatus(ThingStatus.ONLINE);
        logger.debug("Zone {}: RainMaker zone updated to {}.", config.uid, zone);

        updateState(RainMachineBindingConstants.CHANNEL_ID_ZONE_STATE, new DecimalType(zone.state));
    }

}

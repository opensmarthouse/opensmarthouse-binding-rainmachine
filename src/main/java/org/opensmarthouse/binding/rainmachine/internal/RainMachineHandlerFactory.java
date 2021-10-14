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
package org.opensmarthouse.binding.rainmachine.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.opensmarthouse.binding.rainmachine.internal.handler.RainMachineBridgeHandler;
import org.opensmarthouse.binding.rainmachine.internal.handler.RainMachineZoneHandler;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link RainMachineHandlerFactory} is responsible for creating RainMachine things and thing
 * handlers.
 *
 * @author Chris Jackson - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.rainmachine")
@NonNullByDefault
public class RainMachineHandlerFactory extends BaseThingHandlerFactory {

    private final RainMachineAddressCache hostAddressCache;

    @Activate
    public RainMachineHandlerFactory(@Reference final HttpClientFactory httpClientFactory,
            @Reference final RainMachineAddressCache hostAddressCache) {
        this.hostAddressCache = hostAddressCache;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return RainMachineBindingConstants.SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(RainMachineBindingConstants.RAINMACHINE_BRIDGE)) {
            return new RainMachineBridgeHandler((Bridge) thing, hostAddressCache);
        }

        if (thingTypeUID.equals(RainMachineBindingConstants.RAINMACHINE_ZONE)) {
            return new RainMachineZoneHandler(thing);
        }

        return null;
    }
}

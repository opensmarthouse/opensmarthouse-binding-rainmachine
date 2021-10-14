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
package org.opensmarthouse.binding.rainmachine.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link RainMachineConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Chris Jackson - Initial contribution
 */

@NonNullByDefault
public class RainMachineConfiguration {

    /**
     * Hostname for the RainMachine API.
     */
    public String host = "";

    /**
     * The password to connect to the RainMachine API.
     */
    public String password = "pw";

    /**
     * Number of seconds in between refreshes from the RainMachine device.
     */
    public int refresh = 60;
}

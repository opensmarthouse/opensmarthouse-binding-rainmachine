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

/**
 * The {@link RainMachineException} class defines an exception for handling
 * RainMachineExceptions
 *
 * @author Chris Jackson - Initial contribution
 */

@NonNullByDefault
public class RainMachineException extends Exception {
    private static final long serialVersionUID = 2247293108913709712L;

    public RainMachineException(String message) {
        super(message);
    }
}

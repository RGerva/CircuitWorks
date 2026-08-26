/**
 * Interface: IOperationalComponent
 * Defines the contract for implementations of this type.
 *
 * <p>Created by: superuser
 * <p>On: 2026/ago.
 *
 * <p>GitHub: https://github.com/RGerva
 *
 * <p>All Rights Reserved.
 */

package com.rgerva.circuitworks.electrical.component;

public interface IOperationalComponent extends IElectricalComponent {

    ComponentOperationalStatus getOperationalStatus();

    default boolean isOperational() {
        return getOperationalStatus().isOperational();
    }
}
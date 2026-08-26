/**
 * Interface: IElectricalSource
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

import com.rgerva.circuitworks.electrical.api.ElectricalPort;

public interface IElectricalSource extends ICurrentLimitedComponent {
    double getVoltage();

    double getInternalResistance();

    ElectricalPort getPositiveTerminal();

    ElectricalPort getNegativeTerminal();
}

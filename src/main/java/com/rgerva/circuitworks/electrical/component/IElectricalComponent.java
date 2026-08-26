/**
 * Interface: IElectricalComponent
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
import com.rgerva.circuitworks.electrical.api.ElectricalState;
import com.rgerva.circuitworks.electrical.api.IElectricalDevice;

import java.util.List;

public interface IElectricalComponent extends IElectricalDevice {

    List<ElectricalPort> getPorts();

    void updateElectricalState(ElectricalState state);
}

/**
 * Interface: IThermalComponent
 * Defines the contract for implementations of this type.
 *
 * <p>Created by: superuser
 * <p>On: 2026/ago.
 *
 * <p>GitHub: https://github.com/RGerva
 *
 * <p>All Rights Reserved.
 */

package com.rgerva.circuitworks.electrical.thermal;

public interface IThermalComponent {

    ThermalState getThermalState();

    ThermalProperties getThermalProperties();

    ThermalLimits getThermalLimits();

    ThermalStatus getThermalStatus();

    void updateThermalState(double ambientTemperature, double deltaSeconds);
}
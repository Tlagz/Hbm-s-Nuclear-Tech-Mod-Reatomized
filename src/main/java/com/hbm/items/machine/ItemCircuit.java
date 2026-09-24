package com.hbm.items.machine;

import com.hbm.interfaces.IOrderedEnum;

/** Circuits, one item per type (ModItems.circuit), in the creative tab order of the original's getSubItems */
public class ItemCircuit {

	public static enum EnumCircuitType implements IOrderedEnum {
		VACUUM_TUBE,
		CAPACITOR,
		CAPACITOR_TANTALIUM,
		PCB,
		SILICON,
		CHIP,
		CHIP_BISMOID,
		ANALOG,
		BASIC,
		ADVANCED,
		CAPACITOR_BOARD,
		BISMOID,
		CONTROLLER_CHASSIS,
		CONTROLLER,
		CONTROLLER_ADVANCED,
		QUANTUM,
		CHIP_QUANTUM,
		CONTROLLER_QUANTUM,
		ATOMIC_CLOCK,
		NUMITRON,
		CRYSTAL;

		@Override
		public Enum[] getOrder() {
			return new Enum[] {
					VACUUM_TUBE, NUMITRON, CAPACITOR, CAPACITOR_TANTALIUM, ATOMIC_CLOCK, PCB, SILICON, CHIP, CHIP_BISMOID, CHIP_QUANTUM,
					ANALOG, BASIC, ADVANCED, CAPACITOR_BOARD, BISMOID, QUANTUM, CRYSTAL,
					CONTROLLER_CHASSIS, CONTROLLER, CONTROLLER_ADVANCED, CONTROLLER_QUANTUM
			};
		}
	}
}

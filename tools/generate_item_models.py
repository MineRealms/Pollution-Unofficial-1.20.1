#!/usr/bin/env python3
"""Generates placeholder item models for the Pollution item registry."""

from __future__ import annotations

import json
from pathlib import Path

PROJECT_ROOT = Path(__file__).resolve().parents[1]
ITEM_MODELS = PROJECT_ROOT / "src" / "main" / "resources" / "assets" / "pollution" / "models" / "item"

PLACEHOLDER_TEXTURE = "minecraft:item/paper"

ITEMS = [
    "packaged_aura_node",

    # goggles / wings / baubles / food
    "nano_goggles", "quantum_goggles", "wing_nano", "wing_quantum",
    "baubles.water_ring", "heart_fruit_i",

    # batteries
    "magic_battery.hull.lv", "magic_battery.hull.mv", "magic_battery.hull.hv",
    "magic_battery.hull.ev", "magic_battery.hull.iv", "magic_battery.hull.luv",
    "magic_battery.hull.zpm", "magic_battery.hull.uv",
    "battery.lv.magic", "battery.mv.magic", "battery.hv.magic", "battery.ev.magic",
    "battery.iv.magic", "battery.luv.magic", "battery.zpm.magic", "battery.uv.magic",

    # circuits
    "magic_circuit.ulv", "magic_circuit.lv", "magic_circuit.mv", "magic_circuit.hv",
    "magic_circuit.ev", "magic_circuit.iv", "magic_circuit.luv", "magic_circuit.zpm",
    "magic_circuit.uv", "magic_circuit.uhv", "magic_circuit.uev", "magic_circuit.uiv",
    "magic_circuit.uxv", "magic_circuit.opv", "magic_circuit.max",

    # filters
    "filter.i", "filter.ii", "filter.iii", "filter.iv", "filter.v",

    # catalyst cores / runes / upgrade cores
    "blank_catalyst_core", "hot_catalyst_core", "cold_catalyst_core",
    "integration_catalyst_core", "segregation_catalyst_core", "coking_catalyst_core",
    "evolution_catalyst_core",
    "white_rune", "black_rune", "starry_rune",
    "vis_checker", "energy_reduce", "time_increase", "overclocking_enhance",
    "parallel_enhance", "transform_enhance",

    # magic components
    "core_of_idea", "bottle_of_phlogistonic_oneness", "auto_elenchus_device",
    "elucidator_of_four_causes", "symptomatic_vis_data_link",
    "needle_of_mystic_interpellation", "cogito_defibrillator", "ball_in_itself",

    # philosophical stones
    "stone_of_philosopher_1", "stone_of_philosopher_2", "stone_of_philosopher_3",
    "stone_of_philosopher_4", "stone_of_philosopher_final",

    # slimes
    "tar_slime", "sugar_slime", "glue_slime", "glycerol_slime", "rubber_slime",

    # blood culture templates / circuits / materials
    "blood_circuit", "blood_circuit_advanced", "blood_circuit_ultimate", "blood_circuit_supreme",
    "blood_circuit.0", "blood_circuit.1", "blood_circuit.2", "blood_circuit.3",
    "blood_circuit.4", "blood_circuit.5", "blood_circuit.6", "blood_circuit.7",
    "blood_circuit.8", "blood_circuit.9", "blood_circuit.10", "blood_circuit.11",
    "blood_circuit.12",
    "primitive_meat", "rat_brain", "mitochondrion_power", "endorphins_stabilizer",
    "freeze_cooler", "lysosome_stabilizer", "ips_human_brain", "blood_port",

    # astral integration cores / intermediates
    "astral_lens_basic", "astral_lens_advanced", "celestial_calibration_core",
    "harmonizing_rune_core", "astral_blood_catalyst", "astral_neural_bundle",
    "primordial_star_blood_crystal", "causality_catalyst",
    "silvered_glass_lens", "astral_resonance_coil", "mana_resonance_coil",
    "sterile_slate_blank", "precision_rune_blank", "rock_crystal_seed",
    "celestial_crystal_embryo", "cultivated_crystal", "natural_infused_coil",
    "magic_control_assembly_ev", "node_stabilization_frame",
    "blank_tarot_card", "arcane_ink_capsule",

    # magic circuit boards
    "magic_circuit_board.ulv", "magic_circuit_board.lv", "magic_circuit_board.mv",
    "magic_circuit_board.hv", "magic_circuit_board.ev", "magic_circuit_board.iv",
    "magic_circuit_board.luv", "magic_circuit_board.zpm", "magic_circuit_board.uv",
    "magic_circuit_board.uhv", "magic_circuit_board.uev", "magic_circuit_board.uiv",
    "magic_circuit_board.uxv", "magic_circuit_board.opv", "magic_circuit_board.max",

    # recipe-first functional carriers / stubs
    "attuned_crystal_wafer", "constellation_data_wafer", "living_magic_biofilm",
    "depleted_magic_core", "starstream_linker", "magic_sweep",

    # utilities
    "pesticide.empty", "pesticide.full", "devay_pill.empty",
    "devay_pill.1", "devay_pill.5", "devay_pill.10", "devay_pill.20",

    # tarots
    "test_item", "the_fool", "the_magician", "the_high_priestess", "the_empress",
    "the_emperor", "the_highophant", "the_lovers", "the_chariot", "the_strength",
    "the_hermit", "the_wheel_of_fortune", "the_justice", "the_hanged_man",
    "the_death", "the_temperance", "the_devil", "the_tower", "the_star",
    "the_moon", "the_sun", "the_judgement", "the_world",
    "test",
]


def main() -> int:
    ITEM_MODELS.mkdir(parents=True, exist_ok=True)
    for name in ITEMS:
        payload = {
            "parent": "minecraft:item/generated",
            "textures": {"layer0": PLACEHOLDER_TEXTURE},
        }
        (ITEM_MODELS / f"{name}.json").write_text(json.dumps(payload, indent=2) + "\n", encoding="utf-8")
    print(f"wrote {len(ITEMS)} item models")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())

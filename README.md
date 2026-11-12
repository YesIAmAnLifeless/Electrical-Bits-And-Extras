# Create: Electrical Bits And Extras (NeoForge 1.21.1)

Addon for Create 6.0.x + Create: Power Grid 0.6.x.

## Blocks and items
| Name | What it does |
|---|---|
| Custom Transformer Core (Ponder: hold W on it; made by sequenced assembly: copper, gold, iron sheets, starting from a copper sheet with a Deployer + Press) | Wrench alone → Small Custom Voltage Transformer (slider, max 640 V). Wrench two cores side by side → Large (2x1, max 2000 V); four in a 2x2 wall → Huge (no limit). |
| Large Custom Voltage Transformer | See above. |
| Radioisotope Thermoelectric Generator | Sequenced assembly from a Copper Casing, x3: Polished Rose Quartz, Iron Sheet, Gold Sheet, press, Blazing Acid. Click the top or bottom of a 2x2 or 3x3 stack with another RTG to place the whole layer (Shift = single block). Goggles show stack size, voltage, short-circuit current and max power. Taller stacks lower the internal resistance, so they deliver more current. Stack like batteries: 1x1 = 12 V, 2x2 = 120 V, 3x3 = 240 V. Never depletes. (Merged from the old rtgs mod: remove rtgs-1.0.0.jar.) |
| Ultimate Multimeter | Power Grid's multimeter with no voltage/current limit (auto-ranging needle, SI prefixes up to yotta). Recipe: Multimeter + Nether Star. |
| Unbreakable Switch | Looks and fits exactly like the MV Switch but purple. No voltage or current limit: it can't arc over, overheat or burn out, and it is blast/wither/dragon proof. Recipe: MV Switch + Netherite Ingot. |
| Industrial Siren | Very loud looping alarm. Works from ~12 V; the **voltage sets how loud it is**, up to 500 V (three stacked voices, plays on the Master channel). The **pitch / speed** is set with Create's slider (hold right-click on the right-hand side of the body): one slider, 0.5x to 2.0x, 50 = normal; a higher pitch is also a faster wail. **Burns out (block destroyed, no drop) above 500 V.** Recipe: Alarm Bell surrounded by iron plates + a copper coil. |
| Heavy Cable (300 A) | 300 A rating, 48 m max span, very low resistance. |
| Heavy Cable (1000 A) | 1000 A rating, 96 m max span, even lower resistance. |

Transformers: hold right-click on the front panel and drag Create's slider.
Terminals (block facing you, from above): back row = INPUT, front row = OUTPUT, red = +, blue = -.

## Cables
Defined as Power Grid wire types in `data/pgtransformers/powergrid/wire_types/*.json`
(edit maximumCurrent, resistancePerItem, maximumLength, thermalMass ... and reload).
They are added to `powergrid:light_wires`, so every terminal that accepts copper or gold wire accepts them;
heavy connectors, contactors, medium transformers and HV switches accept any wire anyway.
Note: Power Grid does not let two different wire types be joined into one wire segment; connect them at terminals.

Recipes: 300 A = 8 Copper Wire + 1 Copper Ingot (ring) -> 4.  1000 A = 6x 300 A + 2 Gold Ingot + 1 Iron Ingot -> 2.

## Build
1. JDK 21 + Gradle.
2. Put in `libs/`: Create jar, Power Grid jar, and the three jars inside Create's META-INF/jarjar (Registrate, Ponder, Flywheel).
3. `gradle build`, jar in `build/libs/`.

## Unbreakable Switch notes
- Reuses Power Grid's `MvSwitchBlock` (same terminals, shape, facing/rotation/open states, sounds).
- `maxVoltage = infinity` (no overvoltage weld), no thermal behaviour (no overcurrent trip), fixed 0.0001 ohm closed resistance.
- It can still be picked up with a pickaxe. To make it literally unminable like bedrock, change `strength(2.0f, ...)` to `strength(-1.0f, ...)` in `PGTransformers.java`.

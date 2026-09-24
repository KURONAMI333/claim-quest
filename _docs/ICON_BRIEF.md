# Icon design brief — Claim Quest

Per LOGO_PLAYBOOK.md §③: addon-mod icons are produced by the project owner,
not generated. This brief only names the host assets and the intended
two-layer composition; no candidate images were produced.

## Classification

③ addon — two-layer syntax: **contribution (constant)** × **host frame (variable)**.

Host mods: **FTB Quests** (owns the task list this extends) + **FTB Chunks**
(owns the claim data it reads). The product is a bridge between the two;
FTB Quests is the natural frame since that is where the user meets the feature.

## Host assets to sample

Inside the already-cached dev jars — no fetch needed:

- `ftb-quests-neoforge-2101.1.36.jar`
  - `assets/ftbquests/textures/item/book.png` — the quest book; the same
    host anchor mod-074 (Quest Completion Broadcast) used, keeps the FTB
    addon family consistent
  - `assets/ftbquests/textures/gui/chest.png`, `gui/alert.png` — small host
    glyphs if a secondary mark is wanted
- `ftb-chunks-neoforge-2101.1.22.jar`
  - `assets/ftbchunks/textures/checkered.png` — the claimed-land
    checkerboard overlay players see on the map; the most literal
    "claimed chunks" symbol available
  - `assets/ftbchunks/textures/square_border.png` / `square_mask.png` —
    the claim-selection square; clean chunk-grid geometry
  - `assets/ftbchunks/textures/waypoint_default.png` — map pin, only if a
    locator accent is wanted

## Suggested composition

- **Contribution layer (main)**: the claim-grid motif — checkerboard tile or
  the bordered square — since the product is *claimed chunk count*.
- **Host layer (frame)**: the FTB Quests book or a small quest-book corner
  emblem, matching the family's existing FTB addon icons.

## Readability check

The store grid renders ~64 px; the checkerboard must stay visibly a grid
(not noise) and the book silhouette must stay recognisable at that size.

## Output spec

- `src/main/resources/logo.png` in-jar + `branding/claimquest_icon_512/256/128/64.png` exports
  (per LOGO_PLAYBOOK output convention).

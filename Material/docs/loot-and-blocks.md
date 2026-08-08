# 광석·압축 블록 속성 · 루트

> **성격**: 블록 등록·loot 구현 디테일. 채굴 요약(실크터치 / Fortune)은 [`Material/README.md`](../README.md) §채굴·드롭. 명칭·계열 ID는 [`.cursor/rules/material.mdc`](../../.cursor/rules/material.mdc).

## 블록 등록 (`ModBlocks`)

### 광석

`DropExperienceBlock(UniformInt.of(0, 2), …)` — 채굴 시 XP 0~2 (바닐라 coal ore와 동일 대역).

| | 설정 |
|--|------|
| 일반 광석 | `Properties.ofFullCopy(Blocks.COAL_ORE)` |
| 심층 광석 | 일반 광석 복사 + `mapColor(DEEPSLATE)` + `strength(4.5F, 3.0F)` + `SoundType.DEEPSLATE` |

곡괭이 티어 제한 없음 (`#minecraft:mineable/pickaxe`만, README와 동일).

### 압축 연료 블록

`Block` + `Properties.ofFullCopy(Blocks.COAL_BLOCK)`. 블록 아이템은 `useBlockDescriptionPrefix()`.

연료 burn 값은 블록 클래스가 아니라 `ModItems`의 `FuelValueEvents`에 등록한다 → [`fuel-registration.md`](fuel-registration.md).

## Loot tables

경로: `data/material/loot_table/blocks/{id}.json`

### 광석 (`*_ore` / `deepslate_*_ore`)

`minecraft:block` 풀 1회 · `alternatives`:

1. Silk Touch (`match_tool` + enchantment min 1) → **광석 블록** 자체
2. 그 외 → 해당 **연료 아이템** + `apply_bonus` (`fortune`, formula `ore_drops`) + `explosion_decay`

`random_sequence`: `material:blocks/{id}`

실크터치로 캔 광석은 용광로·고로 제련으로 연료 1개를 얻는다 (훈연기 불가). 레시피 레이아웃은 [`tags-recipes-advancements.md`](tags-recipes-advancements.md).

### 압축 블록 (`*_block`)

`survives_explosion` 조건 · 자기 자신 드롭 (`rolls` 1, `bonus_rolls` 0). 바닐라 `coal_block`과 같은 단순 self-drop 패턴.

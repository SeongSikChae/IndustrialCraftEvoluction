# 태그 · 레시피 · Advancement 레이아웃

> **성격**: 데이터팩 레이아웃·커스텀 태그 관례. 사용처 정책·제련 I/O·압축 ×9는 [`.cursor/rules/material.mdc`](../../.cursor/rules/material.mdc). 플레이어용 UI 표는 [`Material/README.md`](../README.md).

## 커스텀 아이템 태그 (`#material:…`)

경로: `data/material/tags/item/`

| 태그 | values (예) | 용도 |
|------|-------------|------|
| `#material:peat` (및 lignite / sub_bituminous / anthracite) | 해당 연료 아이템 1개 | 압축 shaped 키 |
| `#material:peat_blocks` (및 `*_blocks`) | 해당 압축 블록 1개 | 분해 shapeless 재료 |

예: `peat_block.json`은 키 `"P": "#material:peat"`, `peat_from_block.json`은 `"#material:peat_blocks"`.

바닐라 역청탄 블록 제작·분해는 Material이 재선언하지 않는다 (바닐라 유지, 역청탄만).

## 바닐라 태그 확장

경로: `data/minecraft/tags/…`

| 태그 | Material이 추가하는 것 |
|------|------------------------|
| `item/coals.json` | peat, lignite, sub_bituminous, anthracite |
| `item/furnace_minecart_fuel.json` | 위와 동일 4종 (압축 블록 **제외**) |
| `block/coal_ores.json` · `item/coal_ores.json` | Material 광석 8종 |
| `block/mineable/pickaxe.json` | 광석 8종 + 압축 블록 4종 |

## 레시피 파일 규칙

루트: `data/material/recipe/`

| 패턴 | type / category | 예 |
|------|-----------------|-----|
| `{rank}_block.json` | shaped · `building` | 연료 ×9 → 블록 |
| `{rank}_from_block.json` | shapeless · `building` | 블록 → 연료 ×9 |
| `{rank}_from_smelting_{ore}.json` | smelting · `misc` | 일반·`deepslate_` 광석 |
| `{rank}_from_blasting_{ore}.json` | blasting · `misc` | 동일 |

제련 JSON 공통 수치 (바닐라 coal ore와 맞춤):

- `cookingtime`: 200 (고로는 blasting 타입으로 별도, 바닐라와 같이 더 짧음)
- `experience`: 0.1
- `group`: `"{rank}"`

횃불은 네임스페이스 오버라이드:

- `data/minecraft/recipe/torch.json` — `"X": "#minecraft:coals"` (Material 연료가 `#coals`에 들어가므로 해금)

## Advancement (핸드북 해금)

`project-common` §4: 레시피마다 `rewards.recipes` advancement.

| 경로 | 대상 |
|------|------|
| `data/material/advancement/recipes/building/` | 압축·분해 |
| `data/material/advancement/recipes/misc/` | smelting·blasting |

공통 패턴:

- `parent`: `minecraft:recipes/root`
- criteria: `inventory_changed`(관련 아이템) + `recipe_unlocked`
- `requirements`: 두 criteria AND
- `rewards.recipes`: 해당 레시피 ID 1개

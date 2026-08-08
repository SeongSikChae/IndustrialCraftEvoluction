# 혼합 역청탄 광맥 월드젠

> **성격**: 구현 관례 문서. 계열별 weight·비율·“단독 광맥 금지” 정책은 [`.cursor/rules/material.mdc`](../../.cursor/rules/material.mdc) §월드 생성이 우선.

## 접근 방식

Biome modifier로 피처를 추가하지 않는다. 바닐라 configured feature를 **덮어써** 기존 `ore_coal` / `ore_coal_buried` 배치·시도 횟수를 유지한 채, 블록 내용만 혼합한다.

| 파일 | type | size | discard_chance_on_air_exposure |
|------|------|------|--------------------------------|
| `data/minecraft/worldgen/configured_feature/ore_coal.json` | `material:mixed_coal_ore` | 17 | **0.0** |
| `data/minecraft/worldgen/configured_feature/ore_coal_buried.json` | `material:mixed_coal_ore` | 17 | **0.5** |

`ore_peat` 등 rules의 feature 접두는 ID 명명용 서술일 수 있다. 실제 등록 configured feature는 위 두 개뿐이며, 계열 선택은 `pickOre`에서 블록마다 한다.

## Feature 등록

| 항목 | 값 |
|------|-----|
| Feature ID | `material:mixed_coal_ore` |
| 클래스 | `MixedCoalOreFeature` |
| Config | `MixedCoalOreConfiguration(size 0–64, discard_chance 0–1)` |
| 등록 | `ModFeatures.MIXED_COAL_ORE` → `BuiltInRegistries.FEATURE` |

배치 알고리즘은 바닐라 coal ore vein에 가까운 ellipsoidal strip이다. replaceable은 `#stone_ore_replaceables` / `#deepslate_ore_replaceables`이며, 굴린 계열의 일반·심층 변형을 놓는다.

공기·유체에 노출된 면이 있으면 `discard_chance_on_air_exposure` 확률로 그 칸을 버린다 (buried만 0.5).

## `pickOre` 임계값

`WEIGHT_SUM = 1000`. `roll = random.nextInt(1000)`.

| 조건 | 계열 | 일반 / 심층 |
|------|------|-------------|
| `roll < 500` | 이탄 | `peat_ore` / `deepslate_peat_ore` |
| `roll < 600` | 갈탄 | `lignite_ore` / … |
| `roll < 775` | 아역청탄 | `sub_bituminous_ore` / … |
| `roll < 975` | 역청탄 | `Blocks.COAL_ORE` / `DEEPSLATE_COAL_ORE` |
| else | 무연탄 | `anthracite_ore` / … |

비율 해석(50% / 10% / 17.5% / 20% / 2.5%)은 `material.mdc` 표를 본다. GameTest `MixedCoalOreGameTest`는 seed=42, 1만 샘플 ±3%p로 `pickOre`를 검증한다.

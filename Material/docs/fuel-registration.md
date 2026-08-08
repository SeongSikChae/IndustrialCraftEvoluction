# 연료 등록 · 광차 · Machine 연동

> **성격**: 구현 관례 문서. 승인 연소 틱·효율 표는 [`.cursor/rules/material.mdc`](../../.cursor/rules/material.mdc) §연료가 우선.  
> 바닐라 coal/charcoal 대비는 [`vanilla-coal-charcoal.md`](vanilla-coal-charcoal.md).

## 용광로 `FuelValues` 등록

API: Fabric `FuelValueEvents.BUILD` (`ModItems.initialize`).

| 등록 대상 | 상수 / 위치 | 비고 |
|-----------|-------------|------|
| `minecraft:charcoal` | `MaterialMod.CHARCOAL_BURN_TIME` (1440) | 바닐라 1600을 오버라이드 |
| `peat` / `lignite` / `sub_bituminous` / `anthracite` | `ModItems.*_BURN_TIME` | 설계 표와 동일 |
| `*_block` 4종 | `ModItems.*_BLOCK_BURN_TIME` | 낱개 ×10 |
| `minecraft:coal` / `coal_block` | **미등록** | 바닐라 1600 / 16000 유지 |
| 광석·심층 광석 | — | 연료 아님 (`burnDuration == 0`) |

압축 블록은 `#minecraft:furnace_minecart_fuel`에 **넣지 않는다**. 용광로·고로·훈연기에만 태운다.

## 화로 광차 (`MinecartFurnaceMixin`)

바닐라 `MinecartFurnace.addFuel`은 태그 연료마다 고정 3600틱이다. Material은 HEAD inject로 **전부 대체**한다.

1. `#minecraft:furnace_minecart_fuel`이 아니면 `false`
2. `FuelDurations.minecartStyleFuelTicks` = `furnaceBurn × 3600 / 1600`
3. `canAcceptFuel`: `additional > 0` 이고 `current + additional ≤ MAX_FUEL_TICKS`
4. 통과 시 `fuel` 가산·`push` 설정 후 `true`

클래스: `com.industrialcraft.material.power.FuelDurations`

| 상수 | 값 |
|------|-----|
| `VANILLA_MINECART_FUEL_TICKS` | 3600 |
| `VANILLA_COAL_FURNACE_BURN` | 1600 |
| `MAX_FUEL_TICKS` | **32000** (광차 버퍼) |

스케일 함수는 압축 블록 burn에서도 숫자를 낼 수 있으나, 태그 밖이라 광차에는 쓰이지 않는다.

## Machine 엔진 (별도 클래스)

Machine은 Material `FuelDurations`를 **import하지 않는다**. 자체 `com.industrialcraft.machine.power.FuelDurations` 복제본을 쓴다. Material 로드 시 런타임 `FuelValues`(숯 1440 등)를 읽는다.

| | Material (광차) | Machine (엔진) |
|--|-----------------|----------------|
| `MAX_FUEL_TICKS` | 32000 | **40000** (`machine.mdc`) |
| 연료 판별 | `#furnace_minecart_fuel` | ID 화이트리스트 (`isEngineFuel`) |
| 낱개·숯 | minecart 동일식 | 동일 |
| 압축 블록 | 광차 불가 | `engineFuelTicks` = **용광로 burn × 2** |

화이트리스트 경로 (`material` 네임스페이스):  
`peat` / `lignite` / `sub_bituminous` / `anthracite` 및 각 `*_block`.  
추가로 바닐라 `coal` / `charcoal` / `coal_block`.

예: `anthracite_block` 용광로 20000 → 엔진 40000 = cap과 동일 → 버퍼가 비어 있을 때만 1개 수용.

연동 요약: **공유 JAR 의존이 아니라** FuelValues + 엔진 쪽 path 화이트리스트.

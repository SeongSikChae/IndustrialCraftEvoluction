# 바닐라 Coal / Charcoal 프리뷰

> **성격**: 설계 전 브리핑용 **참고 문서**. Cursor 규칙(`.mdc`)이 아니며, 승인 설계는 [`.cursor/rules/material.mdc`](../../.cursor/rules/material.mdc)를 본다.  
> **기준**: Java Edition (프로젝트 `minecraft_version=26.2`).  
> **범위**: `minecraft:coal` / `charcoal` / coal ore·block, 용광로·고로·훈연기·화로 광차, 바닐라 용광로 연료 개요.

## 핵심 사실

| | `minecraft:coal` | `minecraft:charcoal` |
|---|---|---|
| Material 명칭 | **역청탄** | **숯** |
| 바닐라 용광로 연소 | 1600틱 | 1600틱 (Material은 **1440틱**으로 오버라이드) |
| `#minecraft:coals` | ✓ | ✓ |
| `#minecraft:furnace_minecart_fuel` | ✓ | ✓ |
| 압축 블록 | `coal_block` (×9) | ✗ |

- 용광로 제련 1회 = 200틱. 고로·훈연기는 2배 빠르고 연료도 2배 소모 → ops/연료 동일.
- 화로 광차: 태그 연료만, 1개당 바닐라 3600틱(coal 기준). Material은 furnace burn × 3600/1600.
- 역청탄 블록(`coal_block`) 연료: 16000틱. 화로 광차 불가.

자세한 바닐라 연료 전체 표·장치 용도는 설계 시 위키 [Smelting § Fuel](https://minecraft.wiki/w/Smelting#Fuel)을 대조한다. Material 목표 수치는 `material.mdc`가 우선한다.

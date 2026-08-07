# Showcase

IndustrialCraft: Evolution 문서용 **쇼케이스 클립 / 시나리오** 캡처 툴링 모듈입니다. 플레이어용 모드가 아니며, Fabric Client GameTest로 프레임을 찍고 ffmpeg로 mp4를 만듭니다.

## 범위

- **의존**: `Material`만 (`implementation project(':Material')`). Machine을 로드·수정하지 않습니다.
- **실행**: `:Showcase:` / `:Material:` 태스크만 쓰세요. Machine Client GameTest와 **동시에** 돌리면 Loom 캐시 락이 충돌할 수 있습니다.
- 배포 JAR 수집(`collectModJars`)에서 Showcase는 제외됩니다.

## 실행

### 화로 연료 타임랩스 (현재 entrypoint)

이탄→무연탄 각 **연료 1 + 철광석 64** 를 바닐라 **화로(Furnace)** 에 넣고, **화로 GUI**를 연 채로 월드 시간 **5배속** 한 클립에 담습니다 (총 **60초** @10fps).

```bash
gradlew.bat :Showcase:recordFurnaceFuelShowcase
```

| 등급 | 실제 연소 | 영상 구간 | 예상 철괴 (화로 200틱/개) |
|------|-----------|-----------|---------------------------|
| 이탄 | 20초 (400틱) | 4초 | 2 |
| 갈탄 | 40초 | 8초 | 4 |
| 아역청탄 | 60초 | 12초 | 6 |
| 역청탄 | 80초 | 16초 | 8 |
| 무연탄 | 100초 | 20초 | 10 |

- 출력: `docs/clips/furnace_fuels/furnace_fuels.mp4`
- 가속: 프레임마다 게임 틱 10개 (`TIMELAPSE_TICKS_PER_FRAME=10`) → 10fps면 ×5.

참고: 바닐라 **용광로(Blast Furnace)** 도 석탄·숯·이탄 등 동일 연료를 씁니다. 광석만 2배 빠르게 녹이고 연료도 2배 써서 **개수/연료는 화로와 같습니다**.

### 석탄 등급 아이템/광석 클립

entrypoint를 `CoalGradeShowcaseClipTest` 또는 `PeatShowcaseClipTest`로 바꾼 뒤:

```bash
gradlew.bat :Showcase:recordCoalGradeShowcase
```

프레임만 / 인코딩만:

```bash
gradlew.bat :Showcase:runClientGameTest
gradlew.bat :Showcase:encodeShowcaseClips
```

- 출력: `docs/clips/<clipId>/<clipId>.mp4`
- GameTest 원본 프레임: `build/run/clientGameTest/docs/clips/<clipId>/`
- ffmpeg는 `_tools/ffmpeg` 또는 PATH. 인코딩 후 `frame_*.png` 자동 삭제.

## 클립 스펙

| clipId | 아이템 | 광석 | 심층 광석 | 연료(화로) |
|--------|--------|------|-----------|--------------|
| peat | 이탄 | 이탄 광석 | 심층암 이탄 광석 | 400틱 |
| lignite | 갈탄 | 갈탄 광석 | 심층암 갈탄 광석 | 800틱 |
| sub_bituminous | 아역청탄 | 아역청탄 광석 | 심층암 아역청탄 광석 | 1200틱 |
| bituminous | 역청탄 (`minecraft:coal`) | 역청탄 광석 | 심층 역청탄 광석 | 1600틱 |
| anthracite | 무연탄 | 무연탄 광석 | 심층암 무연탄 광석 | 2000틱 |
| furnace_fuels | 5등급 합본 | — | — | 화로 GUI 5× 타임랩스 60초 |

공통(아이템 클립): 3 × 8초 ≈ **24초**, 10 fps, 1280×720.

## 전시 규칙

- **블록**: 월드에 설치한 채 촬영.
- **일반 아이템**: 바닥 `ItemEntity` 드롭(자연 회전·떠다님). `ItemDrops.dropOnFloor`.

## 새 클립 추가

1. `FuelGradeSpec` / `FuelGradeSpecs`에 등급 추가, 또는 `ShowcaseClip` 구현.
2. `FuelGradeShowcaseClip` / `FurnaceFuelShowcaseClip` + `FabricClientGameTest`로 기록.
3. `fabric.mod.json`의 `fabric-client-gametest`에 등록.
4. `build.gradle`의 `fuelClipCaptions` 또는 `furnaceFuelCaptions`에 한글 자막 추가.

공통 헬퍼: `ShowcaseWorlds`, `Camera`, `FrameCapture`, `ShowcaseClip`, `ItemDrops`, `FuelGradeShowcaseClip`, `FurnaceFuelShowcaseClip`.

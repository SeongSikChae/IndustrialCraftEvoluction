# Showcase

IndustrialCraft: Evolution 문서용 **쇼케이스 클립 / 시나리오** 캡처 툴링 모듈입니다. 플레이어용 모드가 아니며, Fabric Client GameTest로 프레임을 찍고 ffmpeg로 mp4를 만듭니다.

## 범위

- **의존**: `Material`만 (`implementation project(':Material')`). Machine을 로드·수정하지 않습니다.
- **실행**: `:Showcase:` / `:Material:` 태스크만 쓰세요. Machine Client GameTest와 **동시에** 돌리면 Loom 캐시 락이 충돌할 수 있습니다.
- 배포 JAR 수집(`collectModJars`)에서 Showcase는 제외됩니다.

## 실행

갈탄·아역청탄·역청탄·무연탄 (현재 entrypoint):

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
- 자막·HUD·고정 앵글 규칙은 peat 때와 동일합니다.

이탄만 다시 찍으려면 `src/gametest/resources/fabric.mod.json` entrypoint를 `PeatShowcaseClipTest`로 바꾼 뒤 `runClientGameTest` + `encodeShowcaseClips`를 실행하세요.

## 클립 스펙

| clipId | 아이템 | 광석 | 심층 광석 | 연료(용광로) |
|--------|--------|------|-----------|--------------|
| peat | 이탄 | 이탄 광석 | 심층암 이탄 광석 | 400틱 |
| lignite | 갈탄 | 갈탄 광석 | 심층암 갈탄 광석 | 800틱 |
| sub_bituminous | 아역청탄 | 아역청탄 광석 | 심층암 아역청탄 광석 | 1200틱 |
| bituminous | 역청탄 (`minecraft:coal`) | 역청탄 광석 | 심층 역청탄 광석 | 1600틱 |
| anthracite | 무연탄 | 무연탄 광석 | 심층암 무연탄 광석 | 2000틱 |

공통: 3 × 8초 ≈ **24초**, 10 fps, 1280×720.

## 전시 규칙

- **블록**: 월드에 설치한 채 촬영.
- **일반 아이템**: 바닥 `ItemEntity` 드롭(자연 회전·떠다님). `ItemDrops.dropOnFloor`.

## 새 클립 추가

1. `FuelGradeSpec` / `FuelGradeSpecs`에 등급 추가, 또는 `ShowcaseClip` 구현.
2. `FuelGradeShowcaseClip` + `FabricClientGameTest`로 기록.
3. `fabric.mod.json`의 `fabric-client-gametest`에 등록.
4. `build.gradle`의 `fuelClipCaptions`에 한글 자막 3구간 추가.

공통 헬퍼: `ShowcaseWorlds`, `Camera`, `FrameCapture`, `ShowcaseClip`, `ItemDrops`, `FuelGradeShowcaseClip`.

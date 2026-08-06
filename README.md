# IndustrialCraftEvolution
Fabric 기반 Minecraft 산업 모드(IndustrialCraft: Evolution)

멀티 모듈로 구성되며, 각 모듈은 단독 JAR로 빌드·실행할 수 있습니다.

## 모듈

### [Material](Material/README.md)

연료·광물 모듈입니다. 바닐라 석탄을 **역청탄**으로 재해석하고, 이탄·갈탄·아역청탄·무연탄 등 등급별 석탄류와 광석을 추가합니다. 용광로 연소 시간과 연료 태그를 Machine 등 다른 모듈에서 그대로 활용할 수 있습니다.

### [Machine](Machine/README.md)

기계 모듈입니다. 연료로 **회전 동력**(토크·각속도)을 내는 화로 엔진(선택 **조속기 부속**으로 출력 1~100%), 동력을 1:1로 중계하는 Dynamo, Machine 전용 **기계 제작대**, 그리고 **리저버·유체 파이프**에 유압(PU)·유속 기반 유체 저장/이송을 제공합니다. Material이 있으면 등급별 연료를 쓰고, 없어도 바닐라 석탄·숯으로 동작합니다.

## Build

### 요구 사항

- JDK 25
- Minecraft `26.2` / Fabric Loader `0.19.3` / Fabric API `0.156.0+26.2` (`gradle.properties` 기준)

### 빌드

프로젝트 루트에서 Gradle Wrapper로 전체 모듈을 빌드합니다.

```bash
# Windows
gradlew.bat build

# Linux / macOS
./gradlew build
```

빌드가 성공하면 각 모듈의 JAR가 다음 경로에 생성됩니다.

- `Material/build/libs/industrialcraft-evolution-material-<version>.jar`
- `Machine/build/libs/industrialcraft-evolution-machine-<version>.jar`

특정 모듈만 빌드하려면:

```bash
gradlew.bat :Material:build
gradlew.bat :Machine:build
```

### 개발 실행

클라이언트를 바로 실행해 테스트할 수 있습니다.

```bash
gradlew.bat :Material:runClient
gradlew.bat :Machine:runClient
```

# IndustrialCraftEvoluction
Fabric 기반 Minecraft 산업 모드(IndustrialCraft: Evoluction)

## Build

### 요구 사항

- JDK 25
- Minecraft `26.2` / Fabric Loader / Fabric API (버전은 `gradle.properties` 참고)

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

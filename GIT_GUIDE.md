# 스탠다리아 GitHub 저장소 관리 안내

저장소: [seongkeun316-star/regula-guild](https://github.com/seongkeun316-star/regula-guild) · **공개(Public)** · 기본 브랜치 `main`.

현재 `main`의 앱 소스는 **0.1.4**입니다. GitHub Releases의 최신 **첨부 APK는 0.1.2**로, Play Console 내부·비공개 테스트에 사용한 빌드와 구분해야 합니다. APK를 새로 올리거나 릴리스 태그를 발행하지 않은 상태에서 배포 버전이 업데이트됐다고 안내하지 마세요.

## 소스 내려받기

```bash
git clone https://github.com/seongkeun316-star/regula-guild.git
cd regula-guild
git status
git log --oneline
```

GitHub Desktop에서는 **File → Clone repository**로 같은 저장소를 복제할 수 있습니다. 이미 로컬 복제본이 있다면 `git status`로 수정사항을 확인한 뒤 `git pull --ff-only origin main`으로 최신 내역을 가져옵니다. 변경 충돌이 있을 때는 내용을 확인해 해결하고 `--force`로 덮어쓰지 마세요.

## 이후 작업 원칙

1. 기능 수정은 작업 브랜치에서 진행하고, 테스트 후 변경사항을 기록합니다.
2. 버전을 올릴 때는 `app/build.gradle`의 `versionCode`/`versionName`, `app/src/main/AndroidManifest.xml`, 빌드 출력 파일명, 앱 화면의 버전 표기 및 `CHANGELOG.md`를 함께 확인합니다.
3. `main` 소스 업데이트와 GitHub Releases의 **APK 첨부**는 다른 작업입니다. 실제 테스트한 APK만 올리고 버전·서명·출처를 명시합니다.
4. Google Play Console의 **테스트 트랙**과 **일반 공개 출시**는 구분하여 안내합니다.
5. 결함은 GitHub Issues에 기기·Android 버전·재현 단계·예상 동작과 실제 동작을 기록합니다. 화면 캡처의 개인정보를 확인하세요.

예시:

```bash
git switch -c fix/menu-layout
# 변경 및 검증 후
git add app/src/main/java/kr/cau/regulaguild/MainActivity.java
git commit -m "fix: improve Android safe area layout"
```

## APK·AAB 및 서명키 보안

- **공개 저장소에 올려도 되는 것:** 소스코드, 개발 문서, 라이선스 고지, 테스트 코드(민감정보 제외).
- **공개 저장소에 올리지 않을 것:** `*.jks`, `*.keystore`, `*.p12`, `.env`, 비밀번호, Google Play **업로드 키·개인키**, 개인 설정 및 게임 백업.
- `.gitignore`에 빌드 출력, 서명키, 로컬 설정 파일 제외 규칙이 있습니다. 단, 과거에 커밋한 파일을 `.gitignore`만으로 숨길 수는 없습니다.
- `build-apk.sh`는 **로컬 테스트용 개발키**를 사용합니다. 이 키는 Play Console 업로드/배포 서명키와 다르며, 스토어 제출용 비밀키로 사용하지 마세요.
- 기존 APK를 직접 설치한 경우 업데이트 설치에는 동일 패키지·호환되는 서명키가 필요합니다. Play 배포 버전과 개발 APK 사이에 서명 충돌이 있을 수 있습니다.
- `Standaria-upload-key-PRIVATE.zip` 등 서명 관련 파일은 로컬의 안전한 저장소에서만 관리하세요. GitHub Issues나 Releases에도 첨부하지 마세요.

## 관련 자료

- 프로젝트 소개: [README.md](README.md)
- 버전 기록: [CHANGELOG.md](CHANGELOG.md)
- 사용 라이선스: [LICENSE](LICENSE)
- 릴리스 페이지: [Releases](https://github.com/seongkeun316-star/regula-guild/releases)

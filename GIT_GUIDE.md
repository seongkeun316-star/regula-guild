# 규제 길드 Git 관리 안내

현재 소스를 `main` 브랜치의 첫 커밋에 기록하고 `v0.1.1` 태그를 붙였습니다. 이 파일에 설명된 원격 저장소 연결은 아직 실행하지 않았습니다.

## Git과 GitHub

Git은 변경 이력을 기록하는 도구입니다. GitHub는 그 저장소를 온라인에 보관하고 공동 작업하는 서비스입니다. 커밋·태그는 로컬에 준비되어 있으며, 원격 저장소 주소를 결정하면 업로드할 수 있습니다.

## PC에서 시작

제공된 Git 준비 ZIP을 풀면 `RegulaGuild` 폴더 안에 Git 저장소가 들어 있습니다. 숨김 폴더인 `.git`을 함께 유지하십시오. GitHub Desktop에서 ‘Add Local Repository’로 해당 폴더를 선택하거나, 터미널에서 그 폴더로 이동합니다.

```bash
git status
git log --oneline
git tag
```

## GitHub 원격 연결

GitHub에서 빈 저장소를 생성합니다. 예시 이름은 `regula-guild`이며 초기 검토 단계의 접근 범위는 비공개로 둘 수 있습니다. 이 소스에 README와 LICENSE가 이미 있으므로 GitHub 저장소 생성 시 별도 초기 파일을 추가하지 않으면 초기 연결이 간단합니다.

생성된 실제 저장소의 HTTPS 주소로 아래 예시 주소를 교체합니다. 이 명령은 사용자가 저장소를 만든 뒤 실행하는 안내이며 원격 업로드가 완료된 상태를 뜻하지 않습니다.

```bash
git remote add origin https://github.com/OWNER/regula-guild.git
git push -u origin main
git push origin v0.1.1
```

기존 원격에 코드가 있다면 먼저 내용을 확인하고 병합해야 합니다. `--force`로 덮어쓰지 마십시오.

## 이후 개선 작업

```bash
git switch -c fix/menu-layout
# 요청된 변경을 적용하고 확인한 다음
git add app/src/main/java/kr/cau/regulaguild/MainActivity.java
git commit -m "fix: adjust Android safe-area layout"
```

한 번에 한 주제씩 변경하고 커밋에 변경 이유를 남깁니다. 새 버전에서는 AndroidManifest.xml과 app/build.gradle의 versionCode·versionName, 앱에 표시하는 버전, 빌드 출력 파일명과 CHANGELOG를 함께 갱신합니다.

## APK와 서명 키

- Git에는 게임 소스, 시나리오, 오픈소스 라이브러리/폰트 및 라이선스, 빌드 설정, 문서와 테스트를 기록합니다.
- APK·컴파일 결과물·서명 키·개인 설정·로컬 백업은 제외합니다.
- APK는 GitHub Releases의 버전별 첨부 파일로 배포할 수 있습니다. 처음에는 수동 빌드·서명·배포로 관리하면 됩니다. 자동 빌드나 자동 공개 배포는 설정하지 않았습니다.
- 기존 설치 앱을 업데이트하려면 이전과 같은 서명 키가 필요합니다. 기존 0.1.1 소스 배포 ZIP의 `build/development.jks`를 별도 보관하고, 이 저장소에서는 커밋하지 마십시오.
- 이 Git 준비 ZIP에는 서명 키가 없습니다. 빌드 스크립트를 처음 실행하면 새 개발 키를 만들 수 있으므로, 기존 설치 앱을 업데이트할 목적이라면 먼저 기존 키를 로컬 `build/development.jks`에 복원해야 합니다. 다른 키로 만든 APK는 기존 앱 위에 업데이트 설치되지 않습니다.

학습 기록과 사용자의 게임 백업 JSON은 저장소에 올리지 않습니다. 이미 제공된 개발 서명 키는 테스트 호환용이며 상용 배포용 비밀 키와 구별합니다.

## 오류와 개선 요청 기록

GitHub Issues에 발생 화면, 앱 버전, Android 버전, 재현 단계, 예상 동작을 남기면 이후 수정의 근거를 관리하기 편합니다. 스크린샷에는 불필요한 개인정보가 들어가지 않도록 확인하십시오.

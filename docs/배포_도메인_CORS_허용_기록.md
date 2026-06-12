# 배포 도메인 CORS 허용 기록

## 작업 목적

Cloudflare와 EC2 배포 환경에서 고객용 앱의 데모 로그인 요청이 백엔드로 전달될 때 `403` 응답이 발생했다.

Nginx access log에서 아래 요청이 확인되었다.

```text
POST /api/auth/demo-login HTTP/1.1 403
```

요청은 EC2까지 도착했으나, Spring 백엔드 CORS 설정에 배포 도메인이 포함되어 있지 않아 차단된 것으로 판단했다.

## 적용 내용

`CorsConfig.java`의 CORS 허용 출처에 배포 도메인을 추가했다.

```text
https://swapit.my
https://www.swapit.my
https://crew.swapit.my
```

## 수정 파일

```text
src/main/java/com/swapit/config/CorsConfig.java
```

## 검증

로컬에서 백엔드 빌드를 실행해 성공을 확인했다.

```bash
./gradlew.bat clean build -x test
```

## 배포 서버 반영 방법

EC2에서 최신 코드를 받은 뒤 백엔드 서버를 다시 빌드하고 PM2 프로세스를 재시작한다.

```bash
cd ~/swapit/beyond404-server
git pull
./gradlew clean build -x test
pm2 restart swapit-server --update-env
```

## 확인 명령

```bash
curl -i -X POST https://swapit.my/api/auth/demo-login \
  -H "Content-Type: application/json" \
  -H "Origin: https://swapit.my" \
  -d '{"userName":"Domain Test","phoneNumber":"010-3333-4444"}'
```

정상이라면 `403`이 아니라 `200` 응답과 사용자 정보 JSON이 반환되어야 한다.

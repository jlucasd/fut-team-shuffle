# App iOS (iPhone) + notificações de aniversário

O projeto iOS está em `frontend/ios/` (Capacitor + Swift Package Manager). Ele usa o mesmo
frontend React e o mesmo push do Firebase do Android. No iPhone, o Firebase entrega as
notificações pelo serviço da Apple (APNs).

## O que é obrigatório (regras da Apple)

| Item | Por quê | Custo |
|---|---|---|
| **Apple Developer Program** | Sem ele o iPhone não recebe push, e apps instalados com conta grátis expiram em 7 dias | US$ 99/ano |
| **Um Mac com Xcode** *ou* o workflow do GitHub Actions deste repositório | Apps iOS só compilam no macOS | grátis (Actions usa os minutos de macOS do GitHub) |

Como você está no Linux, o caminho recomendado é o **GitHub Actions → TestFlight** (seção 3).
O app chega ao seu iPhone pelo app **TestFlight**, sem publicar na App Store.

## 1. Apple Developer (uma vez)

1. Assine em <https://developer.apple.com/programs/enroll/> (pessoa física serve). A aprovação
   pode levar de algumas horas a 2 dias.
2. Anote o seu **Team ID**: developer.apple.com → Account → Membership details.
3. **Registrar o App ID**: Certificates, IDs & Profiles → Identifiers → **+** → App IDs → App
   - Bundle ID (explícito): `com.gavioesfc.sorteio`
   - Marque **Push Notifications**.
4. **Chave APNs**: Keys → **+** → marque *Apple Push Notifications service (APNs)* → baixe o
   `.p8` (só dá para baixar uma vez) e anote o **Key ID**.
5. **App no App Store Connect**: <https://appstoreconnect.apple.com> → Apps → **+** → New App
   → iOS, nome "Gaviões FC" (se o nome já existir, use outro, ex. "Gaviões FC Sorteio"),
   Bundle ID `com.gavioesfc.sorteio`, SKU qualquer (ex. `gavioesfc`).
6. **Chave da API do App Store Connect** (usada pelo GitHub Actions): Users and Access →
   Integrations → App Store Connect API → **+**, com acesso **Admin**. Baixe o `.p8` e anote
   o **Key ID** e o **Issuer ID**.

## 2. Firebase (mesmo projeto do Android)

1. No console do Firebase → **Adicionar app → iOS**, Bundle ID `com.gavioesfc.sorteio`.
2. Baixe o **`GoogleService-Info.plist`** (está no `.gitignore`).
3. Configurações do projeto → **Cloud Messaging** → *Configuração do app Apple* → **Chave de
   autenticação de APNs** → envie o `.p8` do passo 1.4 com o Key ID e o Team ID.

## 3. Compilar e instalar sem Mac: GitHub Actions → TestFlight

No GitHub: repositório → Settings → Secrets and variables → Actions → **New repository secret**:

| Secret | Valor |
|---|---|
| `APPLE_TEAM_ID` | Team ID (passo 1.2) |
| `ASC_KEY_ID` | Key ID da chave da API (passo 1.6) |
| `ASC_ISSUER_ID` | Issuer ID (passo 1.6) |
| `ASC_KEY_P8` | `.p8` da chave da API em base64: `base64 -w0 AuthKey_XXXX.p8` |
| `GOOGLE_SERVICE_INFO_PLIST` | `base64 -w0 GoogleService-Info.plist` |
| `VITE_API_BASE_URL` | URL do backend, ex. `https://SEU-BACKEND.onrender.com` |

Depois disso, faça push do código para o GitHub e vá em **Actions → "iOS → TestFlight" → Run workflow**. Em cerca de 15 minutos o
build aparece no App Store Connect → TestFlight (a Apple leva mais alguns minutos processando).

No iPhone:
1. Instale o app **TestFlight** pela App Store.
2. No App Store Connect → TestFlight → *Internal Testing* → crie um grupo e adicione o seu
   e-mail da Apple. Você recebe um convite no TestFlight → **Instalar**.
3. Builds do TestFlight valem 90 dias; para atualizar, rode o workflow de novo.

## 3b. Alternativa: compilar num Mac

```bash
cd frontend
VITE_API_BASE_URL=https://SEU-BACKEND.onrender.com npm run build
npx cap sync ios
npx cap open ios
```

Copie o `GoogleService-Info.plist` para `frontend/ios/App/App/`. No Xcode: target **App** →
*Signing & Capabilities* → escolha o seu Team. Com o iPhone conectado por cabo, clique em ▶ Run.
Para gerar um build do TestFlight: Product → Archive → Distribute App.

## 4. Backend

Acrescente a origem do app iOS ao CORS no Render:

```
CORS_ORIGINS=https://SEU-FRONT.vercel.app,https://localhost,capacitor://localhost
```

(`https://localhost` é o Android, `capacitor://localhost` é o iOS.) As demais variáveis
(`FIREBASE_CREDENTIALS`, `CRON_SECRET`) são as mesmas do Android, veja
[APP_ANDROID_FIREBASE.md](APP_ANDROID_FIREBASE.md).

## 5. Ativar no iPhone

Abra o app → login → **Aniversariantes → Ativar notificações** → Permitir → **Testar**.

## Alternativa sem custo: instalar como site (PWA)

Desde o iOS 16.4, um site adicionado à Tela de Início também pode receber push, sem Mac e sem
os US$ 99/ano. Isso ainda não está implementado aqui: precisaria de um manifest, de um service
worker e do Firebase Web Push no frontend. É uma boa opção se o objetivo for só receber os
avisos no seu celular.

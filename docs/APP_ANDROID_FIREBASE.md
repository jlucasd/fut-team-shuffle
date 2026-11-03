# App Android (APK) + notificações de aniversário via Firebase

> iPhone: veja [APP_IOS.md](APP_IOS.md).

O app Android é o mesmo frontend React empacotado com [Capacitor](https://capacitorjs.com).
O backend envia, **todo dia às 9h (horário de Brasília)**, um push com quem faz aniversário
**hoje** e **amanhã** (de todos os times) para todos os celulares que ativaram as notificações,
mesmo com o app fechado.

```
Celular (APK) ──registra token FCM──▶ Backend (/api/notificacoes/dispositivos)
Backend (job diário 9h ou cron externo) ──Firebase Admin SDK──▶ FCM ──▶ Celular
```

Sem as credenciais do Firebase tudo continua funcionando; só o push fica desligado.

## 1. Criar o projeto no Firebase (uma vez)

1. Acesse <https://console.firebase.google.com> → **Adicionar projeto** (Analytics é opcional).
2. **Adicionar app → Android**, com o nome do pacote **`com.gavioesfc.sorteio`**
   (precisa bater com `appId` em `frontend/capacitor.config.ts`).
3. Baixe o **`google-services.json`** e coloque em `frontend/android/app/google-services.json`
   (ele está no `.gitignore`).
4. Em **Configurações do projeto → Contas de serviço → Gerar nova chave privada**, baixe o JSON
   da conta de serviço. **Esse arquivo é secreto**: não comite.

## 2. Configurar o backend (Render)

Variáveis de ambiente novas:

| Variável | Valor |
|---|---|
| `FIREBASE_CREDENTIALS` | Conteúdo do JSON da conta de serviço (pode ser em base64: `base64 -w0 chave.json`) |
| `CRON_SECRET` | Uma string aleatória longa (ex.: `openssl rand -hex 32`) |
| `CORS_ORIGINS` | Acrescente `https://localhost` (app Android) e `capacitor://localhost` (app iOS), separados por vírgula do domínio da Vercel |
| `BIRTHDAY_CRON` | Opcional. Padrão `0 0 9 * * *` (9h) |

No log de inicialização deve aparecer `Firebase inicializado: notificações push habilitadas.`

### Render free tier dorme → keep-alive no GitHub Actions

O workflow [`.github/workflows/keepalive.yml`](../.github/workflows/keepalive.yml) pinga
`/api/health` a cada 5 minutos, então o backend não hiberna mais, e às 09:05 chama o disparo
diário de aniversários. Para esse disparo funcionar, cadastre o mesmo `CRON_SECRET` do Render
como secret do repositório no GitHub (Settings → Secrets and variables → Actions). Sem o secret,
o passo é pulado e vale o job interno das 9h, que funciona porque o servidor fica acordado.

O disparo também pode ser feito manualmente:

```bash
curl -X POST https://fut-team-shuffle.onrender.com/api/notificacoes/aniversarios/disparar -H "X-Cron-Secret: SEU_CRON_SECRET"
```

O envio é idempotente: cada dia é notificado **uma única vez**. A resposta mostra o resultado
(`ENVIADO`, `JA_ENVIADO`, `SEM_ANIVERSARIANTES`, `SEM_DISPOSITIVOS` ou
`FIREBASE_NAO_CONFIGURADO`).

## 3. Gerar o APK

Pré-requisitos: [Android Studio](https://developer.android.com/studio) (traz o SDK e o JDK).

```bash
cd frontend
VITE_API_BASE_URL=https://SEU-BACKEND.onrender.com npm run android:sync
npm run android:open
```

No Android Studio: **Build → Build App Bundle(s) / APK(s) → Build APK(s)**.
O APK de debug sai em `frontend/android/app/build/outputs/apk/debug/app-debug.apk` e pode ser
instalado direto no celular (permitir "fontes desconhecidas").

Para a Play Store, use **Build → Generate Signed App Bundle** e guarde a keystore com cuidado.

Sempre que mudar o código do frontend, rode `npm run android:sync` de novo antes de gerar o APK.

## 4. Ativar no celular

1. Abra o app, faça login.
2. Vá em **Aniversariantes → Ativar notificações** e permita.
3. Toque em **Testar**: deve chegar "As notificações de aniversário estão funcionando!".

Tocar numa notificação abre a página de aniversariantes.

## Endpoints

| Método | Rota | Auth | Uso |
|---|---|---|---|
| `POST` | `/api/notificacoes/dispositivos` | JWT | O app registra o token FCM |
| `DELETE` | `/api/notificacoes/dispositivos` | JWT | Remove um token |
| `GET` | `/api/notificacoes/status` | JWT | Firebase configurado? quantos celulares? |
| `POST` | `/api/notificacoes/teste` | JWT | Push de teste para todos os celulares |
| `POST` | `/api/notificacoes/aniversarios/disparar` | `X-Cron-Secret` | Disparo diário (cron externo) |

Tokens inválidos (app desinstalado) são removidos automaticamente no envio.

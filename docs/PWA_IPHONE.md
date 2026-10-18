# iPhone sem App Store: instalar o site como app (PWA) com push

Desde o **iOS 16.4**, um site adicionado à **Tela de Início** abre como um app (sem a barra do
Safari) e recebe notificações push. Não precisa de Mac, de conta paga da Apple nem de TestFlight.
O push diário de aniversários (9h, "hoje" e "amanhã") é o mesmo do Android e do iOS nativo.

```
iPhone (PWA na Tela de Início) ──token Web Push──▶ Backend ──Firebase──▶ Apple Web Push ──▶ iPhone
```

Também funciona no Chrome/Edge/Firefox do computador e no Android pelo navegador.

## 1. Firebase: adicionar um app Web (uma vez)

Use o mesmo projeto Firebase do Android/iOS.

1. Console do Firebase → **Configurações do projeto → Geral → Seus apps → Adicionar app → Web
   (`</>`)**. Dê um nome (ex. "Gaviões FC Web"); não precisa de Hosting.
2. O Firebase mostra um objeto `firebaseConfig`. Anote `apiKey`, `projectId`,
   `messagingSenderId` e `appId` (são valores públicos, podem ir no frontend).
3. **Configurações do projeto → Cloud Messaging → Configuração da Web → Certificados push da
   Web → Gerar par de chaves**. Copie a **chave pública** (é a "VAPID key").

## 2. Vercel (frontend)

Project → Settings → **Environment Variables** (ambiente *Production*):

| Variável | Valor |
|---|---|
| `VITE_FIREBASE_API_KEY` | `apiKey` |
| `VITE_FIREBASE_PROJECT_ID` | `projectId` |
| `VITE_FIREBASE_MESSAGING_SENDER_ID` | `messagingSenderId` |
| `VITE_FIREBASE_APP_ID` | `appId` |
| `VITE_FIREBASE_VAPID_KEY` | chave pública do passo 1.3 |

Depois disso, faça um **Redeploy** (as variáveis `VITE_*` entram no build).

Sem essas variáveis o site continua com o lembrete antigo do navegador, que só funciona com a
aba aberta.

## 3. Render (backend)

Precisa do mesmo `FIREBASE_CREDENTIALS` do guia do Android
([APP_ANDROID_FIREBASE.md](APP_ANDROID_FIREBASE.md)), e o `CORS_ORIGINS` precisa conter o
domínio da Vercel (já contém, se o site funciona hoje). Para o push sair às 9h mesmo com o
Render dormindo, configure o cron externo descrito no mesmo guia.

## 4. Instalar no iPhone

1. Abra o site **no Safari** (precisa ser o Safari) e faça login.
2. Toque em **Compartilhar** (o quadrado com a seta para cima, na barra de baixo).
3. Role e toque em **Adicionar à Tela de Início** → **Adicionar**.
4. **Feche o Safari e abra o app pelo ícone "Gaviões FC"** na Tela de Início. Isso é
   obrigatório: o iPhone só libera push para o site aberto pelo ícone.
5. Faça login de novo (o app da Tela de Início não compartilha o login com o Safari).
6. Vá em **Aniversariantes → Ativar notificações → Permitir**.
7. Toque em **Testar**. Deve chegar "As notificações de aniversário estão funcionando!".

Se no passo 6 aparecer o quadro "Receber avisos no iPhone", é porque o site foi aberto pelo
Safari e não pelo ícone; volte ao passo 4.

## Problemas comuns

| Sintoma | Solução |
|---|---|
| Não aparece "Adicionar à Tela de Início" | Use o Safari (não Chrome/Instagram/WhatsApp); no WhatsApp, abra o link "no Safari" |
| "Este navegador não suporta notificações push" | Atualize o iPhone para iOS 16.4 ou superior |
| "Notificações bloqueadas" | Ajustes do iPhone → Notificações → Gaviões FC → Permitir Notificações |
| "Testar" diz "Firebase não configurado no servidor" | Falta `FIREBASE_CREDENTIALS` no Render |
| Teste chega, mas o aviso das 9h não | O Render estava dormindo: configure o cron externo |
| Parou de receber depois de muito tempo sem abrir | Abra o app pelo ícone uma vez (o token é renovado ao abrir) |

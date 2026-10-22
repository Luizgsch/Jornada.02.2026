# Frontend CASH Mobile

Next.js com exportacao estatica e Capacitor Android. Veja `../README.md` para arquitetura, requisitos e operacao.

- `npm run dev`: navegador com atualizacao automatica.
- `npm run android:live`: servidor de desenvolvimento para atualizacao ao vivo no emulador. Executar app/debug no Android Studio uma vez e manter o terminal aberto.
- `npm run build`: arquivos estaticos em `out/`.
- `npm run android:sync:dev`: assets para demonstracao no emulador, API em `10.0.2.2:8080`.
- `npm run android:open`: abrir projeto no Android Studio.
- `npm run android:sync`: exige `ANDROID_API_URL` HTTPS para API real.

Nao usar `next start`: este projeto nao usa servidor Next em producao. Login e regras ficarao no Spring. Ainda nao ha telas financeiras nem login funcional.

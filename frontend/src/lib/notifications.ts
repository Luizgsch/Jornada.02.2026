export type NotificationCategory = "financeira" | "sistema";

export const demoNotifications = [
  { id: "aluguel", category: "financeira", title: "Uma conta vence amanhã", time: "22/10/2026", description: "Exemplo com referência em 22/10/2026: aluguel vence em 23/10/2026.", unread: true },
  { id: "pagamento", category: "financeira", title: "Pagamento realizado", time: "15/10/2026", description: "Pagamento fictício do Software de gestão em 15/10/2026.", unread: true },
  { id: "movimentacao", category: "sistema", title: "Nova movimentação registrada", time: "21/10/2026", description: "Alerta fictício. Nenhuma movimentação é criada por este aviso.", unread: false },
  { id: "documento", category: "sistema", title: "Documento anexado", time: "20/10/2026", description: "Alerta fictício. Nenhum arquivo foi criado por este aviso.", unread: false },
] as const;

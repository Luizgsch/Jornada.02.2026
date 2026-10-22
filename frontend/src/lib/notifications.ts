export type NotificationCategory = "financeira" | "sistema";

export const demoNotifications = [
  { id: "aluguel", category: "financeira", title: "Uma conta vence amanhã", time: "Há 10 min", description: "O aluguel do escritório vence em 15 de junho.", unread: true },
  { id: "pagamento", category: "financeira", title: "Pagamento realizado", time: "Há 2 h", description: "A conta Software de gestão foi paga com sucesso.", unread: true },
  { id: "movimentacao", category: "sistema", title: "Nova movimentação registrada", time: "Ontem", description: "Uma entrada de R$ 2.450,00 foi adicionada.", unread: false },
  { id: "documento", category: "sistema", title: "Documento anexado", time: "12 jun", description: "comprovante-junho.pdf foi anexado.", unread: false },
] as const;

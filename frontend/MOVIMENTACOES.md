> Atualização de 28/10/2026: modo API integrado ao Spring; armazenamento local descrito abaixo vale para local-demo. Estado atual e limites em [INTEGRACAO.md](INTEGRACAO.md). Recuperação por e-mail e biometria estão fora do escopo.

# Edição e exclusão local de movimentações

Na aba Financeiro → Movimentações, abra Ver detalhes para editar ou excluir o lançamento. A edição abre os dados existentes, mantém o identificador e a data de criação, valida os mesmos campos do cadastro e permite conservar, trocar ou remover o anexo.

Excluir solicita confirmação pelo nome do lançamento e envia registro e anexo juntos à lixeira. Não há exclusão permanente. A lixeira fica acessível em Ver lixeira e permite restaurar o registro com o anexo. Movimentações excluídas ficam fora dos totais, gráfico, pesquisa e documentos. Ao voltar às consultas, a base local é lida novamente.

As alterações são feitas em uma transação IndexedDB, confirmadas somente ao concluir a gravação. A edição não recria registros que tenham sido enviados à lixeira por outra tela. Falhas preservam os dados do formulário e oferecem nova tentativa. Não existe escrita em API, banco remoto ou legado.

Verificação pelo navegador com registro fictício: alteração de R$ 10,00 para R$ 20,00; total de R$ 135,50 para R$ 145,50; cancelamento da confirmação; exclusão reduzindo o total para R$ 125,50 e retirando o PDF de Documentos; restauração com anexo; remoção e reanexação de PDF durante edição. O registro de teste permanece com R$ 20,00. Validação de layout móvel, lint e exportação estática com prefixo do Pages.

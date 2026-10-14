export interface CamposAtor { nome: string; nacionalidade: string; dataNascimento: string; }

export function validarAtor(ator: CamposAtor, hoje = new Date()): CamposAtor {
  const erros: CamposAtor = { nome: '', nacionalidade: '', dataNascimento: '' };
  if (!ator.nome?.trim()) erros.nome = 'Informe o nome do ator.';
  if (!ator.nacionalidade?.trim()) erros.nacionalidade = 'Informe a nacionalidade do ator.';
  const valor = ator.dataNascimento;
  if (!valor?.trim()) {
    erros.dataNascimento = 'Informe a data de nascimento.';
  } else if (!/^[0-9]{4}-[0-9]{2}-[0-9]{2}$/.test(valor)) {
    erros.dataNascimento = 'Informe uma data válida.';
  } else {
    const [ano, mes, dia] = valor.split('-').map(Number);
    const data = new Date(0);
    data.setUTCFullYear(ano, mes - 1, dia);
    if (ano < 1 || data.getUTCFullYear() !== ano || data.getUTCMonth() !== mes - 1 || data.getUTCDate() !== dia) {
      erros.dataNascimento = 'Informe uma data válida.';
    } else {
      const limite = new Date(0);
      limite.setUTCFullYear(hoje.getFullYear(), hoje.getMonth(), hoje.getDate());
      if (data > limite) erros.dataNascimento = 'A data de nascimento não pode estar no futuro.';
    }
  }
  return erros;
}

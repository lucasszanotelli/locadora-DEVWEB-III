import { validarAtor } from './validacao-ator';

describe('Validação do cadastro de ator', () => {
  const hoje = new Date(2026, 9, 13);
  const valido = { nome: 'Ator', nacionalidade: 'Brasil', dataNascimento: '2000-02-29' };
  it('aceita uma data real em ano bissexto e o dia atual', () => {
    expect(Object.values(validarAtor(valido, hoje)).every(x => !x)).toBeTrue();
    expect(validarAtor({ ...valido, dataNascimento: '2026-10-13' }, hoje).dataNascimento).toBe('');
  });
  it('exige os três campos, incluindo textos com apenas espaços e valores nulos', () => {
    for (const campo of ['nome', 'nacionalidade', 'dataNascimento'] as const) {
      for (const vazio of ['', '   ', null, undefined]) {
        expect(validarAtor({ ...valido, [campo]: vazio } as any, hoje)[campo]).not.toBe('');
      }
    }
  });
  it('rejeita datas impossíveis, formatos incorretos e datas futuras', () => {
    for (const dataNascimento of ['2023-02-29', '2000-02-30', '2000-13-01', '0000-01-01', '29/02/2000', '2000-2-29', '2026-10-14']) {
      expect(validarAtor({ ...valido, dataNascimento }, hoje).dataNascimento).not.toBe('');
    }
  });
});

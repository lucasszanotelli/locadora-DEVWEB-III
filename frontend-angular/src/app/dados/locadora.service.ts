import { Injectable, signal } from '@angular/core';

export interface PessoaSimples { id: number; nome: string; vinculado?: boolean }
export interface Classe { id: number; nome: string; valor: number; prazo: number; vinculado?: boolean }
export interface Titulo { id: number; nome: string; nomeOriginal: string; ano: number; categoria: string; classe: string; diretor: string; atores: string[]; nacionalidade: string; distribuidor: string; sinopse: string; itens: number; disponiveis: number }
export interface Item { id: number; serie: string; titulo: string; aquisicao: string; tipo: 'DVD' | 'Blu-ray' | 'Fita'; status: 'Disponível' | 'Locado' | 'Manutenção'; possuiLocacoes?: boolean }
export interface Cliente { id: number; inscricao: string; nome: string; tipo: 'Sócio' | 'Dependente'; cpf?: string; telefone?: string; endereco?: string; sexo: string; nascimento: string; ativo: boolean; socioId?: number; possuiLocacoes?: boolean }
export interface Locacao { id: number; item: string; serie: string; cliente: string; data: string; prevista: string; devolucao?: string; valor: number; multa: number; paga: boolean; status: 'Em aberto' | 'Atrasada' | 'Devolvida' }

@Injectable({ providedIn: 'root' })
export class LocadoraService {
  atores = signal<PessoaSimples[]>([]);
  diretores = signal<PessoaSimples[]>([]);
  classes = signal<Classe[]>([]);
  titulos = signal<Titulo[]>([]);
  itens = signal<Item[]>([]);
  clientes = signal<Cliente[]>([]);
  locacoes = signal<Locacao[]>([]);

  proximo(lista: { id: number }[]) { return Math.max(0, ...lista.map(x => x.id)) + 1; }
  salvarSimples(tipo: 'atores' | 'diretores', item: PessoaSimples) { const xs = this[tipo](); this[tipo].set(item.id ? xs.map(x => x.id === item.id ? item : x) : [...xs, { ...item, id: this.proximo(xs) }]); }
  excluirSimples(tipo: 'atores' | 'diretores', id: number) { const item = this[tipo]().find(x => x.id === id); if (item?.vinculado) return false; this[tipo].update(xs => xs.filter(x => x.id !== id)); return true; }
  salvarClasse(item: Classe) { const xs = this.classes(); this.classes.set(item.id ? xs.map(x => x.id === item.id ? item : x) : [...xs, { ...item, id: this.proximo(xs) }]); }
  salvarTitulo(item: Titulo) { const xs = this.titulos(); this.titulos.set(item.id ? xs.map(x => x.id === item.id ? item : x) : [...xs, { ...item, id: this.proximo(xs) }]); }
  salvarItem(item: Item) { const xs = this.itens(); this.itens.set(item.id ? xs.map(x => x.id === item.id ? item : x) : [...xs, { ...item, id: this.proximo(xs) }]); }
  salvarCliente(item: Cliente) { const xs = this.clientes(); this.clientes.set(item.id ? xs.map(x => x.id === item.id ? item : x) : [...xs, { ...item, id: this.proximo(xs), inscricao: `${item.tipo === 'Sócio' ? 'SOC' : 'DEP'}-${String(this.proximo(xs)).padStart(4, '0')}` }]); }
  salvarLocacao(item: Locacao) { const xs = this.locacoes(); this.locacoes.set(item.id ? xs.map(x => x.id === item.id ? item : x) : [...xs, { ...item, id: this.proximo(xs) }]); }
}

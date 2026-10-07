import { Injectable, signal } from '@angular/core';

export interface PessoaSimples { id:number; nome:string; vinculado?:boolean }
export interface Classe { id:number; nome:string; valor:number; prazo:number; vinculado?:boolean }
export interface Titulo { id:number; nome:string; nomeOriginal:string; ano:number; categoria:string; classe:string; diretor:string; atores:string[]; nacionalidade:string; distribuidor:string; sinopse:string; itens:number; disponiveis:number }
export interface Item { id:number; serie:string; titulo:string; aquisicao:string; tipo:'DVD'|'Blu-ray'|'Fita'; status:'Disponível'|'Locado'|'Manutenção'; possuiLocacoes?:boolean }
export interface Cliente { id:number; inscricao:string; nome:string; tipo:'Sócio'|'Dependente'; cpf?:string; telefone?:string; endereco?:string; sexo:string; nascimento:string; ativo:boolean; socioId?:number; possuiLocacoes?:boolean }
export interface Locacao { id:number; item:string; serie:string; cliente:string; data:string; prevista:string; devolucao?:string; valor:number; multa:number; paga:boolean; status:'Em aberto'|'Atrasada'|'Devolvida' }

@Injectable({providedIn:'root'})
export class LocadoraService {
  atores = signal<PessoaSimples[]>([]);
  diretores = signal<PessoaSimples[]>([{id:1,nome:'Christopher Nolan',vinculado:true},{id:2,nome:'Greta Gerwig',vinculado:true},{id:3,nome:'Steven Spielberg'}]);
  classes = signal<Classe[]>([{id:1,nome:'Lançamento',valor:12.9,prazo:2,vinculado:true},{id:2,nome:'Catálogo',valor:8.5,prazo:3,vinculado:true},{id:3,nome:'Clássico',valor:6,prazo:5}]);
  titulos = signal<Titulo[]>([
    {id:1,nome:'Oppenheimer',nomeOriginal:'Oppenheimer',ano:2023,categoria:'Drama',classe:'Lançamento',diretor:'Christopher Nolan',atores:['Cillian Murphy'],nacionalidade:'Estados Unidos',distribuidor:'Universal',sinopse:'A história do físico J. Robert Oppenheimer e seu papel no desenvolvimento da bomba atômica.',itens:3,disponiveis:2},
    {id:2,nome:'Barbie',nomeOriginal:'Barbie',ano:2023,categoria:'Comédia',classe:'Catálogo',diretor:'Greta Gerwig',atores:['Emma Stone'],nacionalidade:'Estados Unidos',distribuidor:'Warner Bros.',sinopse:'Uma jornada pelo mundo real transforma a visão de Barbie sobre seu propósito.',itens:2,disponiveis:1},
    {id:3,nome:'O Resgate do Soldado Ryan',nomeOriginal:'Saving Private Ryan',ano:1998,categoria:'Guerra',classe:'Clássico',diretor:'Steven Spielberg',atores:['Tom Hanks'],nacionalidade:'Estados Unidos',distribuidor:'Paramount',sinopse:'Soldados atravessam a França ocupada em busca de um paraquedista.',itens:1,disponiveis:1}
  ]);
  itens = signal<Item[]>([
    {id:1,serie:'DVD-2024-001',titulo:'Oppenheimer',aquisicao:'2024-01-15',tipo:'DVD',status:'Disponível'},
    {id:2,serie:'BLU-2024-002',titulo:'Oppenheimer',aquisicao:'2024-01-15',tipo:'Blu-ray',status:'Locado',possuiLocacoes:true},
    {id:3,serie:'DVD-2024-003',titulo:'Barbie',aquisicao:'2024-02-03',tipo:'DVD',status:'Disponível'},
    {id:4,serie:'FIT-1999-014',titulo:'O Resgate do Soldado Ryan',aquisicao:'1999-05-18',tipo:'Fita',status:'Disponível'}
  ]);
  clientes = signal<Cliente[]>([
    {id:1,inscricao:'SOC-0001',nome:'André Martins',tipo:'Sócio',cpf:'123.456.789-00',telefone:'(11) 99999-1200',endereco:'Rua das Flores, 120',sexo:'Masculino',nascimento:'1988-06-12',ativo:true,possuiLocacoes:true},
    {id:2,inscricao:'DEP-0002',nome:'Lia Martins',tipo:'Dependente',sexo:'Feminino',nascimento:'2008-11-20',ativo:true,socioId:1},
    {id:3,inscricao:'SOC-0003',nome:'Beatriz Souza',tipo:'Sócio',cpf:'987.654.321-00',telefone:'(11) 98888-2211',endereco:'Av. Central, 45',sexo:'Feminino',nascimento:'1994-02-17',ativo:true}
  ]);
  locacoes = signal<Locacao[]>([
    {id:1,item:'Oppenheimer',serie:'BLU-2024-002',cliente:'André Martins',data:'2026-09-27',prevista:'2026-09-29',valor:12.9,multa:3,paga:false,status:'Atrasada'},
    {id:2,item:'Barbie',serie:'DVD-2024-003',cliente:'Beatriz Souza',data:'2026-09-28',prevista:'2026-10-01',valor:8.5,multa:0,paga:false,status:'Em aberto'},
    {id:3,item:'O Resgate do Soldado Ryan',serie:'FIT-1999-014',cliente:'André Martins',data:'2026-09-20',prevista:'2026-09-25',devolucao:'2026-09-25',valor:6,multa:0,paga:true,status:'Devolvida'}
  ]);

  proximo(lista:{id:number}[]) { return Math.max(0,...lista.map(x=>x.id))+1; }
  salvarSimples(tipo:'atores'|'diretores', item:PessoaSimples){ const lista=this[tipo](); this[tipo].set(item.id?lista.map(x=>x.id===item.id?item:x):[...lista,{...item,id:this.proximo(lista)}]); }
  excluirSimples(tipo:'atores'|'diretores', id:number){ const item=this[tipo]().find(x=>x.id===id); if(item?.vinculado) return false; this[tipo].update(xs=>xs.filter(x=>x.id!==id)); return true; }
  salvarClasse(item:Classe){const xs=this.classes();this.classes.set(item.id?xs.map(x=>x.id===item.id?item:x):[...xs,{...item,id:this.proximo(xs)}]);}
  salvarTitulo(item:Titulo){const xs=this.titulos();this.titulos.set(item.id?xs.map(x=>x.id===item.id?item:x):[...xs,{...item,id:this.proximo(xs)}]);}
  salvarItem(item:Item){const xs=this.itens();this.itens.set(item.id?xs.map(x=>x.id===item.id?item:x):[...xs,{...item,id:this.proximo(xs)}]);}
  salvarCliente(item:Cliente){const xs=this.clientes();this.clientes.set(item.id?xs.map(x=>x.id===item.id?item:x):[...xs,{...item,id:this.proximo(xs),inscricao:`${item.tipo==='Sócio'?'SOC':'DEP'}-${String(this.proximo(xs)).padStart(4,'0')}`}]);}
  salvarLocacao(item:Locacao){const xs=this.locacoes();this.locacoes.set(item.id?xs.map(x=>x.id===item.id?item:x):[...xs,{...item,id:this.proximo(xs)}]);}
}

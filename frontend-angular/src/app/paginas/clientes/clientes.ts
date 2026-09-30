import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CabecalhoPagina } from '../../componentes-genericos/cabecalho-pagina/cabecalho-pagina';
import { Botao } from '../../componentes-genericos/botao/botao';
import { Modal } from '../../componentes-genericos/modal/modal';
import { Status } from '../../componentes-genericos/status/status';
import { Cliente, LocadoraService } from '../../dados/locadora.service';

@Component({selector:'app-clientes',standalone:true,imports:[FormsModule,CabecalhoPagina,Botao,Modal,Status],templateUrl:'./clientes.html'})
export class Clientes {
  busca=''; modal=false; erro=''; item=this.vazio();
  constructor(public dados:LocadoraService){}
  vazio():Cliente{return{id:0,inscricao:'Gerada automaticamente',nome:'',tipo:'Sócio',cpf:'',telefone:'',endereco:'',sexo:'',nascimento:'',ativo:true}}
  get lista(){return this.dados.clientes().filter(x=>(x.nome+x.inscricao+x.cpf).toLowerCase().includes(this.busca.toLowerCase()))}
  nomeSocio(id?:number){return this.dados.clientes().find(x=>x.id===id)?.nome||'—'}
  novo(){this.item=this.vazio();this.modal=true;this.erro=''}
  editar(x:Cliente){this.item={...x};this.modal=true;this.erro=''}
  salvar(){if(!this.item.nome||!this.item.sexo||!this.item.nascimento||(this.item.tipo==='Sócio'&&(!this.item.cpf||!this.item.endereco||!this.item.telefone))){this.erro='Preencha todos os dados obrigatórios.';return}if(this.item.tipo==='Dependente'){const qtd=this.dados.clientes().filter(x=>x.socioId===this.item.socioId&&x.ativo&&x.id!==this.item.id).length;if(qtd>=3){this.erro='O sócio selecionado já possui três dependentes ativos.';return}}this.dados.salvarCliente(this.item);this.modal=false}
  alternar(x:Cliente){const ativo=!x.ativo;this.dados.clientes.update(xs=>xs.map(y=>(y.id===x.id||(x.tipo==='Sócio'&&y.socioId===x.id))?{...y,ativo}:y))}
  excluir(x:Cliente){if(x.possuiLocacoes){alert('O cliente possui locações e não pode ser excluído.');return}if(confirm('Excluir este cliente, seus dependentes e reservas?'))this.dados.clientes.update(xs=>xs.filter(y=>y.id!==x.id&&y.socioId!==x.id))}
}

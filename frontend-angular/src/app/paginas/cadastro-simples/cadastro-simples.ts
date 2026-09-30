import { Component, OnInit, signal } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CurrencyPipe } from '@angular/common';
import { CabecalhoPagina } from '../../componentes-genericos/cabecalho-pagina/cabecalho-pagina';
import { Botao } from '../../componentes-genericos/botao/botao';
import { Modal } from '../../componentes-genericos/modal/modal';
import { InputComponent } from '../../componentes-genericos/input/input';
import { LocadoraService, PessoaSimples, Classe } from '../../dados/locadora.service';

@Component({selector:'app-cadastro-simples',standalone:true,imports:[FormsModule,CurrencyPipe,CabecalhoPagina,Botao,Modal,InputComponent],templateUrl:'./cadastro-simples.html'})
export class CadastroSimples implements OnInit{
  tipo:'atores'|'diretores'|'classes'='atores'; titulo='Atores'; modal=false; busca=''; erro=''; item:any={id:0,nome:''};
  constructor(public dados:LocadoraService,private route:ActivatedRoute){}
  ngOnInit(){this.route.data.subscribe(d=>{this.tipo=d['tipo'];this.titulo=d['titulo'];this.modal=false;this.busca='';});}
  get lista():any[]{return this.dados[this.tipo]().filter((x:any)=>x.nome.toLowerCase().includes(this.busca.toLowerCase()));}
  novo(){this.item=this.tipo==='classes'?{id:0,nome:'',valor:0,prazo:1}:{id:0,nome:''};this.erro='';this.modal=true;}
  editar(x:any){this.item={...x};this.erro='';this.modal=true;}
  salvar(){if(!this.item.nome.trim()||(this.tipo==='classes'&&(this.item.valor<=0||this.item.prazo<1))){this.erro='Preencha os dados obrigatórios com valores válidos.';return;} if(this.tipo==='classes')this.dados.salvarClasse(this.item as Classe);else this.dados.salvarSimples(this.tipo,this.item as PessoaSimples);this.modal=false;}
  excluir(x:any){if(!confirm(`Deseja realmente excluir ${x.nome}?`))return;let ok=true;if(this.tipo==='classes'){if(x.vinculado)ok=false;else this.dados.classes.update(xs=>xs.filter(y=>y.id!==x.id));}else ok=this.dados.excluirSimples(this.tipo,x.id);if(!ok)alert(`${this.titulo.slice(0,-1)} associado(a) a títulos. A exclusão não é permitida.`);}
}

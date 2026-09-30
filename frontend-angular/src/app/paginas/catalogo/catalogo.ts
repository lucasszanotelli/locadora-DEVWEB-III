import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CurrencyPipe } from '@angular/common';
import { CabecalhoPagina } from '../../componentes-genericos/cabecalho-pagina/cabecalho-pagina';
import { Modal } from '../../componentes-genericos/modal/modal';
import { Status } from '../../componentes-genericos/status/status';
import { LocadoraService,Titulo } from '../../dados/locadora.service';

@Component({selector:'app-catalogo',standalone:true,imports:[FormsModule,CurrencyPipe,CabecalhoPagina,Modal,Status],templateUrl:'./catalogo.html',styleUrl:'./catalogo.css'})
export class Catalogo {
  busca=''; categoria=''; ator=''; detalhe:Titulo|null=null;
  constructor(public dados:LocadoraService){}
  get categorias(){return[...new Set(this.dados.titulos().map(x=>x.categoria))]}
  get lista(){const q=this.busca.toLowerCase();return this.dados.titulos().filter(x=>(!q||(x.nome+x.nomeOriginal).toLowerCase().includes(q))&&(!this.categoria||x.categoria===this.categoria)&&(!this.ator||x.atores.includes(this.ator)))}
  get valorDetalhe(){return this.dados.classes().find(x=>x.nome===this.detalhe?.classe)?.valor||0}
}

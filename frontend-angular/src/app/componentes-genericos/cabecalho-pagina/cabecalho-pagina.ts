import { Component, Input } from '@angular/core';
@Component({selector:'app-cabecalho-pagina',standalone:true,templateUrl:'./cabecalho-pagina.html',styleUrl:'./cabecalho-pagina.css'})
export class CabecalhoPagina {@Input() titulo='';@Input() subtitulo='';}

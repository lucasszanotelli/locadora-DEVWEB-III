import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { DatePipe } from '@angular/common';
import { CabecalhoPagina } from '../../componentes-genericos/cabecalho-pagina/cabecalho-pagina';
import { Status } from '../../componentes-genericos/status/status';
import { LocadoraService } from '../../dados/locadora.service';
@Component({ selector: 'app-inicio', standalone: true, imports: [RouterLink, DatePipe, CabecalhoPagina, Status], templateUrl: './inicio.html', styleUrl: './inicio.css' })
export class Inicio { constructor(public dados: LocadoraService) { } hoje = new Intl.DateTimeFormat('pt-BR', { weekday: 'long', day: '2-digit', month: 'long' }).format(new Date()); get locacoesAtivas() { return this.dados.locacoes().filter(x => x.status !== 'Devolvida').length } get atrasadas() { return this.dados.locacoes().filter(x => x.status === 'Atrasada').length } get clientesAtivos() { return this.dados.clientes().filter(x => x.ativo).length } get itensDisponiveis() { return this.dados.itens().filter(x => x.status === 'Disponível').length } }

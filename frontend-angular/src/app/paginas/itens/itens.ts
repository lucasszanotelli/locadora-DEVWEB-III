import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DatePipe } from '@angular/common';
import { finalize } from 'rxjs';
import { CabecalhoPagina } from '../../componentes-genericos/cabecalho-pagina/cabecalho-pagina';
import { Botao } from '../../componentes-genericos/botao/botao';
import { Modal } from '../../componentes-genericos/modal/modal';
import { Status } from '../../componentes-genericos/status/status';
import { LocadoraService, Item } from '../../dados/locadora.service';
import { ItemService, ItemApi } from '../../dados/item.service';
import { TituloService } from '../../dados/titulo.service';

@Component({ selector: 'app-itens', standalone: true, imports: [FormsModule, DatePipe, CabecalhoPagina, Botao, Modal, Status], templateUrl: './itens.html' })
export class Itens implements OnInit {
  private api = inject(ItemService);
  private tituloApi = inject(TituloService);
  busca = ''; modal = false; erro = ''; carregando = false; processando = false;
  item = this.vazio();
  constructor(public dados: LocadoraService) { }
  ngOnInit() {
    this.carregar();
    this.tituloApi.listar().subscribe({ next: xs => this.dados.titulos.set(xs.map(x => ({ ...x, atores: x.atores ?? [], itens: 0, disponiveis: 0 }))), error: () => {} });
  }
  private mapear(x: ItemApi): Item { return { id: x.serial, serie: x.numeroSerie, titulo: x.titulo, aquisicao: x.dataAquisicao, tipo: x.tipoItem, status: x.status }; }
  vazio(): Item { return { id: 0, serie: '', titulo: '', aquisicao: new Date().toISOString().slice(0, 10), tipo: 'DVD', status: 'Disponível' }; }
  get lista() { return this.dados.itens().filter(x => (x.serie + x.titulo + x.tipo).toLowerCase().includes(this.busca.toLowerCase())); }
  carregar() {
    this.carregando = true; this.erro = '';
    this.api.listar().pipe(finalize(() => this.carregando = false)).subscribe({ next: xs => this.dados.itens.set(xs.map(x => this.mapear(x))), error: () => this.erro = 'Não foi possível carregar os itens. Verifique se o backend está em execução.' });
  }
  novo() { this.item = this.vazio(); this.erro = ''; this.modal = true; }
  editar(x: Item) { this.item = { ...x }; this.erro = ''; this.modal = true; }
  fechar() { if (!this.processando) this.modal = false; }
  salvar() {
    if (this.processando) return;
    if (!this.item.serie.trim() || !this.item.titulo || !this.item.aquisicao) { this.erro = 'Informe série, título cadastrado e data de aquisição.'; return; }
    const payload = { numeroSerie: this.item.serie.trim(), titulo: this.item.titulo, dataAquisicao: this.item.aquisicao, tipoItem: this.item.tipo, status: this.item.status };
    const req = this.item.id ? this.api.atualizar(this.item.id, payload) : this.api.inserir(payload);
    this.processando = true; this.erro = '';
    req.pipe(finalize(() => this.processando = false)).subscribe({ next: x => { const salvo = this.mapear(x); const atual = this.dados.itens().filter(y => y.id !== salvo.id); this.dados.itens.set([...atual, salvo]); this.modal = false; }, error: () => this.erro = 'Não foi possível salvar o item. Selecione um título do banco e confira os dados.' });
  }
  excluir(x: Item) {
    if (this.processando || !confirm(`Excluir o item ${x.serie}?`)) return;
    this.processando = true; this.erro = '';
    this.api.excluir(x.id).pipe(finalize(() => this.processando = false)).subscribe({ next: () => this.dados.itens.update(xs => xs.filter(y => y.id !== x.id)), error: () => this.erro = 'Não foi possível excluir o item.' });
  }
}

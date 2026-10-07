import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CurrencyPipe } from '@angular/common';
import { finalize } from 'rxjs';
import { CabecalhoPagina } from '../../componentes-genericos/cabecalho-pagina/cabecalho-pagina';
import { Botao } from '../../componentes-genericos/botao/botao';
import { Modal } from '../../componentes-genericos/modal/modal';
import { Status } from '../../componentes-genericos/status/status';
import { LocadoraService, Titulo } from '../../dados/locadora.service';
import { TituloApi, TituloService } from '../../dados/titulo.service';
import { ItemService } from '../../dados/item.service';
import { AtorService } from '../../dados/ator.service';
import { DiretorService } from '../../dados/diretor.service';

@Component({ selector: 'app-titulos', standalone: true, imports: [FormsModule, CurrencyPipe, CabecalhoPagina, Botao, Modal, Status], templateUrl: './titulos.html' })
export class Titulos implements OnInit {
  private tituloApi = inject(TituloService);
  private itemApi = inject(ItemService);
  private atorApi = inject(AtorService);
  private diretorApi = inject(DiretorService);
  busca = ''; modal = false; erro = ''; carregando = false; processando = false;
  detalhe: Titulo | null = null;
  item = this.vazio();
  constructor(public dados: LocadoraService) { }
  ngOnInit() {
    this.carregar();
    this.itemApi.listar().subscribe({ next: itens => { this.dados.itens.set(itens.map(i => ({ id: i.serial, serie: i.numeroSerie, titulo: i.titulo, aquisicao: i.dataAquisicao, tipo: i.tipoItem, status: i.status }))); this.recontarTitulos(); }, error: () => {} });
    this.atorApi.listar().subscribe({ next: xs => this.dados.atores.set(xs.map(x => ({ id: x.id, nome: x.nome }))), error: () => {} });
    this.diretorApi.listar().subscribe({ next: xs => this.dados.diretores.set(xs.map(x => ({ id: x.id, nome: x.nome }))), error: () => {} });
  }
  vazio(): Titulo { return { id: 0, nome: '', nomeOriginal: '', ano: new Date().getFullYear(), categoria: '', classe: '', diretor: '', atores: [], nacionalidade: '', distribuidor: '', sinopse: '', itens: 0, disponiveis: 0 }; }
  private mapear(api: TituloApi[]): Titulo[] {
    const itens = this.dados.itens();
    return api.map(x => { const vinculados = itens.filter(i => i.titulo === x.nome); return { ...x, atores: x.atores ?? [], itens: vinculados.length, disponiveis: vinculados.filter(i => i.status === 'Disponível').length }; });
  }
  private recontarTitulos() {
    this.dados.titulos.update(titulos => titulos.map(titulo => {
      const itens = this.dados.itens().filter(item => item.titulo === titulo.nome);
      return { ...titulo, itens: itens.length, disponiveis: itens.filter(item => item.status === 'Disponível').length };
    }));
  }
  carregar() {
    this.carregando = true; this.erro = '';
    this.tituloApi.listar().pipe(finalize(() => this.carregando = false)).subscribe({ next: xs => this.dados.titulos.set(this.mapear(xs)), error: () => this.erro = 'Não foi possível carregar os títulos. Verifique se o backend está em execução.' });
  }
  get lista() { return this.dados.titulos().filter(x => [x.nome, x.nomeOriginal, x.categoria, x.atores.join()].some(v => v.toLowerCase().includes(this.busca.toLowerCase()))); }
  get valorDetalhe() { return this.dados.classes().find(x => x.nome === this.detalhe?.classe)?.valor || 0; }
  novo() { this.item = this.vazio(); this.erro = ''; this.modal = true; }
  editar(x: Titulo) { this.item = { ...x, atores: [...x.atores] }; this.erro = ''; this.modal = true; }
  atorToggle(nome: string, e: Event) { const ativo = (e.target as HTMLInputElement).checked; this.item.atores = ativo ? [...this.item.atores, nome] : this.item.atores.filter(x => x !== nome); }
  salvar() {
    if (this.processando) return;
    if (!this.item.nome.trim() || !this.item.categoria.trim() || !this.item.diretor) { this.erro = 'Preencha nome, categoria e diretor.'; return; }
    const { id, itens, disponiveis, ...payload } = this.item;
    this.processando = true; this.erro = '';
    const request = id ? this.tituloApi.atualizar(id, payload) : this.tituloApi.inserir(payload);
    request.pipe(finalize(() => this.processando = false)).subscribe({ next: salvo => { const atuais = this.dados.titulos().filter(x => x.id !== salvo.id); this.dados.titulos.set([...atuais, ...this.mapear([salvo])]); this.recontarTitulos(); this.modal = false; }, error: () => this.erro = 'Não foi possível salvar o título. Verifique os dados e a conexão com o backend.' });
  }
  excluir(x: Titulo) {
    if (this.processando) return;
    if (x.itens) { alert('Este título possui itens cadastrados e não pode ser excluído.'); return; }
    if (!confirm(`Excluir o título ${x.nome}?`)) return;
    this.processando = true;
    this.tituloApi.excluir(x.id).pipe(finalize(() => this.processando = false)).subscribe({ next: () => this.dados.titulos.update(xs => xs.filter(y => y.id !== x.id)), error: () => this.erro = 'Não foi possível excluir o título.' });
  }
}

import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DatePipe } from '@angular/common';
import { finalize } from 'rxjs';
import { Ator, AtorService } from '../../dados/ator.service';
import { LocadoraService } from '../../dados/locadora.service';
import { CabecalhoPagina } from '../../componentes-genericos/cabecalho-pagina/cabecalho-pagina';
import { Botao } from '../../componentes-genericos/botao/botao';
import { Modal } from '../../componentes-genericos/modal/modal';
import { InputComponent } from '../../componentes-genericos/input/input';

@Component({
  selector: 'app-atores', standalone: true,
  imports: [FormsModule, DatePipe, CabecalhoPagina, Botao, Modal, InputComponent],
  templateUrl: './atores.html'
})
export class Atores implements OnInit {
  private api = inject(AtorService);
  private dados = inject(LocadoraService);
  atores = signal<Ator[]>([]);
  carregando = false;
  processando = false;
  modal = false;
  busca = '';
  erro = '';
  erroFormulario = '';
  item: Ator = this.vazio();

  ngOnInit() { this.carregar(); }
  get lista() { return this.atores().filter(x => x.nome.toLowerCase().includes(this.busca.toLowerCase())); }
  private vazio(): Ator { return { id: 0, nome: '', nacionalidade: '', dataNascimento: '' }; }
  private atualizarLista(atores: Ator[]) {
    this.atores.set(atores);
    this.dados.atores.set(atores);
  }
  carregar() {
    this.carregando = true;
    this.erro = '';
    this.api.listar().pipe(finalize(() => this.carregando = false)).subscribe({
      next: atores => this.atualizarLista(atores),
      error: () => this.erro = 'Não foi possível carregar os atores. Verifique se o backend está em execução.'
    });
  }
  novo() { this.item = this.vazio(); this.erroFormulario = ''; this.modal = true; }
  editar(ator: Ator) { this.item = { ...ator }; this.erroFormulario = ''; this.modal = true; }
  fechar() { if (!this.processando) this.modal = false; }
  salvar() {
    if (this.processando) return;
    const { nome, nacionalidade, dataNascimento } = this.item;
    if (!nome.trim()) { this.erroFormulario = 'Informe o nome do ator.'; return; }
    const dados = { nome: nome.trim(), nacionalidade: (nacionalidade ?? '').trim(), dataNascimento: dataNascimento ?? '' };
    const requisicao = this.item.id ? this.api.atualizar(this.item.id, dados) : this.api.inserir(dados);
    this.processando = true;
    this.erroFormulario = '';
    requisicao.pipe(finalize(() => this.processando = false)).subscribe({
      next: salvo => {
        const existe = this.atores().some(x => x.id === salvo.id);
        this.atualizarLista(existe ? this.atores().map(x => x.id === salvo.id ? salvo : x) : [...this.atores(), salvo]);
        this.modal = false;
        this.erro = '';
      },
      error: e => this.erroFormulario = e.status === 404 ? 'Este ator não foi encontrado. Atualize a lista.' : 'Não foi possível salvar o ator. Verifique os dados e a conexão com o backend.'
    });
  }
  excluir(ator: Ator) {
    if (this.processando || !confirm(`Deseja realmente excluir ${ator.nome}?`)) return;
    this.processando = true;
    this.erro = '';
    this.api.excluir(ator.id).pipe(finalize(() => this.processando = false)).subscribe({
      next: () => this.atualizarLista(this.atores().filter(x => x.id !== ator.id)),
      error: () => this.erro = 'Não foi possível excluir o ator. Atualize a lista e tente novamente.'
    });
  }
}

import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DatePipe } from '@angular/common';
import { finalize } from 'rxjs';
import { Diretor, DiretorService } from '../../dados/diretor.service';
import { LocadoraService, PessoaSimples } from '../../dados/locadora.service';
import { CabecalhoPagina } from '../../componentes-genericos/cabecalho-pagina/cabecalho-pagina';
import { Botao } from '../../componentes-genericos/botao/botao';
import { Modal } from '../../componentes-genericos/modal/modal';
import { InputComponent } from '../../componentes-genericos/input/input';

@Component({
  selector: 'app-diretores', standalone: true,
  imports: [FormsModule, DatePipe, CabecalhoPagina, Botao, Modal, InputComponent],
  templateUrl: './diretores.html'
})
export class Diretores implements OnInit {
  private api = inject(DiretorService);
  private dados = inject(LocadoraService);
  diretores = signal<Diretor[]>([]);
  carregando = false;
  processando = false;
  modal = false;
  busca = '';
  erro = '';
  erroFormulario = '';
  item: Diretor = this.vazio();

  ngOnInit() { this.carregar(); }
  get lista() { return this.diretores().filter(x => x.nome.toLowerCase().includes(this.busca.toLowerCase())); }
  private vazio(): Diretor { return { id: 0, nome: '', nacionalidade: '', dataNascimento: '' }; }
  private atualizarLista(diretores: Diretor[]) {
    this.diretores.set(diretores);
    this.dados.diretores.set(diretores.map(({ id, nome }) => ({ id, nome })) as PessoaSimples[]);
  }
  carregar() {
    this.carregando = true;
    this.erro = '';
    this.api.listar().pipe(finalize(() => this.carregando = false)).subscribe({
      next: diretores => this.atualizarLista(diretores),
      error: () => this.erro = 'Não foi possível carregar os diretores. Verifique se o backend está em execução.'
    });
  }
  novo() { this.item = this.vazio(); this.erroFormulario = ''; this.modal = true; }
  editar(diretor: Diretor) { this.item = { ...diretor }; this.erroFormulario = ''; this.modal = true; }
  fechar() { if (!this.processando) this.modal = false; }
  salvar() {
    if (this.processando) return;
    const { nome, nacionalidade, dataNascimento } = this.item;
    if (!nome.trim()) { this.erroFormulario = 'Informe o nome do diretor.'; return; }
    const dados = { nome: nome.trim(), nacionalidade: (nacionalidade ?? '').trim(), dataNascimento: dataNascimento ?? '' };
    const requisicao = this.item.id ? this.api.atualizar(this.item.id, dados) : this.api.inserir(dados);
    this.processando = true;
    this.erroFormulario = '';
    requisicao.pipe(finalize(() => this.processando = false)).subscribe({
      next: salvo => {
        const existe = this.diretores().some(x => x.id === salvo.id);
        this.atualizarLista(existe ? this.diretores().map(x => x.id === salvo.id ? salvo : x) : [...this.diretores(), salvo]);
        this.modal = false;
        this.erro = '';
      },
      error: e => this.erroFormulario = e.status === 404 ? 'Este diretor não foi encontrado. Atualize a lista.' : 'Não foi possível salvar o diretor. Verifique os dados e a conexão com o backend.'
    });
  }
  excluir(diretor: Diretor) {
    if (this.processando || !confirm(`Deseja realmente excluir ${diretor.nome}?`)) return;
    this.processando = true;
    this.erro = '';
    this.api.excluir(diretor.id).pipe(finalize(() => this.processando = false)).subscribe({
      next: () => this.atualizarLista(this.diretores().filter(x => x.id !== diretor.id)),
      error: () => this.erro = 'Não foi possível excluir o diretor. Atualize a lista e tente novamente.'
    });
  }
}

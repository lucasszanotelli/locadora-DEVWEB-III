import { Component, OnInit, inject } from '@angular/core'
import { FormsModule } from '@angular/forms'
import { CurrencyPipe } from '@angular/common'
import { HttpErrorResponse } from '@angular/common/http'
import { finalize } from 'rxjs'
import { CabecalhoPagina } from '../../componentes-genericos/cabecalho-pagina/cabecalho-pagina'
import { Botao } from '../../componentes-genericos/botao/botao'
import { Modal } from '../../componentes-genericos/modal/modal'
import { ClasseService } from '../../dados/classe.service'
import { Classe, LocadoraService } from '../../dados/locadora.service'

@Component({
  selector: 'app-classes',
  standalone: true,
  imports: [FormsModule, CurrencyPipe, CabecalhoPagina, Botao, Modal],
  templateUrl: './classes.html'
})
export class Classes implements OnInit {
  private api = inject(ClasseService)
  constructor (public dados: LocadoraService) {}
  busca = ''
  erro = ''
  modal = false
  carregando = false
  processando = false
  item: Classe = this.vazio()
  vazio (): Classe {
    return { id: 0, nome: '', valor: 0, prazo: 1 }
  }
  get lista () {
    return this.dados.classes().filter(x => x.nome.toLowerCase().includes(this.busca.toLowerCase()))
  }
  ngOnInit () {
    this.carregando = true
    this.api
      .listar()
      .pipe(finalize(() => (this.carregando = false)))
      .subscribe({
        next: xs => this.dados.classes.set(xs),
        error: () =>
          (this.erro =
            'Não foi possível carregar as classes. Verifique se o backend está em execução.')
      })
  }
  novo () {
    this.item = this.vazio()
    this.erro = ''
    this.modal = true
  }
  editar (classe: Classe) {
    this.item = { ...classe }
    this.erro = ''
    this.modal = true
  }
  salvar () {
    if (this.processando) return
    const { id, nome, valor, prazo } = this.item
    if (
      !nome.trim() ||
      !Number.isFinite(valor) ||
      valor <= 0 ||
      !Number.isInteger(prazo) ||
      prazo < 1
    ) {
      this.erro = 'Informe nome, valor positivo e prazo em dias inteiros.'
      return
    }
    const anterior = this.dados.classes().find(x => x.id === id)?.nome
    this.processando = true
    this.erro = ''
    const request = id
      ? this.api.atualizar(id, { nome, valor, prazo })
      : this.api.inserir({ nome, valor, prazo })
    request.pipe(finalize(() => (this.processando = false))).subscribe({
      next: salvo => {
        this.dados.classes.update(xs =>
          id ? xs.map(x => (x.id === id ? salvo : x)) : [...xs, salvo]
        )
        if (anterior && anterior !== salvo.nome)
          this.dados.titulos.update(xs =>
            xs.map(x => (x.classe === anterior ? { ...x, classe: salvo.nome } : x))
          )
        this.modal = false
      },
      error: (e: HttpErrorResponse) =>
        (this.erro =
          e.status === 409
            ? 'Já existe uma classe com esse nome.'
            : 'Não foi possível salvar a classe. Verifique os dados e a conexão com o backend.')
    })
  }
  excluir (classe: Classe) {
    if (this.processando || !confirm(`Deseja realmente excluir ${classe.nome}?`)) return
    this.processando = true
    this.erro = ''
    this.api
      .excluir(classe.id)
      .pipe(finalize(() => (this.processando = false)))
      .subscribe({
        next: () => this.dados.classes.update(xs => xs.filter(x => x.id !== classe.id)),
        error: (e: HttpErrorResponse) =>
          (this.erro =
            e.status === 409
              ? 'Classe associada a títulos. A exclusão não é permitida.'
              : 'Não foi possível excluir a classe. Verifique a conexão com o backend.')
      })
  }
}

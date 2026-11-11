import { Component, OnInit, inject } from '@angular/core'
import { FormsModule } from '@angular/forms'
import { CurrencyPipe, DatePipe } from '@angular/common'
import { finalize } from 'rxjs'
import { CabecalhoPagina } from '../../componentes-genericos/cabecalho-pagina/cabecalho-pagina'
import { Botao } from '../../componentes-genericos/botao/botao'
import { Status } from '../../componentes-genericos/status/status'
import { Locacao, LocadoraService } from '../../dados/locadora.service'
import { LocacaoService } from '../../dados/locacao.service'

@Component({
  selector: 'app-devolucoes', standalone: true,
  imports: [FormsModule, CurrencyPipe, DatePipe, CabecalhoPagina, Botao, Status],
  templateUrl: './devolucoes.html', styleUrl: './devolucoes.css'
})
export class Devolucoes implements OnInit {
  private api = inject(LocacaoService)
  serie = ''
  locacao: Locacao | null = null
  erro = ''
  erroLista = ''
  carregando = false
  concluida = false
  processando = false
  constructor (public dados: LocadoraService) {}
  ngOnInit () { this.carregar() }
  get abertas () { return this.dados.locacoes().filter(x => x.status !== 'Devolvida') }
  carregar () {
    this.carregando = true
    this.erroLista = ''
    this.api.listar().pipe(finalize(() => this.carregando = false)).subscribe({
      next: xs => this.dados.locacoes.set(xs),
      error: () => this.erroLista = 'Não foi possível carregar as locações em aberto. Tente novamente.'
    })
  }
  selecionar (x: Locacao) {
    if (this.processando) return
    this.serie = x.serie
    this.buscar()
  }
  limpar () { this.locacao = null; this.erro = ''; this.concluida = false }
  buscar () {
    if (this.processando) return
    this.limpar()
    if (!this.serie.trim()) { this.erro = 'Informe o número de série do item.'; return }
    this.processando = true
    this.api.listar().pipe(finalize(() => this.processando = false)).subscribe({
      next: xs => {
        this.dados.locacoes.set(xs)
        const serie = this.serie.trim().toLowerCase()
        const encontradas = xs.filter(x => x.serie.toLowerCase() === serie).sort((a, b) => b.id - a.id)
        this.locacao = encontradas.find(x => x.status !== 'Devolvida') || encontradas[0] || null
        if (!this.locacao) this.erro = 'Nenhuma locação encontrada para o número de série informado.'
      },
      error: e => this.erro = e.error?.message || 'Não foi possível buscar a locação. Verifique a conexão com o backend.'
    })
  }
  get total () { return this.locacao?.total || 0 }
  confirmar () {
    if (!this.locacao || this.locacao.status === 'Devolvida' || this.processando) return
    this.processando = true
    this.erro = ''
    this.api.devolver(this.locacao.id).pipe(finalize(() => this.processando = false)).subscribe({
      next: salvo => {
        this.dados.locacoes.update(xs => xs.map(x => x.id === salvo.id ? salvo : x))
        this.dados.itens.update(xs => xs.map(x => x.id === salvo.itemId ? { ...x, status: 'Disponível' } : x))
        this.concluida = true
        this.locacao = salvo
      },
      error: e => { this.erro = e.error?.message || 'Não foi possível registrar a devolução.'; this.locacao = null }
    })
  }
}

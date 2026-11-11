import { Component, OnInit, inject } from '@angular/core'
import { FormsModule } from '@angular/forms'
import { CurrencyPipe, DatePipe } from '@angular/common'
import { finalize, forkJoin } from 'rxjs'
import { CabecalhoPagina } from '../../componentes-genericos/cabecalho-pagina/cabecalho-pagina'
import { Botao } from '../../componentes-genericos/botao/botao'
import { Modal } from '../../componentes-genericos/modal/modal'
import { Status } from '../../componentes-genericos/status/status'
import { Locacao, LocadoraService } from '../../dados/locadora.service'
import { LocacaoService } from '../../dados/locacao.service'
import { ClienteService } from '../../dados/cliente.service'
import { ItemService } from '../../dados/item.service'
import { TituloService } from '../../dados/titulo.service'
import { ClasseService } from '../../dados/classe.service'

@Component({
  selector: 'app-locacoes', standalone: true,
  imports: [FormsModule, CurrencyPipe, DatePipe, CabecalhoPagina, Botao, Modal, Status],
  templateUrl: './locacoes.html'
})
export class Locacoes implements OnInit {
  private api = inject(LocacaoService)
  private clientesApi = inject(ClienteService)
  private itensApi = inject(ItemService)
  private titulosApi = inject(TituloService)
  private classesApi = inject(ClasseService)
  busca = ''
  modal = false
  erro = ''
  erroLista = ''
  carregando = false
  processando = false
  item = this.vazio()
  constructor (public dados: LocadoraService) {}
  ngOnInit () { this.carregar() }
  carregar () {
    this.carregando = true
    this.erroLista = ''
    forkJoin({ locacoes: this.api.listar(), clientes: this.clientesApi.listar(), itens: this.itensApi.listar(),
      titulos: this.titulosApi.listar(), classes: this.classesApi.listar() })
      .pipe(finalize(() => this.carregando = false)).subscribe({
        next: x => {
          this.dados.locacoes.set(x.locacoes)
          this.dados.clientes.set(x.clientes)
          this.dados.itens.set(x.itens.map(i => ({ id: i.serial, serie: i.numeroSerie, titulo: i.titulo,
            aquisicao: i.dataAquisicao, tipo: i.tipoItem, status: i.status })))
          this.dados.titulos.set(x.titulos.map(t => ({ ...t, itens: 0, disponiveis: 0 })))
          this.dados.classes.set(x.classes)
        },
        error: () => this.erroLista = 'Não foi possível carregar os dados. Verifique se o backend está em execução.'
      })
  }
  vazio (): Locacao {
    return { id: 0, clienteId: 0, itemId: 0, item: '', serie: '', cliente: '',
      data: new Intl.DateTimeFormat('en-CA', { timeZone: 'America/Sao_Paulo' }).format(new Date()),
      prevista: '', valor: 0, multa: 0, paga: false, status: 'Em aberto', diasAtraso: 0, total: 0 }
  }
  get pagamentoRegistrado () { return !!this.dados.locacoes().find(x => x.id === this.item.id)?.paga }
  get lista () {
    return this.dados.locacoes().filter(x => (x.cliente + x.item + x.serie).toLowerCase().includes(this.busca.toLowerCase()))
  }
  novo () { this.item = this.vazio(); this.modal = true; this.erro = ''; this.carregar() }
  disponibilidade (id: number) {
    const item = this.dados.itens().find(x => x.id === id)
    if (item?.status === 'Disponível') return ''
    const locacao = this.dados.locacoes().find(x => x.itemId === id && x.status !== 'Devolvida')
    if (!locacao) return ` — ${item?.status || 'Indisponível'}`
    const prevista = locacao.prevista.split('-').reverse().join('/')
    return locacao.status === 'Atrasada' ? ` — Devolução em atraso (prevista: ${prevista})` : ` — Disponibilidade prevista: ${prevista}`
  }
  selecionarItem (id: number) {
    this.item.itemId = id
    const it = this.dados.itens().find(x => x.id === id)
    this.item.serie = it?.serie || ''
    this.item.item = it?.titulo || ''
    const titulo = this.dados.titulos().find(x => x.nome.toLowerCase() === it?.titulo.toLowerCase())
    const classe = this.dados.classes().find(x => x.nome.toLowerCase() === titulo?.classe.toLowerCase())
    this.item.valor = classe?.valor || 0
    this.item.prevista = ''
    if (!classe) { this.erro = 'Configure a classe do título para calcular valor e prazo.'; return }
    const d = new Date(this.item.data + 'T12:00:00')
    d.setDate(d.getDate() + classe.prazo)
    this.item.prevista = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
    this.erro = ''
  }
  salvar () {
    if (this.processando || this.carregando) return
    if (!this.item.clienteId || !this.item.itemId || !this.item.prevista || this.item.valor <= 0) {
      this.erro = 'Selecione cliente e item e informe valor e prazo válidos.'; return
    }
    if (this.item.prevista <= this.item.data) { this.erro = 'A devolução prevista deve ser posterior à data da locação.'; return }
    const dados = { clienteId: this.item.clienteId, itemId: this.item.itemId, valor: this.item.valor, paga: this.item.paga }
    this.processando = true
    this.erro = ''
    const req = this.item.id ? this.api.atualizar(this.item.id, dados) : this.api.inserir(dados)
    req.pipe(finalize(() => this.processando = false)).subscribe({
      next: () => { this.modal = false; this.carregar() },
      error: e => this.erro = e.error?.message || 'Não foi possível salvar a locação. Confira a conexão e os dados.'
    })
  }
  editar (x: Locacao) { this.item = { ...x }; this.modal = true; this.erro = ''; this.carregar() }
  fechar () { if (!this.processando) this.modal = false }
  cancelar (x: Locacao) {
    if (this.processando || !confirm('Cancelar esta locação e liberar o item?')) return
    this.processando = true
    this.erroLista = ''
    this.api.cancelar(x.id).pipe(finalize(() => this.processando = false)).subscribe({
      next: () => this.carregar(),
      error: e => this.erroLista = e.error?.message || 'Não foi possível cancelar a locação.'
    })
  }
}

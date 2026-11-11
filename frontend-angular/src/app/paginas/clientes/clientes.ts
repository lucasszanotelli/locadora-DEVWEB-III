import { Component, OnInit, inject } from '@angular/core'
import { FormsModule } from '@angular/forms'
import { finalize } from 'rxjs'
import { CabecalhoPagina } from '../../componentes-genericos/cabecalho-pagina/cabecalho-pagina'
import { Botao } from '../../componentes-genericos/botao/botao'
import { Modal } from '../../componentes-genericos/modal/modal'
import { Status } from '../../componentes-genericos/status/status'
import { Cliente, LocadoraService } from '../../dados/locadora.service'
import { ClienteService } from '../../dados/cliente.service'

@Component({
  selector: 'app-clientes',
  standalone: true,
  imports: [FormsModule, CabecalhoPagina, Botao, Modal, Status],
  templateUrl: './clientes.html'
})
export class Clientes implements OnInit {
  private api = inject(ClienteService)
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
    this.api.listar().pipe(finalize(() => this.carregando = false)).subscribe({
      next: xs => this.dados.clientes.set(xs),
      error: () => this.erroLista = 'Não foi possível carregar os clientes. Verifique se o backend está em execução.'
    })
  }
  vazio (): Cliente {
    return {
      id: 0,
      inscricao: 'Gerada automaticamente',
      nome: '',
      tipo: 'Sócio',
      cpf: '',
      telefone: '',
      endereco: '',
      sexo: '',
      nascimento: '',
      ativo: true
    }
  }
  get lista () {
    return this.dados
      .clientes()
      .filter(x => (x.nome + x.inscricao + x.cpf).toLowerCase().includes(this.busca.toLowerCase()))
  }
  nomeSocio (id?: number) {
    return this.dados.clientes().find(x => x.id === id)?.nome || '—'
  }
  novo () {
    this.item = this.vazio()
    this.modal = true
    this.erro = ''
  }
  editar (x: Cliente) {
    this.item = { ...x }
    this.modal = true
    this.erro = ''
  }
  salvar () {
    if (this.processando) return
    if (
      !this.item.nome ||
      !this.item.sexo ||
      !this.item.nascimento ||
      (this.item.tipo === 'Sócio' && (!this.item.cpf || !this.item.endereco || !this.item.telefone))
    ) {
      this.erro = 'Preencha todos os dados obrigatórios.'
      return
    }
    if (this.item.tipo === 'Dependente') {
      const qtd = this.dados
        .clientes()
        .filter(x => x.socioId === this.item.socioId && x.ativo && x.id !== this.item.id).length
      if (qtd >= 3) {
        this.erro = 'O sócio selecionado já possui três dependentes ativos.'
        return
      }
    }
    const { id, inscricao, ...cliente } = this.item
    const requisicao = id ? this.api.atualizar(id, cliente) : this.api.inserir(cliente)
    this.processando = true
    this.apiRequisicao(requisicao, salvo => {
      const atual = this.dados.clientes()
      this.dados.clientes.set(id ? atual.map(x => x.id === salvo.id ? salvo : x) : [...atual, salvo])
      this.modal = false
    })
  }
  alternar (x: Cliente) {
    if (this.processando) return
    this.processando = true
    this.apiRequisicao(this.api.alternarStatus(x.id), salvo => {
      this.dados.clientes.update(xs => xs.map(y => y.id === salvo.id || (salvo.tipo === 'Sócio' && y.socioId === salvo.id) ? { ...y, ativo: salvo.ativo } : y))
    })
  }
  excluir (x: Cliente) {
    if (x.possuiLocacoes) {
      alert('O cliente possui locações e não pode ser excluído.')
      return
    }
    if (this.processando || !confirm('Excluir este cliente e seus dependentes?')) return
    this.processando = true
    this.api.excluir(x.id).pipe(finalize(() => this.processando = false)).subscribe({
      next: () => this.dados.clientes.update(xs => xs.filter(y => y.id !== x.id && y.socioId !== x.id)),
      error: () => this.erroLista = 'Não foi possível excluir o cliente. Atualize a lista e tente novamente.'
    })
  }
  private apiRequisicao (requisicao: ReturnType<ClienteService['inserir']>, sucesso: (cliente: Cliente) => void) {
    this.erro = ''
    requisicao.pipe(finalize(() => this.processando = false)).subscribe({
      next: sucesso,
      error: e => this.erro = e.status === 400 ? (e.error?.message || 'Confira os dados informados.') : 'Não foi possível salvar o cliente. Verifique a conexão com o backend.'
    })
  }
}

import { Injectable, inject } from '@angular/core'
import { HttpClient } from '@angular/common/http'
import { Locacao } from './locadora.service'

export interface DadosLocacao {
  clienteId: number
  itemId: number
  prevista?: string
  valor?: number
  paga: boolean
}

@Injectable({ providedIn: 'root' })
export class LocacaoService {
  private http = inject(HttpClient)
  private url = '/api/locacoes'
  listar () { return this.http.get<Locacao[]>(this.url) }
  inserir (dados: DadosLocacao) { return this.http.post<Locacao>(this.url, dados) }
  atualizar (id: number, dados: DadosLocacao) { return this.http.put<Locacao>(`${this.url}/${id}`, dados) }
  cancelar (id: number) { return this.http.delete<void>(`${this.url}/${id}`) }
  vigente (serie: string) { return this.http.get<Locacao>(`${this.url}/vigente`, { params: { serie } }) }
  devolver (id: number) { return this.http.post<Locacao>(`${this.url}/${id}/devolucao`, {}) }
}

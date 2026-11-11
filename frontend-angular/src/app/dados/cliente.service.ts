import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Cliente } from './locadora.service';

@Injectable({ providedIn: 'root' })
export class ClienteService {
  private http = inject(HttpClient);
  private url = '/api/clientes';

  listar() { return this.http.get<Cliente[]>(this.url); }
  inserir(cliente: Omit<Cliente, 'id' | 'inscricao'>) { return this.http.post<Cliente>(this.url, cliente); }
  atualizar(id: number, cliente: Omit<Cliente, 'id' | 'inscricao'>) { return this.http.put<Cliente>(`${this.url}/${id}`, cliente); }
  alternarStatus(id: number) { return this.http.patch<Cliente>(`${this.url}/${id}/status`, {}); }
  excluir(id: number) { return this.http.delete<void>(`${this.url}/${id}`); }
}

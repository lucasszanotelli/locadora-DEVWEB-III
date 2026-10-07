import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';

export interface Ator {
  id: number;
  nome: string;
  nacionalidade: string;
  dataNascimento: string;
}
export type DadosAtor = Omit<Ator, 'id'>;

@Injectable({ providedIn: 'root' })
export class AtorService {
  private http = inject(HttpClient);
  private url = '/api/atores';

  listar() { return this.http.get<Ator[]>(this.url); }
  inserir(ator: DadosAtor) { return this.http.post<Ator>(this.url, ator); }
  atualizar(id: number, ator: DadosAtor) { return this.http.put<Ator>(`${this.url}/${id}`, ator); }
  excluir(id: number) { return this.http.delete<void>(`${this.url}/${id}`); }
}

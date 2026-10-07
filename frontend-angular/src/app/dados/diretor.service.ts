import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';

export interface Diretor {
  id: number;
  nome: string;
  nacionalidade: string;
  dataNascimento: string;
}

export type DadosDiretor = Omit<Diretor, 'id'>;

@Injectable({ providedIn: 'root' })
export class DiretorService {
  private http = inject(HttpClient);
  private url = '/api/diretores';

  listar() { return this.http.get<Diretor[]>(this.url); }
  inserir(diretor: DadosDiretor) { return this.http.post<Diretor>(this.url, diretor); }
  atualizar(id: number, diretor: DadosDiretor) { return this.http.put<Diretor>(`${this.url}/${id}`, diretor); }
  excluir(id: number) { return this.http.delete<void>(`${this.url}/${id}`); }
}

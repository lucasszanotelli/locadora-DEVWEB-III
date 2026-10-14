import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Classe } from './locadora.service';

@Injectable({ providedIn: 'root' })
export class ClasseService {
  private http = inject(HttpClient);
  private url = '/api/classes';

  listar() { return this.http.get<Classe[]>(this.url); }
  inserir(classe: Omit<Classe, 'id'>) { return this.http.post<Classe>(this.url, classe); }
  atualizar(id: number, classe: Omit<Classe, 'id'>) { return this.http.put<Classe>(`${this.url}/${id}`, classe); }
  excluir(id: number) { return this.http.delete<void>(`${this.url}/${id}`); }
}

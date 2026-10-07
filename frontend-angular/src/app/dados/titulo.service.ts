import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';

export interface TituloApi {
  id: number; nome: string; nomeOriginal: string; ano: number; categoria: string;
  classe: string; diretor: string; atores: string[]; nacionalidade: string;
  distribuidor: string; sinopse: string;
}
export type DadosTitulo = Omit<TituloApi, 'id'>;

@Injectable({ providedIn: 'root' })
export class TituloService {
  private http = inject(HttpClient);
  private url = '/api/titulos';
  listar() { return this.http.get<TituloApi[]>(this.url); }
  inserir(titulo: DadosTitulo) { return this.http.post<TituloApi>(this.url, titulo); }
  atualizar(id: number, titulo: DadosTitulo) { return this.http.put<TituloApi>(`${this.url}/${id}`, titulo); }
  excluir(id: number) { return this.http.delete<void>(`${this.url}/${id}`); }
}

import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';

export interface ItemApi {
  serial: number; dataAquisicao: string; tipoItem: 'DVD' | 'Blu-ray' | 'Fita';
  numeroSerie: string; titulo: string; status: 'Disponível' | 'Locado' | 'Manutenção';
}
export type DadosItem = Omit<ItemApi, 'serial'>;

@Injectable({ providedIn: 'root' })
export class ItemService {
  private http = inject(HttpClient);
  private url = '/api/itens';
  listar() { return this.http.get<ItemApi[]>(this.url); }
  inserir(item: DadosItem) { return this.http.post<ItemApi>(this.url, item); }
  atualizar(serial: number, item: DadosItem) { return this.http.put<ItemApi>(`${this.url}/${serial}`, item); }
  excluir(serial: number) { return this.http.delete<void>(`${this.url}/${serial}`); }
}

import { Component, EventEmitter, Input, Output } from '@angular/core';

@Component({ selector:'app-botao', standalone:true, templateUrl:'./botao.html', styleUrl:'./botao.css' })
export class Botao {
  @Input() tipo: 'primario'|'secundario'|'perigo'|'icone' = 'primario';
  @Input() htmlType: 'button'|'submit' = 'button';
  @Input() desabilitado = false;
  @Input() icone = '';
  @Output() acionado = new EventEmitter<void>();
}

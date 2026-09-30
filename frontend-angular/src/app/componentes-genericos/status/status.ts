import { Component, Input } from '@angular/core';
@Component({selector:'app-status',standalone:true,template:'<span [class]="tipo">{{texto}}</span>',styles:['span{display:inline-block;padding:5px 9px;border-radius:999px;font-size:10px;font-weight:750;letter-spacing:.25px;text-transform:uppercase}.sucesso{background:#e8f7ef;color:#24784b}.alerta{background:#fff4db;color:#996917}.erro{background:#ffebe8;color:#b84434}.neutro{background:#edf0f4;color:#5f6978}']})
export class Status{@Input() texto='';@Input() tipo:'sucesso'|'alerta'|'erro'|'neutro'='neutro';}

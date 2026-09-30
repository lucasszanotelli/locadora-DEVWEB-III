import { Component, EventEmitter, Input, Output } from '@angular/core';
@Component({selector:'app-modal',standalone:true,templateUrl:'./modal.html',styleUrl:'./modal.css'})
export class Modal{@Input() aberto=false;@Input() titulo='';@Input() largura='620px';@Output() fechar=new EventEmitter<void>();}

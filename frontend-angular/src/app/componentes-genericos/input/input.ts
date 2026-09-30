import { Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
  selector: 'app-input',
  standalone: true,
  templateUrl: './input.html',
  styleUrl: './input.css',
})
export class InputComponent {
  @Input() label = '';
  @Input() placeholder = '';
  @Input() type = 'text';
  @Input() value: string | number = '';
  @Input() required = false;
  @Input() erro = '';
  @Input() min?: string | number;
  @Output() valueChange = new EventEmitter<string>();
}

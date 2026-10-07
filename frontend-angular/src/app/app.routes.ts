import { Atores } from './paginas/atores/atores';
import { Routes } from '@angular/router';
import { Inicio } from './paginas/inicio/inicio';
import { CadastroSimples } from './paginas/cadastro-simples/cadastro-simples';
import { Titulos } from './paginas/titulos/titulos';
import { Itens } from './paginas/itens/itens';
import { Clientes } from './paginas/clientes/clientes';
import { Locacoes } from './paginas/locacoes/locacoes';
import { Devolucoes } from './paginas/devolucoes/devolucoes';
import { Catalogo } from './paginas/catalogo/catalogo';

export const routes: Routes = [
  {path:'',pathMatch:'full',redirectTo:'inicio'},
  {path:'inicio',component:Inicio},
  {path:'titulos',component:Titulos},
  {path:'itens',component:Itens},
  {path:'atores',component:Atores},
  {path:'diretores',component:CadastroSimples,data:{tipo:'diretores',titulo:'Diretores'}},
  {path:'classes',component:CadastroSimples,data:{tipo:'classes',titulo:'Classes'}},
  {path:'clientes',component:Clientes},
  {path:'locacoes',component:Locacoes},
  {path:'devolucoes',component:Devolucoes},
  {path:'catalogo',component:Catalogo},
  {path:'**',redirectTo:'inicio'},
];

import { Routes } from '@angular/router';
import { Inicio  } from './inicio/inicio';
import { Login } from './login/login';
import { RegistroComponent } from './registro-component/registro-component';
import { PerfilComponent } from './perfil-component/perfil-component';
import { CrearProductoComponent } from './crear-producto-component/crear-producto-component';
import { MenuComponent } from './menu-component/menu-component';
import { PersonalizarPedidoComponent } from './personalizar-pedido-component/personalizar-pedido-component';
import { CarritoComponent } from './carrito-component/carrito-component';
import { MisPedidosComponent } from './mis-pedidos-component/mis-pedidos-component';
import { CrearTrabajadorComponent } from './crear-trabajador-component/crear-trabajador-component';
import { PedidosPendientesComponent } from './pedidos-pendientes-component/pedidos-pendientes-component';
import { CanjearPromoComponent } from './canjear-promo-component/canjear-promo-component';
import { VentasManualesComponent } from './ventas-manuales-component/ventas-manuales-component';
import { AuthGuard } from './guards/auth-guard';
import { RoleGuard } from './guards/role-guard';

export const routes: Routes = [
    // Rutas PÚBLICAS
    { path: 'inicio', component: Inicio },
    { path: 'login', component: Login },
    { path: 'registro', component: RegistroComponent },
    { path: 'menu', component: MenuComponent },
    
    // Rutas para CLIENTES (requieren login)
    { path: 'perfil', component: PerfilComponent, canActivate: [AuthGuard] },
    { path: 'personalizar/:id', component: PersonalizarPedidoComponent, canActivate: [AuthGuard] },
    { path: 'carrito', component: CarritoComponent, canActivate: [AuthGuard] },
    { path: 'mis-pedidos', component: MisPedidosComponent, canActivate: [AuthGuard] },
    
    // Rutas para ADMIN/TRABAJADORES
    { path: 'crear-producto', component: CrearProductoComponent, canActivate: [RoleGuard] },
    { path: 'crear-trabajador', component: CrearTrabajadorComponent, canActivate: [RoleGuard] },
    { path: 'pedidos-pendientes', component: PedidosPendientesComponent, canActivate: [RoleGuard] },
    { path: 'canjear-promo', component: CanjearPromoComponent, canActivate: [RoleGuard] },
    { path: 'venta-manual', component: VentasManualesComponent, canActivate: [RoleGuard] },
    
    // Redirección por defecto
    { path: '', redirectTo: '/inicio', pathMatch: 'full' }
];

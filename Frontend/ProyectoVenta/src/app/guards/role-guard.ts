/*import { Injectable } from '@angular/core';
import { CanActivate, Router } from '@angular/router';
import { AuthService } from '../servicios/auth-service';

@Injectable({
  providedIn: 'root'
})
export class RoleGuard implements CanActivate {
  
  constructor(private authService: AuthService, private router: Router) {}

  canActivate(): boolean {
    const role = this.authService.getUserRole();
    
    // Solo admin y trabajador pueden acceder
    if (role === 'admin' || role === 'trabajador') {
      return true;
    } else {
      this.router.navigate(['/inicio']);
      return false;
    }
  }
}*/

import { Injectable } from '@angular/core';
import { CanActivate, Router } from '@angular/router';
import { AuthService } from '../servicios/auth-service';

@Injectable({
  providedIn: 'root'
})
export class RoleGuard implements CanActivate {
  
  constructor(private authService: AuthService, private router: Router) {}

  canActivate(): boolean {
    const role = this.authService.getUserRole();

    console.log('=== DEBUG RoleGuard ===');
    console.log('Rol del usuario:', role);
    console.log('¿Es admin?:', role === 'admin');
    console.log('¿Es trabajador?:', role === 'trabajador');
    console.log('¿Tiene acceso?:', role === 'admin' || role === 'trabajador');
    console.log('Ruta intentada:', window.location.pathname);
    console.log('=======================');
    

    if (role === 'admin' || role === 'trabajador') {
      console.log('✅ ACCESO CONCEDIDO');
      return true;
    } else {
      console.log('❌ ACCESO DENEGADO - Redirigiendo a /inicio');
      this.router.navigate(['/inicio']);
      return false;
    }
  }
}